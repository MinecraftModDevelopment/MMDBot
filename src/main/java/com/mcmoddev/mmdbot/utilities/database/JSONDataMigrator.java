/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
package com.mcmoddev.mmdbot.utilities.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.jagrosh.jdautilities.commons.utils.SafeIdUtil;
import com.mcmoddev.mmdbot.utilities.database.dao.PersistedRoles;
import com.mcmoddev.mmdbot.utilities.database.dao.UserFirstJoins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Migrates data from the old JSON flat-file storage to the SQL database tables.
 *
 * @author sciwhiz12
 */
public final class JSONDataMigrator {
    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JSONDataMigrator.class);

    /**
     * The shared Gson instance, used to load JSON files.
     *
     * @see InstantDeserializer
     */
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Instant.class, new InstantDeserializer())
        .create();

    /**
     * The type token for the user first join times data, which represents a {@code Map<String, Instant>}.
     */
    private static final TypeToken<?> USER_FIRST_JOIN_TIMES_TYPE = TypeToken.getParameterized(
        Map.class, String.class, Instant.class);

    /**
     * The type token for the stick roles data, which represents a {@code Map<String, List<String>>}.
     */
    private static final TypeToken<?> STICKY_ROLES_TYPE = TypeToken.getParameterized(
        Map.class, String.class, TypeToken.getParameterized(List.class, String.class).getType());

    /**
     * The extension appended to JSON data files once their data has been migrated.
     */
    private static final String MIGRATED_FILE_EXTENSION = ".old";
    /**
     * The name of the JSON file that contains the user first join times data.
     */
    public static final String USER_JOIN_TIMES_FILE_PATH = "mmdbot_user_join_times.json";
    /**
     * The name of the JSON file that contains the sticky roles data.
     */
    public static final String STICKY_ROLES_FILE_PATH = "mmdbot_sticky_roles.json";

    /**
     * Utility classes should not be constructed.
     */
    private JSONDataMigrator() { // Prevent instantiation
    }

    /**
     * Checks for existing JSON files and migrates them to the database if they exist. JSON data migrations which fail
     * due to file loading or SQL errors will not cause the application to stop, but the file will remain in place to
     * allow repeating the migration for the next time this method is called (on application bootup).
     *
     * @param database the database manager
     */
    public static void checkAndMigrate(final DatabaseManager database) {
        LOGGER.debug("Checking for JSON files to migrate");

        // User first join times
        Path joinTimesFile = Path.of(USER_JOIN_TIMES_FILE_PATH);
        if (Files.exists(joinTimesFile) && Files.isRegularFile(joinTimesFile) && Files.isReadable(joinTimesFile)) {
            LOGGER.info("Found JSON file for user first join times data, migrating...");

            migrate("user first join times", joinTimesFile, reader -> {
                final Map<String, Instant> joinTimes = GSON.fromJson(reader, USER_FIRST_JOIN_TIMES_TYPE.getType());
                database.jdbi().useExtension(UserFirstJoins.class, j -> j.useTransaction(joins ->
                    joinTimes.forEach((idStr, timestamp) -> {
                        final long id = SafeIdUtil.safeConvert(idStr);
                        if (id == 0L) {
                            LOGGER.warn("Could not parse user ID {}", idStr);
                        } else {
                            if (joins.get(id).isEmpty()) {
                                joins.insert(id, timestamp);
                            }
                        }
                    })
                ));
            });

            LOGGER.info("Migrated user first join times data");
        }

        // Sticky roles
        Path rolesFile = Path.of(STICKY_ROLES_FILE_PATH);
        if (Files.exists(rolesFile) && Files.isRegularFile(rolesFile) && Files.isReadable(rolesFile)) {
            LOGGER.info("Found JSON file for sticky roles data, migrating...");

            migrate("sticky roles", rolesFile, reader -> {
                final Map<String, List<String>> joinTimes = GSON.fromJson(reader, STICKY_ROLES_TYPE.getType());

                database.jdbi().useExtension(PersistedRoles.class, r -> r.useTransaction(roles ->
                    joinTimes.forEach((idStr, rolesList) -> {
                        final long id = SafeIdUtil.safeConvert(idStr);
                        if (id == 0L) {
                            LOGGER.warn("Could not parse user ID {}", idStr);
                        } else {
                            roles.insert(id, rolesList.stream()
                                .map(SafeIdUtil::safeConvert)
                                .filter(s -> s != 0)
                                .toList());
                        }
                    })
                ));
            });

            LOGGER.info("Migrated sticky roles data");
        }
    }

    /**
     * Performs a migration on the data from the given file using the given consumer. The consumer is passed a
     * {@link Reader} which is opened on the given file (which does not need to be closed by the consumer). After the
     * consumer does its operation, the given file is then renamed according to {@link #renameMigratedFile(Path)}.
     *
     * <p>If an exception occurs while loading the data or while performing SQL operations, the exception is logged
     * using the given {@code migrateType} string in the log message to describe the migration. The file is only
     * renamed if there are no exceptions thrown.</p>
     *
     * @param migrateType a string describing what migration is taking place
     * @param file        the file containing the data to be migrated
     * @param migrator    the operation which migrates the data using the passed in reader
     * @param <T>         the throwable
     * @throws T used to sneakily throw caught throwables without the JVM requiring caller methods to catch or declare
     *           the exception in their method signature
     */
    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void migrate(final String migrateType, final Path file,
                                                      final ThrowingConsumer<Reader> migrator) throws T {
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            migrator.acceptThrows(reader);

            renameMigratedFile(file);
        } catch (final IOException exception) {
            LOGGER.warn("""
                    Could not load %s data for migration.
                    Please fix the error, and restart the application to try migration again.""".formatted(migrateType),
                exception);
        } catch (final Throwable exception) {
            if (exception instanceof SQLException) {
                LOGGER.warn("""
                        Could not insert %s data into database.
                        Please fix the error, and restart the application to try migration again.""".formatted(migrateType),
                    exception);
                return;
            }
            throw (T) exception;
        }
    }

    /**
     * Renames the file to append the {@link #MIGRATED_FILE_EXTENSION} ({@value #MIGRATED_FILE_EXTENSION}), and logs
     * any exceptions silently.
     *
     * @param file the file to rename
     */
    private static void renameMigratedFile(final Path file) {
        try {
            Files.move(file, file.resolveSibling(file.getFileName() + MIGRATED_FILE_EXTENSION),
                StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException exception) {
            LOGGER.warn("Failed to rename migrated data file {}", file, exception);
        }
    }

    /**
     * Represents an operation that accepts a single input argument and returns no
     * result, which may throw an exception. Like its non-throwing counterpart,
     * {@code ThrowingConsumer} is expected to operate via side-effects.
     *
     * <p>This is a <a href="package-summary.html">functional interface</a>
     * whose functional method is {@link #acceptThrows(Object)}.</p>
     *
     * @param <T> the type of the input to the operation
     * @see Consumer
     */
    @FunctionalInterface
    private interface ThrowingConsumer<T> {
        /**
         * Performs this operation on the given argument, potentially throwing an exception.
         *
         * @param t the input argument
         */
        void acceptThrows(T t) throws Exception;
    }

    /**
     * A simple deserializer for {@link Instant}. This avoids the use of reflection in creating the Instant objects,
     * allowing for compatibility on newer Java versions where reflecting into classes from modules is disallowed
     * by default.
     */
    private static class InstantDeserializer implements JsonDeserializer<Instant> {
        /**
         * {@inheritDoc}
         */
        @Override
        public Instant deserialize(final JsonElement json, final Type typeOfT,
                                   final JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonNull()) {
                return null;
            }

            final JsonObject jsonObj = json.getAsJsonObject();
            final long seconds = jsonObj.get("seconds").getAsLong();
            final long nanos = jsonObj.get("nanos").getAsLong();

            return Instant.ofEpochSecond(seconds, nanos);
        }
    }
}
