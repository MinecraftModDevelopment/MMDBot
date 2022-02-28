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
import com.mcmoddev.mmdbot.thelistener.TheListener;
import com.mcmoddev.mmdbot.thelistener.util.ListenerAdapter;
import com.mcmoddev.mmdbot.thelistener.util.LoggingType;
import com.mcmoddev.mmdbot.thelistener.util.Utils;
import discord4j.core.event.domain.message.MessageBulkDeleteEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.AllowedMentionsData;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.rest.util.Color;

import java.time.Instant;

public final class MessageEvents extends ListenerAdapter {

    public static final AllowedMentionsData ALLOWED_MENTIONS_DATA = AllowedMentionsData.builder().repliedUser(false).build();

    @Override
    public void onMessageDelete(final MessageDeleteEvent event) {
        if (event.getGuildId().isEmpty()) {
            return; // Not from guild
        }
        if (event.getMessage().isPresent() && ReferencingListener.isStringReference(event.getMessage().get().getContent())) {
            return; // Don't log referencing
        }
        if (LoggingType.MESSAGE_EVENTS.getChannels(event.getGuildId().get()).contains(event.getChannelId())) {
            return; // Don't log deletions in a logging channel
        }
        final var embed = event.getMessage()
            .flatMap(message -> Pair.of(message, message.getAuthor().orElse(null)).toOptional())
            .map(c -> c.map((message, user) -> {
                final var embedBuilder = EmbedCreateSpec.builder();
                embedBuilder.color(Color.GRAY_CHATEAU)
                    .description("""
                        **A message sent by <@%s> in <#%s> has been deleted!**
                        %s"""
                        .formatted(user.getId().asLong(), message.getChannelId().asLong(), message.getContent()));
                embedBuilder.author(user.getUsername(), null, user.getAvatarUrl());
                embedBuilder.timestamp(Instant.now())
                    .footer("Author: %s | Message ID: %s".formatted(user.getId().asLong(), message.getId().asLong()), null);
                return embedBuilder.build();
            })).orElseGet(() -> EmbedCreateSpec.builder().description("A message sent in <#%s> has been deleted! No more information could be retrieved."
                    .formatted(event.getChannelId().asLong())).timestamp(Instant.now())
                .color(Color.CINNABAR).build());
        Utils.executeInLoggingChannel(event.getGuildId().get(), LoggingType.MESSAGE_EVENTS,
            channel -> channel.createMessage(MessageCreateRequest.builder()
                .embed(embed.asRequest())
                .allowedMentions(ALLOWED_MENTIONS_DATA).build()).subscribe());
    }

    @Override
    public void onMessageUpdate(final MessageUpdateEvent event) {
        if (event.getGuildId().isEmpty() /* Not from guild */ || !event.isContentChanged()) {
            return;
        }
        if (LoggingType.MESSAGE_EVENTS.getChannels(event.getGuildId().get()).contains(event.getChannelId())) {
            return; // Don't log deletions in a logging channel
        }
        event.getMessage().subscribe(newMessage -> {
            if (newMessage.getAuthor()
                .map(u -> u.getId().asLong() == event.getClient().getSelfId().asLong())
                .orElse(false)) {
                return; // The bot edited the message, so it can be ignored
            }
            // The horrible mapping takes in an optional of the old message, and another
            // optional of the message author. The optionals are merged into a
            // pair optional, which is empty if either the message optional or the author optional
            // are empty
            final var embed = Pair.of(event.getOld(), newMessage.getAuthor())
                .map(Pair::makeOptional)
                .map(c -> c.map((oldMessage, author) -> {
                    final var embedBuilder = EmbedCreateSpec.builder();
                    embedBuilder.color(Color.VIVID_VIOLET)
                        .description("**A message sent by <@%s> in <#%s> has been edited!** [Jump to message.](%s)"
                            .formatted(author.getId().asLong(), event.getChannelId().asLong(), Utils.createMessageURL(newMessage)));
                    embedBuilder.timestamp(Instant.now());
                    embedBuilder.addField("Before", oldMessage.getContent(), false)
                        .addField("After", newMessage.getContent(), false);
                    embedBuilder.author(author.getUsername(), null, author.getAvatarUrl())
                        .footer("Author ID: " + author.getId().asLong(), null);
                    return embedBuilder.build();
                })).orElseGet(() -> EmbedCreateSpec.builder().description("A message sent in <#%s> has been edited! Old content information could not be retrieved."
                        .formatted(event.getChannelId().asLong()))
                    .timestamp(Instant.now()).addField("After", newMessage.getContent(), false)
                    .color(Color.ENDEAVOUR).build());

            Utils.executeInLoggingChannel(event.getGuildId().get(), LoggingType.MESSAGE_EVENTS,
                channel -> channel.createMessage(MessageCreateRequest.builder()
                    .embed(embed.asRequest())
                    .allowedMentions(ALLOWED_MENTIONS_DATA).build()).subscribe());
        }, e -> TheListener.LOGGER.error("Error while trying to log a message edit!", e));
    }

    @Override
    public void onMessageBulkDelete(final MessageBulkDeleteEvent event) {
        final var embed = EmbedCreateSpec.builder()
            .description("%s messages have been bulk deleted in <#%s>!"
                .formatted(event.getMessages().size(), event.getChannelId().asLong()))
            .timestamp(Instant.now())
            .build();

        Utils.executeInLoggingChannel(event.getGuildId(), LoggingType.MESSAGE_EVENTS,
            channel -> channel.createMessage(MessageCreateRequest.builder()
                .embed(embed.asRequest())
                .allowedMentions(ALLOWED_MENTIONS_DATA).build()).subscribe());
    }
}
