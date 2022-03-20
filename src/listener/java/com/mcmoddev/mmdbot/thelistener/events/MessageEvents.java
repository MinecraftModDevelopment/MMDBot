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

import com.mcmoddev.mmdbot.core.event.moderation.ScamLinkEvent;
import com.mcmoddev.mmdbot.core.util.MessageUtilities;
import com.mcmoddev.mmdbot.core.util.Pair;
import com.mcmoddev.mmdbot.thelistener.TheListener;
import com.mcmoddev.mmdbot.thelistener.util.ListenerAdapter;
import com.mcmoddev.mmdbot.thelistener.util.LoggingType;
import com.mcmoddev.mmdbot.thelistener.util.Utils;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageBulkDeleteEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.AllowedMentionsData;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.Color;
import io.github.matyrobbrt.eventdispatcher.SubscribeEvent;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MessageEvents extends ListenerAdapter {

    public static final AllowedMentions ALLOWED_MENTIONS_DATA = AllowedMentions.builder().repliedUser(false).build();

    public static final MessageEvents INSTANCE = new MessageEvents();
    private MessageEvents() {}

    @Override
    public void onMessageDelete(final MessageDeleteEvent event) {
        if (event.getGuildId().isEmpty()) {
            return; // Not from guild
        }
        if (LoggingType.MESSAGE_EVENTS.getChannels(event.getGuildId().get()).contains(event.getChannelId())) {
            return; // Don't log deletions in a logging channel
        }
        final var doLog = new AtomicBoolean(true);
        final var embed = event.getMessage()
            .flatMap(message -> Pair.of(message, message.getAuthor().orElse(null)).toOptional())
            .map(c -> c.map((message, user) -> {
                if (message.getContent().isBlank() || message.getContent().equals(".") || message.getContent().equals("^")) {
                    doLog.set(false);
                }
                final var msgSplit = message.getContent().split(" ");
                if (msgSplit.length == 1) {
                    final var matcher = MessageUtilities.MESSAGE_LINK_PATTERN.matcher(msgSplit[0]);
                    if (matcher.find()) {
                        doLog.set(false);
                    }
                }
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
        if (doLog.get()) {
            Utils.executeInLoggingChannel(event.getGuildId().get(), LoggingType.MESSAGE_EVENTS,
                channel -> channel.createMessage(MessageCreateSpec.builder()
                    .embeds(embed)
                    .allowedMentions(ALLOWED_MENTIONS_DATA).build()).subscribe(e -> {}, t -> {}));
        }
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
            final var contentChanged = new AtomicBoolean(true);
            final var embed = Pair.of(event.getOld(), newMessage.getAuthor())
                .map(Pair::makeOptional)
                .map(c -> c.map((oldMessage, author) -> {
                    if (oldMessage.getContent().equals(newMessage.getContent())) {
                        contentChanged.set(false);
                    }
                    final var embedBuilder = EmbedCreateSpec.builder();
                    embedBuilder.color(Color.VIVID_VIOLET)
                        .description("**A message sent by <@%s> in <#%s> has been edited!** [Jump to message.](%s)"
                            .formatted(author.getId().asLong(), event.getChannelId().asLong(), Utils.createMessageURL(newMessage)));
                    embedBuilder.timestamp(Instant.now());
                    embedBuilder.addField("Before", oldMessage.getContent().isBlank() ? "*Blank*" : oldMessage.getContent(), false)
                        .addField("After", newMessage.getContent().isBlank() ? "*Blank*" : newMessage.getContent(), false);
                    embedBuilder.author(author.getUsername(), null, author.getAvatarUrl())
                        .footer("Author ID: " + author.getId().asLong(), null);
                    return embedBuilder.build();
                })).orElseGet(() -> EmbedCreateSpec.builder().description("A message sent in <#%s> has been edited! Old content information could not be retrieved. [Jump to message.](%s)"
                        .formatted(event.getChannelId().asLong(), Utils.createMessageURL(newMessage)))
                    .timestamp(Instant.now()).addField("After", newMessage.getContent(), false)
                    .color(Color.ENDEAVOUR).build());

            if (contentChanged.get()) {
                Utils.executeInLoggingChannel(event.getGuildId().get(), LoggingType.MESSAGE_EVENTS,
                    channel -> channel.createMessage(MessageCreateSpec.builder()
                        .embeds(embed)
                        .allowedMentions(ALLOWED_MENTIONS_DATA).build()).subscribe(e -> {}, t -> {}));
            }
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
            channel -> channel.createMessage(MessageCreateSpec.builder()
                .embeds(embed)
                .allowedMentions(ALLOWED_MENTIONS_DATA).build()).subscribe(e -> {}, t -> {}));
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onScamLink(final ScamLinkEvent event) {
        final var embed = EmbedCreateSpec.builder()
            .title("Scam link detected!")
            .description(String.format("User <@%s> sent a scam link in <#%s>%s. Their message was deleted, and they were muted.",
                event.getTargetId(), event.getChannelId(), event.isMessageEdited() ? "" : ", by editing an old message"))
            .addField("Message Content", """
                ```
                %s
                ```""".formatted(event.getMessageContent()), false)
            .color(Color.RED)
            .timestamp(Instant.now())
            .footer(EmbedCreateFields.Footer.of("User ID: " + event.getTargetId(), event.getTargetAvatar()))
            .thumbnail(event.getTargetAvatar())
            .build();

        if (TheListener.getClient() != null) {
            // Hardcoded until configs
            final var channels = TheListener.getInstance().getConfigForGuild(Snowflake.of(event.getGuildId())).getScamLoggingChannels();
            channels.forEach(channelId -> {
                TheListener.getClient()
                    .getChannelById(channelId)
                    .subscribe(channel -> {
                        if (channel instanceof MessageChannel msgChannel) {
                            msgChannel.createMessage(MessageCreateSpec.builder()
                                .embeds(embed)
                                .allowedMentions(ALLOWED_MENTIONS_DATA)
                                .build()).subscribe();
                        }
                    });
            });
        }
    }
}
