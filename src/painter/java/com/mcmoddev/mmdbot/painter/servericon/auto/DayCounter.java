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

import com.google.gson.reflect.TypeToken;
import com.mcmoddev.mmdbot.painter.ThePainter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Guild;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public record DayCounter(Map<String, Data> days) {
    public Data getCurrentDay(Guild guild) {
        return days.getOrDefault(guild.getId(), new Data(0, false));
    }

    public void setDay(Guild guild, int day, boolean backwards) {
        days.put(guild.getId(), new Data(day, backwards));
    }

    @SneakyThrows
    public static DayCounter read() {
        Path path = ThePainter.getInstance().getRunPath().resolve("autoicons/days.json");
        if (!Files.exists(path)) return new DayCounter(new HashMap<>());
        try (final var reader = Files.newBufferedReader(path)) {
            return new DayCounter(new HashMap<>(AutomaticIconConfiguration.GSON.fromJson(reader, new TypeToken<Map<String, Data>>() {})));
        }
    }

    @SneakyThrows
    public void write() {
        Path path = ThePainter.getInstance().getRunPath().resolve("autoicons/days.json");
        Files.createDirectories(path.getParent());
        try (final var writer = Files.newBufferedWriter(path)) {
            AutomaticIconConfiguration.GSON.toJson(days(), writer);
        }
    }

    public record Data(int day, boolean backwards) {}
}
