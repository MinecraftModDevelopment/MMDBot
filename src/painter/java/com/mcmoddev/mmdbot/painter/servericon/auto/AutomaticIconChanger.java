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

import com.mcmoddev.mmdbot.core.util.Utils;
import com.mcmoddev.mmdbot.painter.ThePainter;
import com.mcmoddev.mmdbot.painter.util.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.RestAction;

@Slf4j
public class AutomaticIconChanger implements Runnable {

    @Override
    public void run() {
        if (ThePainter.getInstance() != null) {
            final var dayCounter = DayCounter.read();
            for (final var guild : ThePainter.getInstance().getJDA().getGuilds()) {
                try {
                    final var conf = AutomaticIconConfiguration.get(guild.getId());
                    if (conf != null && conf.enabled()) {
                        doRun(guild, conf, dayCounter);
                        dayCounter.write();
                    }
                } catch (Exception exception) {
                    ThePainter.LOGGER.error("Encountered exception cycling icon for guild {}: ", guild, exception);
                }
            }
        }
    }

    private void doRun(Guild guild, AutomaticIconConfiguration configuration, DayCounter dayCounter) throws Exception {
        final var current = dayCounter.getCurrentDay(guild);
        final int nextDay;
        final boolean backwards;
        if (current.backwards()) {
            if (current.day() <= 1) {
                // If the current day is 1, or lower, go to the start
                nextDay = 2;
                backwards = false;
            } else {
                // Else, go to the previous day
                nextDay = current.day() - 1;
                backwards = true;
            }
        } else {
            if (current.day() >= configuration.colours().size()) {
                // If the current day is greater, or equal to the amount of colours, start going back
                nextDay = configuration.colours().size() - 1;
                backwards = true;
            } else {
                // Else, go to the next day
                nextDay = current.day() + 1;
                backwards = false;
            }
        }

        final var targetColour = configuration.colours().get(nextDay - 1); // Indexes start at 0

        log.warn("Generating and changing server {} icon to colour {}.", guild, Utils.rgbToString(targetColour));

        final var logChannelId = guild.getJDA().getChannelById(MessageChannel.class, configuration.logChannelId());

        final var iconBytes = ImageUtils.toBytes(
            configuration.createImage(nextDay),
            "png"
        );

        //noinspection ConstantConditions
        guild.getManager()
            .setIcon(Icon.from(iconBytes))
            .flatMap(it -> logChannelId != null, it -> logChange(
                logChannelId, guild, targetColour, nextDay, backwards
            ))
            .queue();

        dayCounter.setDay(guild, nextDay, backwards);
    }

    private RestAction<?> logChange(MessageChannel channel, Guild guild, int newColour, int currentDay, boolean backwards) {
        return channel.sendMessageEmbeds(new EmbedBuilder()
                .setTitle("Server icon changed")
                .setDescription("The server's automatic icon advanced to day " + currentDay + (backwards ? " and is going backwards." : "."))
                .appendDescription("\nNew colour is `%s`".formatted(Utils.rgbToString(newColour)))
                .setColor(newColour)
                .setThumbnail(guild.getIconUrl())
            .build());
    }

}
