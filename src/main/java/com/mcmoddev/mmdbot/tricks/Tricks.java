package com.mcmoddev.mmdbot.tricks;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.commands.tricks.CmdRunTrick;

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
public final class Tricks {
    private static final String TRICK_STORAGE_PATH = "mmdbot_tricks.json";
    private static final Gson GSON;
    private static final Map<String, Trick.TrickType<?>> trickTypes = new HashMap<>();

    private static @Nullable List<Trick> tricks = null;

    public static Optional<Trick> getTrick(String name) {
        return getTricks().stream().filter(trick -> trick.getNames().contains(name)).findAny();
    }

    public static List<CmdRunTrick> createTrickCommands() {
        return getTricks().stream().map(CmdRunTrick::new).collect(Collectors.toList());
    }

    public static List<Trick> getTricks() {
        if (tricks == null) {
            final File file = new File(TRICK_STORAGE_PATH);
            if (!file.exists())
                tricks = new ArrayList<>();
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

    public static void registerTrickType(String name, Trick.TrickType<?> type) {
        trickTypes.put(name, type);
    }

    public static Map<String, Trick.TrickType<?>> getTrickTypes() {
        return new HashMap<>(trickTypes);
    }

    public static Trick.TrickType<?> getTrickType(String name) {
        return trickTypes.get(name);
    }

    public static void registerTrick(Trick trick) {
        getTricks().add(trick);
        MMDBot.getCommandClient().addCommand(new CmdRunTrick(trick));
    }

    private static void write() {
        final File userJoinTimesFile = new File(TRICK_STORAGE_PATH);
        List<Trick> tricks = getTricks();
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(userJoinTimesFile), StandardCharsets.UTF_8)) {
            GSON.toJson(tricks, writer);
        } catch (final FileNotFoundException exception) {
            MMDBot.LOGGER.error("An FileNotFoundException occurred saving tricks...", exception);
        } catch (final IOException exception) {
            MMDBot.LOGGER.error("An IOException occurred saving tricks...", exception);
        }
    }

    static {
        GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new TrickSerializer())
            .create();
    }

    final static class TrickSerializer implements TypeAdapterFactory {
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
                        T result = gson.getDelegateAdapter(TrickSerializer.this, readType).read(in);
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
