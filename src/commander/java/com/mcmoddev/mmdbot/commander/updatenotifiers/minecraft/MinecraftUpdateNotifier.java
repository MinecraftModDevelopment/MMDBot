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
package com.mcmoddev.mmdbot.commander.updatenotifiers.minecraft;

import com.mcmoddev.mmdbot.commander.config.Configuration;
import com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifier;
import com.mcmoddev.mmdbot.commander.updatenotifiers.minecraft.MinecraftVersionHelper.VersionsInfo;
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
public final class MinecraftUpdateNotifier extends UpdateNotifier<MinecraftVersionHelper.VersionsInfo> {

    public MinecraftUpdateNotifier() {
        super(NotifierConfiguration.<MinecraftVersionHelper.VersionsInfo>builder()
            .name("minecraft")
            .channelGetter(Configuration.Channels.UpdateNotifiers::minecraft)
            .versionComparator(NotifierConfiguration.notEqual())
            .serializer(StringSerializer.json(StringSerializer.RECORD_GSON, VersionsInfo.class))
            .webhookInfo(new WebhookInfo("Minecraft Updates", "https://media.discordapp.net/attachments/957353544493719632/1005934698767323156/unknown.png?width=594&height=594"))
            .build());
    }

    @Override
    protected VersionsInfo queryLatest() {
        final var meta = MinecraftVersionHelper.getMeta();
        if (meta == null) return null;
        return meta.latest;
    }

    @NotNull
    @Override
    protected EmbedBuilder getEmbed(@Nullable final VersionsInfo oldVersion, final @NotNull VersionsInfo newVersion) {
        if (oldVersion == null) {
            return new EmbedBuilder()
                .setDescription("New Minecraft version available!")
                .setColor(Color.CYAN)
                .setDescription(newVersion.snapshot());
        }
        final var embed = new EmbedBuilder();
        if (!oldVersion.release().equals(newVersion.release())) {
            // https://www.minecraft.net/en-us/article/minecraft-java-edition-1-18-1
            embed.setTitle("New Minecraft release available!");
            embed.setDescription(newVersion.release() + "\nChangelog: " + "https://www.minecraft.net/en-us/article/minecraft-java-edition-%s".formatted(newVersion.release().replace('.', '-')));
            embed.setColor(Color.GREEN);
        } else {
            if (newVersion.snapshot().contains("-rc")) {
                // https://www.minecraft.net/en-us/article/minecraft-1-19-4-release-candidate-1
                embed.setTitle("New Minecraft Release Candidate available!");
                final String[] split = newVersion.snapshot().split("-");
                embed.setDescription(newVersion.snapshot() + "\nChangelog: "
                    + "https://www.minecraft.net/en-us/article/minecraft-%s-release-candidate-%s"
                    .formatted(split[0].replace('.', '-'), split[1].substring(2)));
                embed.setColor(Color.PINK);
            } else if (newVersion.snapshot().contains("-pre")) {
                // https://www.minecraft.net/en-us/article/minecraft-1-19-4-pre-release-2
                embed.setTitle("New Minecraft Pre-Release available!");
                final String[] split = newVersion.snapshot().split("-");
                embed.setDescription(newVersion.snapshot() + "\nChangelog: "
                    + "https://www.minecraft.net/en-us/article/minecraft-%s-pre-release-%s"
                    .formatted(split[0].replace('.', '-'), split[1].substring(3)));
                embed.setColor(Color.ORANGE);
            } else {
                // https://www.minecraft.net/en-us/article/minecraft-snapshot-23w07a
                embed.setTitle("New Minecraft snapshot available!");
                embed.setDescription(newVersion.snapshot() + "\nChangelog: " + "https://www.minecraft.net/en-us/article/minecraft-snapshot-%s".formatted(newVersion.snapshot()));
                embed.setColor(Color.CYAN);
            }
        }
        return embed;
    }
}
