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
package com.mcmoddev.mmdbot.utilities.updatenotifiers.fabric;

import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;

import java.awt.Color;
import java.time.Instant;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;
import static com.mcmoddev.mmdbot.utilities.console.MMDMarkers.NOTIFIER_FABRIC;

/**
 * The type Fabric api update notifier.
 *
 * @author williambl
 */
public final class FabricApiUpdateNotifier implements Runnable {

    /**
     * The Last latest.
     */
    private String lastLatest;

    /**
     * Instantiates a new Fabric api update notifier.
     */
    public FabricApiUpdateNotifier() {
        lastLatest = FabricVersionHelper.getLatestApi();
    }

    /**
     * Run.
     */
    @Override
    public void run() {
        LOGGER.debug(NOTIFIER_FABRIC, "Checking for new Fabric API versions...");
        final String latest = FabricVersionHelper.getLatestApi();

        final long channelId = getConfig().getChannel("notifications.fabric");

        if (!lastLatest.equals(latest)) {
            LOGGER.info(NOTIFIER_FABRIC, "New Fabric API release found, from {} to {}", lastLatest, latest);
            lastLatest = latest;

            Utils.getChannelIfPresent(channelId, channel -> {
                final var embed = new EmbedBuilder();
                embed.setTitle("New Fabric API release available!");
                embed.setDescription(latest);
                embed.setColor(Color.WHITE);
                embed.setTimestamp(Instant.now());
                channel.sendMessageEmbeds(embed.build()).queue(msg -> {
                    if (channel.getType() == ChannelType.NEWS) {
                        msg.crosspost().queue();
                    }
                });
            });
        } else {
            LOGGER.debug(NOTIFIER_FABRIC, "No new Fabric API version found");
        }

        lastLatest = latest;
    }
}
