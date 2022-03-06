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
package com.mcmoddev.mmdbot.thelistener.events;

import com.mcmoddev.mmdbot.core.util.Pair;
import com.mcmoddev.mmdbot.thelistener.util.ListenerAdapter;
import com.mcmoddev.mmdbot.thelistener.util.Utils;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

public final class ReferencingListener extends ListenerAdapter {

    private static final AllowedMentions ALLOWED_MENTIONS = AllowedMentions.builder().repliedUser(false).build();

    @Override
    public void onMessageCreate(final MessageCreateEvent event) {
        if (event.getGuildId().isEmpty()) {
            return; // Not from guild
        }
        final var originalMsg = event.getMessage();
        if (isStringReference(originalMsg.getContent())) {
            Pair.makeOptional(originalMsg.getReferencedMessage(), event.getMember())
                .ifPresent(pair -> {
                    final var referenceMessage = pair.first();
                    event.getMessage().getChannel().subscribe(channel -> channel.createMessage(reference(referenceMessage, pair.second()))
                        .withAllowedMentions(ALLOWED_MENTIONS).subscribe());
                    originalMsg.delete("Quote successful").subscribe();
                });
        }

        final String[] msg = originalMsg.getContent().split(" ");
        if (msg.length < 1) {
            return;
        }

        final var matcher = Utils.MESSAGE_LINK_PATTERN.matcher(msg[0]);
        if (matcher.find()) {
            Mono.zip(event.getGuild(), Mono.justOrEmpty(event.getMember())).subscribe(pair -> {
                try {
                    final var message = Utils.getMessageByLink(msg[0], pair.getT1());
                    if (message != null) {
                        event.getMessage().getChannel().subscribe(channel -> channel.createMessage(reference(message, pair.getT2()))
                            .withAllowedMentions(ALLOWED_MENTIONS).subscribe());
                        if (msg.length == 1) {
                            originalMsg.delete("Quote successful").subscribe();
                        }
                    }
                } catch (Utils.MessageLinkException ignored) {

                }
            });
        }
    }

    public final static String ZERO_WIDTH_SPACE = "\u200E";

    public static boolean isStringReference(final String string) {
        // The zero-width space
        return string.equalsIgnoreCase(".") || string.equalsIgnoreCase(ZERO_WIDTH_SPACE);
    }

    public static EmbedCreateSpec reference(final Message message, final Member quoter) {
        final var msgLink = Utils.createMessageURL(message);
        final var embed = EmbedCreateSpec.builder().timestamp(message.getTimestamp())
            .color(Color.DARK_GRAY);
        final var description = new StringBuilder();
        message.getAuthor().ifPresent(user -> {
            embed.author(user.getTag(), msgLink, user.getAvatarUrl());
        });
        if (!message.getContent().isBlank()) {
            description.append("[%s](%s)".formatted("Reference ➤ ", msgLink))
                .append(message.getContent());
        } else {
            description.append("[%s](%s)".formatted("Jump to referenced message.", msgLink));
        }
        if (quoter.getId().asLong() != message.getAuthor().map(u -> u.getId().asLong()).orElse(0L)) {
            embed.footer("%s#%s".formatted(quoter.getUsername(), quoter.getDiscriminator()) + " referenced", quoter.getAvatarUrl());
        }
        if (!message.getAttachments().isEmpty()) {
            embed.image(message.getAttachments().get(0).getUrl());
        }
        embed.description(description.toString());
        return embed.build();
    }
}
