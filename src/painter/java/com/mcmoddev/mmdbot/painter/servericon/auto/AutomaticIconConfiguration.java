/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.painter.servericon.auto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mcmoddev.mmdbot.painter.ThePainter;
import com.mcmoddev.mmdbot.painter.servericon.IconConfiguration;
import com.mcmoddev.mmdbot.painter.servericon.ServerIconMaker;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public record AutomaticIconConfiguration(List<Integer> colours, long logChannelId, boolean isRing, boolean enabled) {
    static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping().create();

    public BufferedImage createImage(int day) throws IOException {
        return ServerIconMaker.createIcon(IconConfiguration.builder()
            .setCircular(isRing).setHasRing(isRing)
            .setColour(colours.get(day - 1))
            .build());
    }

    @Nullable
    public static AutomaticIconConfiguration get(String guildId) throws IOException {
        final var path = ThePainter.getInstance().getRunPath().resolve("autoicons").resolve(guildId + ".json");
        if (Files.exists(path)) {
            try (final var reader = Files.newBufferedReader(path)) {
                return GSON.fromJson(reader, AutomaticIconConfiguration.class);
            }
        }
        return null;
    }

    public void save(String guildId) throws IOException {
        final var path = ThePainter.getInstance().getRunPath().resolve("autoicons").resolve(guildId + ".json");
        try (final var writer = Files.newBufferedWriter(path)) {
            GSON.toJson(this, writer);
        }
    }
}
