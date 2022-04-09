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
package com.mcmoddev.mmdbot.commander.tricks;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.commands.tricks.RunTrickCommand;
import com.mcmoddev.mmdbot.commander.migrate.TricksMigrator;
import com.mcmoddev.mmdbot.commander.tricks.Trick.TrickType;
import com.mcmoddev.mmdbot.core.database.VersionedDatabase;
import com.mcmoddev.mmdbot.core.dfu.Codecs;
import com.mcmoddev.mmdbot.core.util.Constants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * The Tricks Module.
 *
 * @author Will BL
 */
public final class Tricks {

    /**
     * The current version of the database schema.
     */
    public static final int CURRENT_SCHEMA_VERSION = 1;

    /**
     * The storage location for the tricks file.
     */
    private static final Supplier<Path> TRICK_STORAGE_PATH = () -> TheCommander.getInstance().getRunPath().resolve("tricks.json");

    /**
     * The codec that serializes and deserializes the tricks.
     */
    public static final Codec<List<Trick>> CODEC = Codecs.mutableList(new TrickCodec());

    /**
     * All registered {@link TrickType}s.
     */
    private static final BiMap<String, TrickType<?>> TRICK_TYPES = HashBiMap.create();

    /**
     * All registered tricks.
     */
    private static @Nullable List<Trick> tricks = null;

    /**
     * Gets a trick by name.
     *
     * @param name the name of the trick
     * @return an optional of the trick, or empty if no such trick exists
     */
    public static Optional<Trick> getTrick(final String name) {
        return getTricks().stream().filter(trick -> trick.getNames().contains(name)).findAny();
    }

    /**
     * Gets all the trick names.
     *
     * @return a stream containing all the trick names
     */
    public static Stream<String> getAllNames() {
        return getTricks().stream().flatMap(t -> t.getNames().stream());
    }

    /**
     * Gets all tricks.
     *
     * @return a list of the tricks
     */
    public static List<Trick> getTricks() {
        if (tricks == null) {
            final var path = TRICK_STORAGE_PATH.get();
            if (!Files.exists(path)) {
                return tricks = new ArrayList<>();
            }
            final var data = VersionedDatabase.fromFile(path, CODEC, CURRENT_SCHEMA_VERSION, new ArrayList<>())
                .flatMap(db -> {
                    if (db.getSchemaVersion() != CURRENT_SCHEMA_VERSION) {
                        new TricksMigrator(TheCommander.getInstance().getRunPath()).migrate();
                        final var newDb = VersionedDatabase.fromFile(path, CODEC, CURRENT_SCHEMA_VERSION, new ArrayList<>());
                        return newDb.map(VersionedDatabase::getData);
                    } else {
                        return DataResult.success(db.getData());
                    }
                });
            if (data.result().isPresent()) {
                return tricks = data.result().get();
            } else if (data.error().isPresent()) {
                TheCommander.LOGGER.error("Reading tricks file encountered an error: {}", data.error().get().message());
                return tricks = new ArrayList<>();
            } else {
                return tricks = new ArrayList<>(); // this shouldn't be reached
            }
        }
        return tricks;
    }

    /**
     * Register a new {@link TrickType}.
     *
     * @param name the name to register the type under
     * @param type the type
     */
    public static void registerTrickType(final String name, final Trick.TrickType<?> type) {
        TRICK_TYPES.put(name, type);
    }

    /**
     * Gets all trick types.
     *
     * @return a map where the values are the trick types and the keys are their names
     */
    public static BiMap<String, Trick.TrickType<?>> getTrickTypes() {
        return HashBiMap.create(TRICK_TYPES);
    }

    /**
     * Gets a trick type by name.
     *
     * @param name the name
     * @return the trick type, or null if no such type exists
     */
    public static @Nullable Trick.TrickType<?> getTrickType(final String name) {
        return TRICK_TYPES.get(name);
    }

    /**
     * Gets the name of a trick type.
     *
     * @param type the type whose name to search.
     * @return the name of the trick type, or null if no such type exists
     */
    public static @Nullable String getTrickTypeName(final TrickType<?> type) {
        return TRICK_TYPES.inverse().get(type);
    }

    /**
     * Adds a trick.
     *
     * @param trick the trick to add.
     */
    public static void addTrick(final Trick trick) {
        final var cfg = TheCommander.getInstance().getGeneralConfig().features().tricks();
        if (!cfg.tricksEnabled()) return;
        getTricks().add(trick);
        write();
        if (cfg.prefixEnabled()) {
            TheCommander.getInstance().getCommandClient().addCommand(new RunTrickCommand.Prefix(trick));
        }
    }

    /**
     * Removes a trick.
     *
     * @param trick the trick
     */
    public static void removeTrick(final Trick trick) {
        final var cfg = TheCommander.getInstance().getGeneralConfig();
        if (!cfg.features().tricks().tricksEnabled()) return;
        getTricks().remove(trick);
        write();
        if (cfg.features().tricks().prefixEnabled()) {
            TheCommander.getInstance().getCommandClient().removeCommand(trick.getNames().get(0));
        }
    }

    public static void replaceTrick(final Trick oldTrick, final Trick newTrick) {
        getTricks().remove(oldTrick);
        getTricks().add(newTrick);
        write();
    }

    /**
     * Write tricks to disk.
     */
    private static void write() {
        final var tricksFile = TRICK_STORAGE_PATH.get().toFile();
        final var tricks = getTricks();
        final var db = VersionedDatabase.inMemory(CURRENT_SCHEMA_VERSION, tricks);
        try (var writer = new OutputStreamWriter(new FileOutputStream(tricksFile), StandardCharsets.UTF_8)) {
            final var result = db.toJson(CODEC);
            Constants.Gsons.NO_PRETTY_PRINTING.toJson(result.result()
                    .orElseThrow(
                        () -> new IOException(result.error()
                            .orElseThrow() // throw if the message doesn't exist... that would be weird
                            .message())
                    ),
                writer);
        } catch (final FileNotFoundException exception) {
            TheCommander.LOGGER.error("A FileNotFoundException occurred saving tricks...", exception);
        } catch (final IOException exception) {
            TheCommander.LOGGER.error("An IOException occurred saving tricks...", exception);
        }
    }

    static {
        Tricks.registerTrickType("string", StringTrick.TYPE);
        Tricks.registerTrickType("embed", EmbedTrick.TYPE);
        Tricks.registerTrickType("script", ScriptTrick.TYPE);
    }
}
