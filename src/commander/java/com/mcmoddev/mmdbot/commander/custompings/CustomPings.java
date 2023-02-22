/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2023 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
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
package com.mcmoddev.mmdbot.commander.custompings;

import com.google.common.base.Suppliers;
import com.google.gson.reflect.TypeToken;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.core.database.VersionedDataMigrator;
import com.mcmoddev.mmdbot.core.database.VersionedDatabase;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.experimental.UtilityClass;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.mcmoddev.mmdbot.core.util.Constants.Gsons.NO_PRETTY_PRINTING;
import static it.unimi.dsi.fastutil.longs.Long2ObjectMaps.synchronize;
import static java.util.Collections.synchronizedMap;

@UtilityClass
public class CustomPings {

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
     * A function that resolves the path of the custom pings file from the bot run path.
     */
    public static final UnaryOperator<Path> PATH_RESOLVER = p -> p.resolve("custom_pings.json");

    /**
     * The path of the custom pings file.
     */
    public static final Supplier<Path> PATH = Suppliers.memoize(() -> CustomPings.PATH_RESOLVER.apply(TheCommander.getInstance().getRunPath()));

    /**
     * The type of the custom pings.
     */
    private static final Type TYPE = new com.google.common.reflect.TypeToken<Map<Long, Map<Long, List<CustomPing>>>>() {
    }.getType();

    /**
     * Example data:
     * {@code
     * guildId: {
     *  memberId: [
     *      {
     *          pattern: pattern1,
     *          text: text1
     *      },
     *      {
     *          pattern: pattern2,
     *          text: text2
     *      }
     *  ],
     *  memberId2: [
     *      {
     *          pattern: pattern1,
     *          text: text1
     *      },
     *      {
     *          pattern: pattern2,
     *          text: text2
     *      }
     *  ]
     * }
     * }
     */
    private static Long2ObjectMap<Map<Long, List<CustomPing>>> pings;

    public static Long2ObjectMap<Map<Long, List<CustomPing>>> getPings() {
        if (pings != null) return pings;
        final var path = PATH.get();
        if (!Files.exists(path)) {
            return pings = synchronize(new Long2ObjectOpenHashMap<>());
        }
        try {
            final var db = VersionedDatabase.<Map<Long, Map<Long, List<CustomPing>>>>fromFile(NO_PRETTY_PRINTING, path, TYPE, CURRENT_SCHEMA_VERSION, new HashMap<>());
            if (db.getSchemaVersion() != CURRENT_SCHEMA_VERSION) {
                MIGRATOR.migrate(CURRENT_SCHEMA_VERSION, path);
                final var newDb = VersionedDatabase.<Map<Long, Map<Long, List<CustomPing>>>>fromFile(NO_PRETTY_PRINTING, path, TYPE, CURRENT_SCHEMA_VERSION, new HashMap<>());
                return pings = synchronize(new Long2ObjectOpenHashMap<>(newDb.getData()));
            } else {
                return pings = synchronize(new Long2ObjectOpenHashMap<>(db.getData()));
            }
        } catch (final IOException exception) {
            TheCommander.LOGGER.error("Failed to read custom pings file...", exception);
            return pings = synchronize(new Long2ObjectOpenHashMap<>());
        }
    }

    /**
     * Write the pings to disk.
     */
    private static void write() {
        final var path = PATH.get();
        final var rems = getPings();
        final var db = VersionedDatabase.inMemory(CURRENT_SCHEMA_VERSION, rems);
        try (var writer = new OutputStreamWriter(new FileOutputStream(path.toFile()), StandardCharsets.UTF_8)) {
            NO_PRETTY_PRINTING.toJson(db.toJson(NO_PRETTY_PRINTING), writer);
        } catch (final IOException e) {
            TheCommander.LOGGER.error("An IOException occurred saving custom pings...", e);
        }
    }

    public static Map<Long, List<CustomPing>> getAllPingsInGuild(final long guildId) {
        return getPings().computeIfAbsent(guildId, k -> synchronizedMap(new HashMap<>()));
    }

    public static void addPing(final long guildId, final long memberId, final CustomPing ping) {
        getPingsForUser(guildId, memberId).add(ping);
        write();

    }

    public static void addPings(final long guildId, final long memberId, final List<CustomPing> pings) {
        getPingsForUser(guildId, memberId).addAll(pings);
        write();
    }

    public static void removePing(final long guildId, final long memberId, final CustomPing ping) {
        getPingsForUser(guildId, memberId).remove(ping);
        write();
    }

    public static void clearPings(final long guildId, final long memberId) {
        getPingsForUser(guildId, memberId).clear();
        write();
    }

    public static List<CustomPing> getPingsForUser(final long guildId, final long memberId) {
        return getAllPingsInGuild(guildId).computeIfAbsent(memberId, k -> Collections.synchronizedList(new ArrayList<>()));
    }

    public static List<CustomPing> fromString(final String string) {
        return NO_PRETTY_PRINTING.fromJson(string, new TypeToken<>() {});
    }

    public static String toString(List<CustomPing> pings) {
        return NO_PRETTY_PRINTING.toJson(pings);
    }
}
