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
