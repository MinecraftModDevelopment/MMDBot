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
package com.mcmoddev.mmdbot.thelistener.events;

import club.minnced.discord.webhook.send.AllowedMentions;
import com.mcmoddev.mmdbot.core.event.moderation.ScamLinkEvent;
import com.mcmoddev.mmdbot.core.util.Utils;
import com.mcmoddev.mmdbot.core.util.jda.caching.MessageData;
import com.mcmoddev.mmdbot.core.util.webhook.WebhookManager;
import com.mcmoddev.mmdbot.thelistener.TheListener;
import com.mcmoddev.mmdbot.thelistener.util.LoggingType;
import io.github.matyrobbrt.eventdispatcher.SubscribeEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.Instant;

public final class MessageEvents extends ListenerAdapter {

    public static final MessageEvents INSTANCE = new MessageEvents();

    private static final String WEBHOOK_NAME = "MessageLogs";
    private static final WebhookManager WEBHOOKS = WebhookManager.of(e -> e.trim().equals(WEBHOOK_NAME), WEBHOOK_NAME, AllowedMentions.none());

    public static final java.awt.Color GRAY_CHATEAOU = new java.awt.Color(0x979C9F);
    public static final java.awt.Color VIVID_VIOLET = new java.awt.Color(0x71368A);

    private MessageEvents() {
    }

    public void onMessageDelete(final net.dv8tion.jda.api.events.message.MessageDeleteEvent event, final MessageData data) {
        if (!event.isFromGuild() || (data.getContent().isBlank() && data.getAttachments().isEmpty())) return;
        final var loggingChannels = LoggingType.MESSAGE_EVENTS.getChannels(event.getGuild().getIdLong());
        if (loggingChannels.stream().anyMatch(v -> v.test(event.getChannel()))) return; // Don't log in event channels
        final var msgSplit = data.getContent().split(" ");
        if (msgSplit.length == 1) {
            final var matcher = Message.JUMP_URL_PATTERN.matcher(msgSplit[0]);
            if (matcher.find()) {
                return;
            }
        }
        final var embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(GRAY_CHATEAOU)
            .setDescription("""
                **A message sent by <@%s> in <#%s> has been deleted!**
                %s"""
                .formatted(data.getAuthorId(), data.getChannelId(), Utils.truncate(data.getContent(), MessageEmbed.DESCRIPTION_MAX_LENGTH - 30)));
        embedBuilder.setTimestamp(Instant.now())
            .setFooter("Author: %s | Message ID: %s".formatted(data.getAuthorId(), data.getChannelId()), null);
        final var interaction = data.getInteraction();
        if (interaction != null) {
            embedBuilder.addField("Interaction Author: ", "<@%s> (%s)".formatted(interaction.getAuthorId(), interaction.getAuthorId()), true);
        }
        if (!data.getAttachments().isEmpty()) {
            embedBuilder.setImage(data.getAttachments().get(0));
        }
        final var embed = embedBuilder.build();
        loggingChannels.forEach(id -> {
            final var ch = id.resolve(idL -> event.getJDA().getChannelById(StandardGuildMessageChannel.class, idL));
            if (ch != null) {
                WEBHOOKS.getWebhook(ch)
                    .send(Utils.webhookMessage(embed)
                        .setUsername(data.getAuthorUsername())
                        .setAvatarUrl(data.getAuthorAvatar())
                        .build());
            }
        });
    }

    public void onMessageUpdate(final net.dv8tion.jda.api.events.message.MessageUpdateEvent event, MessageData data) {
        final var newMessage = event.getMessage();
        if (!event.isFromGuild() || (newMessage.getContentRaw().isBlank() && newMessage.getAttachments().isEmpty()))
            return;
        final var loggingChannels = LoggingType.MESSAGE_EVENTS.getChannels(event.getGuild().getIdLong());
        if (loggingChannels.stream().anyMatch(v -> v.test(event.getChannel()))) return; // Don't log in event channels
        if (newMessage.getContentRaw().equals(data.getContent())) {
            return;
        }
        final var embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(VIVID_VIOLET)
            .setDescription("**A message sent by <@%s> in <#%s> has been edited!** [Jump to message.](%s)"
                .formatted(data.getAuthorId(), event.getChannel().getId(), newMessage.getJumpUrl()));
        embedBuilder.setTimestamp(Instant.now());
        embedBuilder.addField("Before", data.getContent().isBlank() ? "*Blank*" : Utils.truncate(data.getContent(), MessageEmbed.VALUE_MAX_LENGTH), false)
            .addField("After", newMessage.getContentRaw().isBlank() ? "*Blank*" : Utils.truncate(newMessage.getContentRaw(), MessageEmbed.VALUE_MAX_LENGTH), false);
        embedBuilder.setFooter("Author ID: " + data.getAuthorId(), null);
        final var interaction = data.getInteraction();
        if (interaction != null) {
            embedBuilder.addField("Interaction Author: ", "<@%s> (%s)".formatted(interaction.getAuthorId(), interaction.getAuthorId()), true);
        }
        if (!data.getAttachments().isEmpty()) {
            embedBuilder.setImage(data.getAttachments().get(0));
        }
        final var embed = embedBuilder.build();
        loggingChannels
            .forEach(id -> {
                final var ch = id.resolve(idL -> event.getJDA().getChannelById(StandardGuildMessageChannel.class, idL));
                if (ch != null) {
                    WEBHOOKS.getWebhook(ch)
                        .send(Utils.webhookMessage(embed)
                            .setUsername(data.getAuthorUsername())
                            .setAvatarUrl(data.getAuthorAvatar())
                            .build());
                }
            });
    }

    @Override
    public void onMessageBulkDelete(@NotNull final net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent event) {
        final var embed = new EmbedBuilder()
            .setDescription("%s messages have been bulk deleted in %s!"
                .formatted(event.getMessageIds().size(), event.getChannel().getAsMention()))
            .setTimestamp(Instant.now())
            .build();
        LoggingType.MESSAGE_EVENTS.getChannels(event.getGuild().getIdLong())
            .forEach(snowflakeValue -> {
                final var ch = snowflakeValue.resolve(id -> event.getJDA().getChannelById(MessageChannel.class, id));
                if (ch != null) {
                    ch.sendMessageEmbeds(embed).queue();
                }
            });
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
            .setColor(Color.RED)
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
