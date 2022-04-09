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
package com.mcmoddev.mmdbot.commander.reminders;

import static java.util.Collections.synchronizedMap;
import com.google.common.base.Suppliers;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.core.database.SnowflakeStorage;
import com.mcmoddev.mmdbot.core.database.VersionedDataMigrator;
import com.mcmoddev.mmdbot.core.database.VersionedDatabase;
import com.mcmoddev.mmdbot.core.dfu.Codecs;
import com.mcmoddev.mmdbot.core.util.Constants;
import com.mcmoddev.mmdbot.core.util.Utils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.experimental.UtilityClass;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@UtilityClass
public class Reminders {

    /**
     * The current schema version used for data migration.
     */
    public static final int CURRENT_SCHEMA_VERSION = 1;

    /**
     * The migrator.
     */
    public static final VersionedDataMigrator MIGRATOR = VersionedDataMigrator.builder()
        .build();

    /**
     * A function that resolves the path of the reminders file from the bot run path.
     */
    public static final UnaryOperator<Path> PATH_RESOLVER = p -> p.resolve("reminders.json");

    /**
     * The path of the reminders file.
     */
    public static final Supplier<Path> PATH = Suppliers.memoize(() -> PATH_RESOLVER.apply(TheCommander.getInstance().getRunPath()));

    /**
     * The timer that runs reminders.
     */
    private static final ScheduledExecutorService TIMER = Executors.newSingleThreadScheduledExecutor(r -> Utils.setThreadDaemon(new Thread(r, "Reminders"), true));

    /**
     * The codec used for serializing reminders.
     */
    private static final Codec<SnowflakeStorage<List<Reminder>>> CODEC = SnowflakeStorage.codec(Codecs.mutableList(Reminder.CODEC));

    private static SnowflakeStorage<List<Reminder>> reminders;

    public static SnowflakeStorage<List<Reminder>> getReminders() {
        if (reminders != null) return reminders;
        final var path = PATH.get();
        if (!Files.exists(path)) {
            return reminders = new SnowflakeStorage<>();
        }
        final var data = VersionedDatabase.fromFile(path, CODEC, CURRENT_SCHEMA_VERSION, new SnowflakeStorage<>())
            .flatMap(db -> {
                if (db.getSchemaVersion() != CURRENT_SCHEMA_VERSION) {
                    try {
                        MIGRATOR.migrate(CURRENT_SCHEMA_VERSION, path);
                    } catch (IOException e) {
                        TheCommander.LOGGER.error("Exception migrating reminders: ", e);
                    }
                    final var newDb = VersionedDatabase.fromFile(path, CODEC, CURRENT_SCHEMA_VERSION, new SnowflakeStorage<>());
                    return newDb.map(VersionedDatabase::getData);
                } else {
                    return DataResult.success(db.getData());
                }
            });
        if (data.result().isPresent()) {
            return reminders = data.result().get();
        } else if (data.error().isPresent()) {
            TheCommander.LOGGER.error("Reading reminders file encountered an error: {}", data.error().get().message());
            return reminders = new SnowflakeStorage<>();
        } else {
            return reminders = new SnowflakeStorage<>(); // this shouldn't be reached
        }
    }

    /**
     * Write the reminders to disk.
     */
    private static void write() {
        final var path = PATH.get();
        final var rems = getReminders();
        final var db = VersionedDatabase.inMemory(CURRENT_SCHEMA_VERSION, rems);
        try (var writer = new OutputStreamWriter(new FileOutputStream(path.toFile()), StandardCharsets.UTF_8)) {
            final var result = db.toJson(CODEC);
            Constants.Gsons.NO_PRETTY_PRINTING.toJson(result.result()
                    .orElseThrow(
                        () -> new IOException(result.error()
                            .orElseThrow() // throw if the message doesn't exist... that would be weird
                            .message())
                    ),
                writer);
        } catch (final IOException e) {
            TheCommander.LOGGER.error("An IOException occurred saving reminders...", e);
        }
    }

    /**
     * Schedules all the reminders to be run.
     */
    public static void scheduleAllReminders() {
        getReminders().values().forEach(l -> l.forEach(Reminders::registerReminder));
    }

    /**
     * Gets all the reminders a user has.
     *
     * @param userId the ID of the user to query the reminders from
     * @return the user's reminders
     */
    public static List<Reminder> getRemindersForUser(final long userId) {
        return getReminders().computeIfAbsent(userId, k -> new ArrayList<>());
    }

    /**
     * Clears all reminders from a user.
     *
     * @param userId the id of the user to clear reminders from
     */
    public static void clearAllUserReminders(final long userId) {
        getRemindersForUser(userId).forEach(Reminders::removeReminder);
    }

    /**
     * Checks if a user reached the max pending reminders.
     *
     * @param userId the id of the user to search reminders
     * @return if the user reached the max pending reminders.
     */
    public static boolean userReachedMax(final long userId) {
        return getRemindersForUser(userId).size() >= TheCommander.getInstance().getGeneralConfig().features().reminders().getLimitPerUser();
    }

    /**
     * Removes a reminder.
     *
     * @param reminder the reminder to remove.
     */
    public static void removeReminder(final Reminder reminder) {
        reminder.removed().set(true);
        getRemindersForUser(reminder.ownerId()).remove(reminder);
        write();
    }

    /**
     * Adds and schedules a reminder.
     *
     * @param reminder the reminder to add.
     */
    public static void addReminder(final Reminder reminder) {
        getRemindersForUser(reminder.ownerId()).add(reminder);
        registerReminder(reminder);
        write();
    }

    /**
     * Schedules a reminder.
     *
     * @param reminder the reminder to schedule
     */
    public static void registerReminder(final Reminder reminder) {
        Utils.scheduleTask(TIMER, () -> {
            reminder.run();
            removeReminder(reminder);
        }, reminder.time());
    }
}
