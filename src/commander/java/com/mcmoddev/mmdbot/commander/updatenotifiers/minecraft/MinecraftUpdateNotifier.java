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
package com.mcmoddev.mmdbot.commander.updatenotifiers.minecraft;

import static com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifiers.LOGGER;
import static com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifiers.MARKER;
import com.mcmoddev.mmdbot.commander.TheCommander;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.Color;
import java.time.Instant;

/**
 * The type Minecraft update notifier.
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
        if (TheCommander.getInstance() == null) {
            LOGGER.warn(MARKER, "Cannot start Minecraft Update Notifier due to the bot instance being null.");
            return;
        }
        LOGGER.debug(MARKER, "Checking for new Minecraft versions...");
        MinecraftVersionHelper.update();
        final String latest = MinecraftVersionHelper.getLatest();
        final String latestStable = MinecraftVersionHelper.getLatestStable();

        if (!lastLatestStable.equals(latestStable)) {
            LOGGER.info(MARKER, "New Minecraft release found, from {} to {}", lastLatest, latest);

            TheCommander.getInstance().getGeneralConfig().channels().updateNotifiers().minecraft().forEach(chId -> {
                final var channel = TheCommander.getJDA().getChannelById(MessageChannel.class, chId);
                if (channel != null) {
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
                }
            });
        } else if (!lastLatest.equals(latest)) {
            LOGGER.info(MARKER, "New Minecraft snapshot found, from {} to {}", lastLatest, latest);

            TheCommander.getInstance().getGeneralConfig().channels().updateNotifiers().minecraft().forEach(chId -> {
                final var channel = TheCommander.getJDA().getChannelById(MessageChannel.class, chId);
                if (channel != null) {
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
                }
            });
        } else {
            LOGGER.debug(MARKER, "No new Minecraft version found");
        }

        lastLatest = latest;
        lastLatestStable = latestStable;
    }
}
