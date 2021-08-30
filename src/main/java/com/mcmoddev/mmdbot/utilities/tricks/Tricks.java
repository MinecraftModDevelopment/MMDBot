package com.mcmoddev.mmdbot.utilities.tricks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.jagrosh.jdautilities.command.Command;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.modules.commands.CommandModule;
import com.mcmoddev.mmdbot.modules.commands.server.tricks.CmdRunTrick;

import javax.annotation.Nullable;
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
import java.util.stream.Collectors;

//TODO: Migrate to a SQLite DB with PR #45

/**
 * The type Tricks.
 *
 * @author williambl
 */
public final class Tricks {

    /**
     * The storage location for the tricks file.
     */
    private static final String TRICK_STORAGE_PATH = "mmdbot_tricks.json";

    /**
     * The constant GSON.
     */
    private static final Gson GSON;

    /**
     * The constant trickTypes.
     */
    private static final Map<String, Trick.TrickType<?>> TRICK_TYPES = new HashMap<>();

    /**
     * The Tricks.
     */
    private static @Nullable
    List<Trick> tricks = null;

    /**
     * Gets trick.
     *
     * @param name the name
     * @return the trick
     */
    public static Optional<Trick> getTrick(final String name) {
        return getTricks().stream().filter(trick -> trick.getNames().contains(name)).findAny();
    }

    /**
     * Create trick commands list.
     *
     * @return the list
     */
    public static List<CmdRunTrick> createTrickCommands() {
        return getTricks().stream().map(CmdRunTrick::new).collect(Collectors.toList());
    }

    /**
     * Gets tricks.
     *
     * @return the tricks
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
     * Register trick type.
     *
     * @param name the name
     * @param type the type
     */
    public static void registerTrickType(final String name, final Trick.TrickType<?> type) {
        TRICK_TYPES.put(name, type);
    }

    /**
     * Gets trick types.
     *
     * @return the trick types
     */
    public static Map<String, Trick.TrickType<?>> getTrickTypes() {
        return new HashMap<>(TRICK_TYPES);
    }

    /**
     * Gets trick type.
     *
     * @param name the name
     * @return the trick type
     */
    public static Trick.TrickType<?> getTrickType(final String name) {
        return TRICK_TYPES.get(name);
    }

    /**
     * Add a trick.
     *
     * @param trick the trick to add.
     *
     * @throws IllegalArgumentException propogated from
     * {@link com.jagrosh.jdautilities.command.CommandClient#addCommand(Command)} if a command with the name or alias
     * of the given trick already exists.
     */
    public static void addTrick(final Trick trick) {
        CommandModule.getCommandClient().addCommand(new CmdRunTrick(trick));
        getTricks().add(trick);
        write();
    }

    /**
     * Remove trick.
     *
     * @param trick the trick
     */
    public static void removeTrick(final Trick trick) {
        getTricks().remove(trick);
        CommandModule.getCommandClient().removeCommand(trick.getNames().get(0));
        write();
    }

    /**
     * Write.
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

    static {
        Tricks.registerTrickType("string", new StringTrick.Type());
        Tricks.registerTrickType("embed", new EmbedTrick.Type());

        GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new TrickSerializer())
            .create();
    }

    /**
     * The type Trick serializer.
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
            return new TypeAdapter<T>() {
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
