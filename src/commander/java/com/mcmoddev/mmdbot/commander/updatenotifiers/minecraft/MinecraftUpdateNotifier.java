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
package com.mcmoddev.mmdbot.commander.updatenotifiers.minecraft;

import com.mcmoddev.mmdbot.commander.config.Configuration;
import com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifier;
import com.mcmoddev.mmdbot.commander.util.StringSerializer;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.awt.Color;

/**
 * The Minecraft update notifier.
 *
 * @author unknown
 * @author matyrobbrt
 */
public final class MinecraftUpdateNotifier extends UpdateNotifier<MinecraftUpdateNotifier.McVersion> {

    public MinecraftUpdateNotifier() {
        super(NotifierConfiguration.<McVersion>builder()
            .name("minecraft")
            .channelGetter(Configuration.Channels.UpdateNotifiers::minecraft)
            .versionComparator(NotifierConfiguration.notEqual())
            .serializer(new StringSerializer<>() {
                @NotNull
                @Override
                public String serialize(final @NotNull McVersion input) {
                    return serializeStr(input.latest()) + ";" + serializeStr(input.stable());
                }

                @NotNull
                @Override
                public McVersion deserialize(final @NotNull String input) {
                    final var split = input.split(";");
                    return new McVersion(deserializeStr(split[0]), deserializeStr(split[1]));
                }

                private String serializeStr(String str) {
                    if (str == null) return "";
                    return str;
                }

                private String deserializeStr(String str) {
                    if (str.isEmpty()) return null;
                    return str;
                }
            })
            .build());
    }

    @Override
    protected McVersion queryLatest() {
        return new McVersion(
            MinecraftVersionHelper.getLatest(),
            MinecraftVersionHelper.getLatestStable()
        );
    }

    @NotNull
    @Override
    protected EmbedBuilder getEmbed(@Nullable final McVersion oldVersion, final @NotNull McVersion newVersion) {
        if (oldVersion == null) {
            return new EmbedBuilder()
                .setDescription("New Minecraft version available!")
                .setColor(Color.CYAN)
                .setDescription(newVersion.latest);
        }
        final var embed = new EmbedBuilder();
        if (!oldVersion.stable().equals(newVersion.stable())) {
            embed.setTitle("New Minecraft release available!");
            embed.setDescription(newVersion.stable());
            embed.setColor(Color.GREEN);
        } else {
            embed.setTitle("New Minecraft snapshot available!");
            embed.setDescription(newVersion.latest());
            embed.setColor(Color.ORANGE);
        }
        return embed;
    }

    public record McVersion(String latest, String stable) {
    }
}
