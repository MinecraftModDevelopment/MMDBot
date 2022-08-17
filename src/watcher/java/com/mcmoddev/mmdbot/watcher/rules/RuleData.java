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
package com.mcmoddev.mmdbot.watcher.rules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.matyrobbrt.curseforgeapi.util.gson.RecordTypeAdapterFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.util.Random;

public record RuleData(String title, String description, int colour)  {
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
        .create();
    public static RuleData from(String data) {
        return GSON.fromJson(data, RuleData.class);
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    private static final float MIN_BRIGHTNESS = 0.8f;
    private static final Random RANDOM = new Random();

    public RuleData(String title, String description) {
        this(title, description, createRandomBrightColor().getRGB());
    }

    public EmbedBuilder asEmbed(int index) {
        return new EmbedBuilder()
            .setTitle(index + ". " + title())
            .setDescription(description())
            .setColor(colour);
    }

    public static Color createRandomBrightColor() {
        float h = RANDOM.nextFloat();
        float s = RANDOM.nextFloat();
        float b = MIN_BRIGHTNESS + ((1f - MIN_BRIGHTNESS) * RANDOM.nextFloat());
        return Color.getHSBColor(h, s, b);
    }

}
