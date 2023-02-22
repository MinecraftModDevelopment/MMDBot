/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2023 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
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
package com.mcmoddev.mmdbot.core.util.jda;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EmbedParser {
    public static final Map<String, EmbedValue> TYPES = new HashMap<>();

    static {
        TYPES.put("title", EmbedBuilder::setTitle);
        TYPES.put("description", EmbedBuilder::setDescription);
        TYPES.put("color", (builder, in) -> builder.setColor(Color.decode(in)));
        TYPES.put("image", EmbedBuilder::setImage);
        TYPES.put("thumbnail", EmbedBuilder::setThumbnail);
    }

    public static EmbedBuilder parse(String data) {
        final var lines = List.of(data.split("\n"));
        final Map<String, String> dataById = new HashMap<>();
        String currentType = "";
        StringBuilder currentData = new StringBuilder();
        for (final var line : lines) {
            final var idx = line.indexOf(':');
            if (idx >= 0) {
                final var type = line.substring(0, idx).trim().toLowerCase(Locale.ROOT);
                if (TYPES.containsKey(type)) {
                    if (!currentType.isEmpty()) {
                        dataById.put(currentType, currentData.toString());
                    }
                    currentType = type;
                    currentData = new StringBuilder().append(line.substring(idx + 1).trim());
                    continue;
                }
            }
            if (!currentData.isEmpty()) currentData.append('\n');
            currentData.append(line);
        }
        if (!currentType.isEmpty())
            dataById.put(currentType, currentData.toString());
        final var builder = new EmbedBuilder();
        dataById.forEach((type, value) -> TYPES.get(type).append(builder, value));
        return builder;
    }

    interface EmbedValue {
        void append(EmbedBuilder embedBuilder, String inData);
    }
}
