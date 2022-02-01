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
package com.mcmoddev.mmdbot.utilities.tricks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.modules.commands.CommandModule;
import com.mcmoddev.mmdbot.modules.commands.server.DeletableCommand;
import com.mcmoddev.mmdbot.modules.commands.server.tricks.CmdRunTrickSeparated;
import com.mcmoddev.mmdbot.utilities.tricks.Trick.TrickType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//TODO: Migrate to a SQLite DB with PR #45

/**
 * The Tricks Module.
 *
 * @author Will BL
 */
public final class Tricks {

    /**
     * The storage location for the tricks file.
     */
    private static final String TRICK_STORAGE_PATH = "mmdbot_tricks.json";

    /**
     * The GSON instance.
     */
    private static final Gson GSON;

    /**
     * All registered {@link TrickType}s.
     */
    private static final Map<String, Trick.TrickType<?>> TRICK_TYPES = new HashMap<>();

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
     * Gets all tricks.
     *
     * @return a list of the tricks
     */
    public static List<Trick> getTricks() {
        if (tricks == null) {
            final File file = new File(TRICK_STORAGE_PATH);
            if (!file.exists()) {
                tricks = new ArrayList<>();
            }
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                Type typeOfList = new TypeToken<List<Trick>>() {
                }.getType();
                tricks = GSON.fromJson(reader, typeOfList);
            } catch (final IOException exception) {
                MMDBot.LOGGER.trace("Failed to read tricks file...", exception);
                tricks = new ArrayList<>();
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
    public static Map<String, Trick.TrickType<?>> getTrickTypes() {
        return new HashMap<>(TRICK_TYPES);
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
     * Adds a trick.
     *
     * @param trick the trick to add.
     */
    public static void addTrick(final Trick trick) {
        getTricks().add(trick);
        write();
        // addOrRestoreCommand(trick);
    }

    /**
     * Removes a trick.
     *
     * @param trick the trick
     */
    public static void removeTrick(final Trick trick) {
        getTricks().remove(trick);
        write();
        // CommandModule.removeCommand(trick.getNames().get(0), true);
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
        final var tricksFile = new File(TRICK_STORAGE_PATH);
        List<Trick> tricks = getTricks();
        try (var writer = new OutputStreamWriter(new FileOutputStream(tricksFile), StandardCharsets.UTF_8)) {
            GSON.toJson(tricks, writer);
        } catch (final FileNotFoundException exception) {
            MMDBot.LOGGER.error("An FileNotFoundException occurred saving tricks...", exception);
        } catch (final IOException exception) {
            MMDBot.LOGGER.error("An IOException occurred saving tricks...", exception);
        }
    }

    /**
     * Adds or restores the command for a trick.
     *
     * @param trick the trick
     */
    private static void addOrRestoreCommand(final Trick trick) {
        CommandModule.getCommandClient().getSlashCommands().stream()
            .filter(cmd -> cmd.getName().equals(trick.getNames().get(0)))
            .filter(cmd -> cmd instanceof DeletableCommand)
            .map(cmd -> (DeletableCommand) cmd)
            .findFirst().ifPresentOrElse(
                DeletableCommand::restore,
                () -> {
                    final var cmd = new CmdRunTrickSeparated(trick);
                    CommandModule.getCommandClient().addSlashCommand(cmd);
                    CommandModule.upsertCommand(cmd);
                });
    }

    static {
        Tricks.registerTrickType("string", new StringTrick.Type());
        Tricks.registerTrickType("embed", new EmbedTrick.Type());

        GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new TrickSerializer())
            .create();
    }

    /**
     * Handles serializing tricks to JSON.
     */
    static final class TrickSerializer implements TypeAdapterFactory {
        /**
         * Create type adapter.
         *
         * @param <T>  the type parameter
         * @param gson the gson
         * @param type the type
         * @return the type adapter
         */
        @Override
        public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
            if (!Trick.class.isAssignableFrom(type.getRawType())) {
                return null;
            }
            final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
            return new TypeAdapter<>() {
                @Override
                public void write(final JsonWriter out, final T value) throws IOException {
                    out.beginObject();
                    out.name("$type");
                    out.value(type.toString());
                    out.name("value");
                    delegate.write(out, value);
                    out.endObject();
                }

                @Override
                public T read(final JsonReader in) throws IOException {
                    in.beginObject();
                    if (!"$type".equals(in.nextName())) {
                        return null;
                    }
                    try {
                        @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) Class.forName(in.nextString());
                        TypeToken<T> readType = TypeToken.get(clazz);
                        in.nextName();
                        var result = gson.getDelegateAdapter(TrickSerializer.this, readType).read(in);
                        in.endObject();
                        return result;
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }
    }
}
