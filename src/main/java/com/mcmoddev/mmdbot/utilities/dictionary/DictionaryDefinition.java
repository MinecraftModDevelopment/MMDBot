package com.mcmoddev.mmdbot.utilities.dictionary;

import com.google.gson.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record DictionaryDefinition(@Nullable String type, @Nonnull String definition,
                                   @Nullable String example, @Nullable String image, @Nullable String emoji) {

    public static DictionaryDefinition fromJson(@Nonnull final JsonObject json) {
        final var type = getFromJson(json, "type");
        final var definition = getFromJson(json, "definition");
        final var example = getFromJson(json, "example");
        final var image = getFromJson(json, "image_url");
        final var emoji = getFromJson(json, "emoji");
        return new DictionaryDefinition(type, definition, example, image, emoji);
    }

    static String getFromJson(final JsonObject json, final String key) {
        return json.get(key).isJsonPrimitive() && json.get(key).getAsJsonPrimitive().isString() ? json.get(key).getAsString() : null;
    }

}
