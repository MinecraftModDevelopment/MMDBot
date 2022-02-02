package com.mcmoddev.mmdbot.utilities.dictionary;

import com.google.gson.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public record DictionaryEntry(@Nonnull String word, @Nullable String pronunciation, @Nonnull List<DictionaryDefinition> definitions) {

    public static DictionaryEntry fromJson(@Nonnull final JsonObject json) {
        final var word = json.get("word").getAsString();
        final var pronunciation = DictionaryDefinition.getFromJson(json, "pronunciation");
        final List<DictionaryDefinition> definitions = new ArrayList<>();
        json.get("definitions").getAsJsonArray().forEach(elem -> {
            if (elem.isJsonObject()) {
                definitions.add(DictionaryDefinition.fromJson(elem.getAsJsonObject()));
            }
        });
        return new DictionaryEntry(word, pronunciation, definitions);
    }

}
