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
package com.mcmoddev.mmdbot.utilities.updatenotifiers.minecraft;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.console.MMDMarkers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;

import java.awt.Color;
import java.time.Instant;

/**
 * The type Minecraft update notifier.
 *
 * @author
 */
public final class MinecraftUpdateNotifier implements Runnable {

    /**
     * The Last latest.
     */
    private String lastLatest;

    /**
     * The Last latest stable.
     */
    private String lastLatestStable;

    /**
     * Instantiates a new Minecraft update notifier.
     */
    public MinecraftUpdateNotifier() {
        lastLatest = MinecraftVersionHelper.getLatest();
        lastLatestStable = MinecraftVersionHelper.getLatestStable();
    }

    /**
     * Run.
     */
    @Override
    public void run() {
        LOGGER.debug(MMDMarkers.NOTIFIER_MC, "Checking for new Minecraft versions...");
        MinecraftVersionHelper.update();
        final String latest = MinecraftVersionHelper.getLatest();
        final String latestStable = MinecraftVersionHelper.getLatestStable();
        final long channelId = getConfig().getChannel("notifications.minecraft");

        if (!lastLatestStable.equals(latestStable)) {
            LOGGER.info(MMDMarkers.NOTIFIER_MC, "New Minecraft release found, from {} to {}", lastLatest, latest);

            Utils.getChannelIfPresent(channelId, channel -> {
                final var embed = new EmbedBuilder();
                embed.setTitle("New Minecraft release available!");
                embed.setDescription(latest);
                embed.setColor(Color.GREEN);
                embed.setTimestamp(Instant.now());
                channel.sendMessageEmbeds(embed.build()).queue(msg -> {
                    if (channel.getType() == ChannelType.NEWS) {
                        msg.crosspost().queue();
                    }
                });
            });
        } else if (!lastLatest.equals(latest)) {
            LOGGER.info(MMDMarkers.NOTIFIER_MC, "New Minecraft snapshot found, from {} to {}", lastLatest, latest);

            Utils.getChannelIfPresent(channelId, channel -> {
                final var embed = new EmbedBuilder();
                embed.setTitle("New Minecraft snapshot available!");
                embed.setDescription(latest);
                embed.setColor(Color.ORANGE);
                embed.setTimestamp(Instant.now());
                channel.sendMessageEmbeds(embed.build()).queue(msg -> {
                    if (channel.getType() == ChannelType.NEWS) {
                        msg.crosspost().queue();
                    }
                });
            });
        } else {
            LOGGER.debug(MMDMarkers.NOTIFIER_MC, "No new Minecraft version found");
        }

        lastLatest = latest;
        lastLatestStable = latestStable;
    }
}
