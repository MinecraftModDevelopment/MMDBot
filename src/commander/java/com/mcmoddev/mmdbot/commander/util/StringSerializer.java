package com.mcmoddev.mmdbot.commander.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.matyrobbrt.curseforgeapi.util.gson.RecordTypeAdapterFactory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface StringSerializer<T> {
    Gson RECORD_GSON = new GsonBuilder().registerTypeAdapterFactory(new RecordTypeAdapterFactory()).create();
    StringSerializer<String> SELF = new StringSerializer<>() {
        @NotNull
        @Override
        public String serialize(final String input) {
            return input;
        }

        @NotNull
        @Override
        public String deserialize(final String input) {
            return input;
        }
    };

    @Nonnull
    String serialize(T input);

    @Nonnull
    T deserialize(String input);

    static <T> StringSerializer<T> json(Gson gson, Class<T> type) {
        return new StringSerializer<>() {
            @NotNull
            @Override
            public String serialize(final T input) {
                return gson.toJson(input, type);
            }

            @NotNull
            @Override
            public T deserialize(final String input) {
                return gson.fromJson(input, type);
            }
        };
    }
}
