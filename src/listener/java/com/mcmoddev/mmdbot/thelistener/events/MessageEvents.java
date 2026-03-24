/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2026 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.thelistener.events;

import com.mcmoddev.mmdbot.core.event.moderation.ScamLinkEvent;
import com.mcmoddev.mmdbot.thelistener.TheListener;
import io.github.matyrobbrt.eventdispatcher.SubscribeEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.Instant;

public final class MessageEvents extends ListenerAdapter {

    public static final MessageEvents INSTANCE = new MessageEvents();

    private MessageEvents() {
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onScamLink(final ScamLinkEvent event) {
        final var embed = new EmbedBuilder()
            .setTitle("Scam link detected!")
            .setDescription(String.format("User <@%s> sent a scam link in <#%s>%s. Their message was deleted, and they were muted.",
                event.getTargetId(), event.getChannelId(), event.isMessageEdited() ? ", by editing an old message" : ""))
            .addField("Message Content", """
                ```
                %s
                ```""".formatted(event.getMessageContent()), false)
            .setColor(0xFF0000)
            .setTimestamp(Instant.now())
            .setFooter("User ID: " + event.getTargetId(), event.getTargetAvatar())
            .setThumbnail(event.getTargetAvatar())
            .build();

        if (TheListener.getInstance() != null) {
            final var jda = TheListener.getInstance().getJDA();
            TheListener.getInstance().getConfigForGuild(event.getGuildId()).getScamLoggingChannels()
                .forEach(snowflakeValue -> {
                    final var ch = snowflakeValue.resolve(id -> jda.getChannelById(MessageChannel.class, id));
                    if (ch != null) {
                        ch.sendMessageEmbeds(embed).queue();
                    }
                });
        }
    }
}
