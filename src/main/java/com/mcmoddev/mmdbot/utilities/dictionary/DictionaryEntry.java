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
package com.mcmoddev.mmdbot.utilities.dictionary;

import com.google.gson.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public record DictionaryEntry(@Nonnull String word, @Nullable String pronunciation,
                              @Nonnull List<DictionaryDefinition> definitions) {

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
