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
package com.mcmoddev.mmdbot.commander.custompings;

import static com.mcmoddev.mmdbot.core.util.Constants.Gsons.NO_PRETTY_PRINTING;
import com.google.common.base.Suppliers;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.migrate.TricksMigrator;
import com.mcmoddev.mmdbot.core.database.SnowflakeStorage;
import com.mcmoddev.mmdbot.core.database.VersionedDataMigrator;
import com.mcmoddev.mmdbot.core.database.VersionedDatabase;
import com.mcmoddev.mmdbot.core.dfu.Codecs;
import com.mcmoddev.mmdbot.core.util.Constants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import lombok.experimental.UtilityClass;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

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
     * The codec used for serializing custom pings.
     */
    public static final Codec<SnowflakeStorage<SnowflakeStorage<List<CustomPing>>>> CODEC = SnowflakeStorage.codec(
        SnowflakeStorage.codec(Codecs.mutableList(CustomPing.CODEC))
    );

    /**
     * Example data:
     * guildId: {
     *     memberId: [
     *         {
     *             pattern: pattern1,
     *             text: text1
     *         },
     *         {
     *             pattern: pattern2,
     *             text: text2
     *         }
     *     ],
     *     memberId2: [
     *         {
     *             pattern: pattern1,
     *             text: text1
     *         },
     *         {
     *             pattern: pattern2,
     *             text: text2
     *         }
     *     ]
     * }
     */
    private static SnowflakeStorage<SnowflakeStorage<List<CustomPing>>> pings;

    public static SnowflakeStorage<SnowflakeStorage<List<CustomPing>>> getPings() {
        if (pings != null) return pings;
        final var path = PATH.get();
        if (!Files.exists(path)) {
            return pings = new SnowflakeStorage<>();
        }
        final var data = VersionedDatabase.fromFile(path, CODEC, CURRENT_SCHEMA_VERSION, new SnowflakeStorage<>())
            .flatMap(db -> {
                if (db.getSchemaVersion() != CURRENT_SCHEMA_VERSION) {
                    new TricksMigrator(TheCommander.getInstance().getRunPath()).migrate();
                    final var newDb = VersionedDatabase.fromFile(path, CODEC, CURRENT_SCHEMA_VERSION, new SnowflakeStorage<>());
                    return newDb.map(VersionedDatabase::getData);
                } else {
                    return DataResult.success(db.getData());
                }
            });
        if (data.result().isPresent()) {
            return pings = data.result().get();
        } else if (data.error().isPresent()) {
            TheCommander.LOGGER.error("Reading quotes file encountered an error: {}", data.error().get().message());
            return pings = new SnowflakeStorage<>();
        } else {
            return pings = new SnowflakeStorage<>(); // this shouldn't be reached
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
            final var result = db.toJson(CODEC);
            Constants.Gsons.NO_PRETTY_PRINTING.toJson(result.result()
                    .orElseThrow(
                        () -> new IOException(result.error()
                            .orElseThrow() // throw if the message doesn't exist... that would be weird
                            .message())
                    ),
                writer);
        } catch (final IOException e) {
            TheCommander.LOGGER.error("An IOException occurred saving custom pings...", e);
        }
    }

    public static SnowflakeStorage<List<CustomPing>> getAllPingsInGuild(final long guildId) {
        return getPings().computeIfAbsent(guildId, k -> new SnowflakeStorage<>());
    }

    public static void addPing(final long guildId, final long memberId, final CustomPing ping) {
        getPingsForUser(guildId, memberId).add(ping);
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

}
