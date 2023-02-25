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
import com.google.errorprone.annotations.CheckReturnValue;
import com.mcmoddev.mmdbot.core.event.moderation.WarningEvent;
import com.mcmoddev.mmdbot.core.util.webhook.WebhookManager;
import com.mcmoddev.mmdbot.thelistener.util.LoggingType;
import io.github.matyrobbrt.eventdispatcher.SubscribeEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogChange;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.audit.AuditLogKey;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Instant;
import java.time.OffsetDateTime;

import static com.mcmoddev.mmdbot.thelistener.TheListener.getInstance;
import static java.util.Objects.requireNonNullElse;

public final class ModerationEvents extends ListenerAdapter {

    public static final ModerationEvents INSTANCE = new ModerationEvents();
    private static final Logger LOGGER = LoggerFactory.getLogger(ModerationEvents.class);

    private static final String WEBHOOK_NAME = "ModerationLogs";
    private static final WebhookManager WEBHOOKS = WebhookManager.of(e
        -> e.trim().equals(WEBHOOK_NAME), WEBHOOK_NAME, AllowedMentions.none());

    public static final Color RUBY = new Color(0xE91E63);
    public static final Color LIGHT_SEA_GREEN = new Color(0x1ABC9C);

    private ModerationEvents() {
    }

    @Override
    public void onGuildAuditLogEntryCreate(@NotNull final GuildAuditLogEntryCreateEvent event) {
        final AuditLogEntry entry = event.getEntry();
        final ActionType type = entry.getType();

        switch (type) {
            case BAN -> retrieveUsers(entry, this::onBan).queue();
            case UNBAN -> retrieveUsers(entry, this::onUnban).queue();
            case MEMBER_UPDATE -> {
                final @Nullable AuditLogChange nicknameChange = entry.getChangeByKey(AuditLogKey.MEMBER_NICK);
                if (nicknameChange != null) retrieveUsers(entry,
                    (target, actor, auditEntry) -> this.onNicknameUpdate(target, actor, auditEntry, nicknameChange)).queue();

                final @Nullable AuditLogChange timeoutChange = entry.getChangeByKey(AuditLogKey.MEMBER_TIME_OUT);
                if (timeoutChange != null) retrieveUsers(entry,
                    (target, actor, auditEntry) -> this.onTimeoutUpdate(target, actor, auditEntry, timeoutChange)).queue();
            }
            case KICK -> retrieveUsers(entry, this::onKick).queue();
        }
    }

    @FunctionalInterface
    private interface AuditLogEntryHandler {
        void handle(final User target, final User actor, final AuditLogEntry entry);
    }

    @CheckReturnValue
    private RestAction<Void> retrieveUsers(final AuditLogEntry entry, final AuditLogEntryHandler handler) {
        final String targetId = entry.getTargetId();
        final String actorId = entry.getUserId();

        final RestAction<@Nullable User> targetRestAction = entry.getJDA().retrieveUserById(targetId)
            .onErrorMap(ErrorResponse.UNKNOWN_USER::test, e -> {
                LOGGER.error("Could not retrieve target user for ID {}", targetId);
                return null;
            });
        final RestAction<@Nullable User> actorRestAction = entry.getJDA().retrieveUserById(actorId)
            .onErrorMap(ErrorResponse.UNKNOWN_USER::test, e -> {
                LOGGER.error("Could not retrieve actor user for ID {}", actorId);
                return null;
            });

        return targetRestAction.and(actorRestAction, (targetUser, actorUser) -> {
            if (targetUser != null && actorUser != null) {
                handler.handle(targetUser, actorUser, entry);
            }
            return null;
        });
    }

    public void onBan(final User bannedUser, final User bannedBy, final AuditLogEntry entry) {
        final var reason = requireNonNullElse(entry.getReason(),
            "_Reason for ban was not provided or could not be found; "
                + "please contact a member of staff for more information about this ban._");

        final var embed = new EmbedBuilder();
        embed.setColor(Color.RED);
        embed.setTitle("User Banned.");
        embed.addField("**User:**", bannedUser.getAsTag(), true);
        embed.addField("**Ban reason:**", reason, false);
        embed.setFooter("User ID: " + bannedUser.getId(), bannedUser.getEffectiveAvatarUrl());
        embed.setTimestamp(Instant.now());

        log(entry.getGuild().getIdLong(), entry.getJDA(), embed.build(), bannedBy);
    }

    public void onUnban(final User unBannedUser, final User bannedBy, final AuditLogEntry entry) {
        final var embed = new EmbedBuilder();
        embed.setColor(Color.GREEN);
        embed.setTitle("User Un-banned.");
        embed.addField("**User:**", unBannedUser.getAsTag(), true);
        embed.setFooter("User ID: " + unBannedUser.getId(), unBannedUser.getEffectiveAvatarUrl());
        embed.setTimestamp(Instant.now());

        log(entry.getGuild().getIdLong(), entry.getJDA(), embed.build(), bannedBy);
    }

    public void onNicknameUpdate(final User target, final User editor, final AuditLogEntry entry, final AuditLogChange nicknameChange) {
        final var embed = new EmbedBuilder();
        embed.setColor(Color.YELLOW);
        embed.setTitle("Nickname Changed");
        embed.addField("User:", target.getAsTag(), true);
        embed.addField("Old Nickname:", wrapNicknameValue(nicknameChange.getOldValue()), true);
        embed.addField("New Nickname:", wrapNicknameValue(nicknameChange.getNewValue()), true);
        embed.setFooter("User ID: " + target.getId(), target.getEffectiveAvatarUrl());
        embed.setTimestamp(Instant.now());
        log(entry.getGuild().getIdLong(), entry.getJDA(), embed.build(), editor);
    }

    private static String wrapNicknameValue(@Nullable String nicknameValue) {
        if (nicknameValue == null) return "*None*";
        return MarkdownSanitizer.escape(nicknameValue);
    }

    public void onKick(final User kickedUser, final User kicker, final AuditLogEntry entry) {
        final var embed = new EmbedBuilder();
        if (kicker.isBot()) {
            var botKickMessage = kickedUser.getAsTag() + " was kicked! Kick Reason: " + entry.getReason();
            log(entry.getGuild().getIdLong(), entry.getJDA(), botKickMessage, kicker);
        } else {
            final var reason = requireNonNullElse(entry.getReason(), "Reason for kick was not provided or could not be found, please contact "
                + "a member of staff for more information about this kick.");

            embed.setColor(RUBY);
            embed.setTitle("User Kicked");
            embed.addField("**Name:**", kickedUser.getAsTag(), true);
            embed.addField("**Kick reason:**", reason, false);
            embed.setFooter("User ID: " + kickedUser.getId(), kickedUser.getAvatarUrl());
            embed.setTimestamp(Instant.now());

            log(entry.getGuild().getIdLong(), entry.getJDA(), embed.build(), kicker);
        }
    }

    public void onTimeoutUpdate(final User user, final User moderator, final AuditLogEntry entry, final AuditLogChange timeoutChange) {
        final OffsetDateTime oldTimeoutEnd = parseDateTime(timeoutChange.getOldValue());
        final OffsetDateTime newTimeoutEnd = parseDateTime(timeoutChange.getNewValue());

        if (oldTimeoutEnd == null && newTimeoutEnd != null) {
            // Somebody was timed out!

            final String reason = requireNonNullElse(entry.getReason(),
                "Reason for timeout was not provided or could not be found; " +
                    "please ask a member of staff for information about this timeout.");

            final var embed = new EmbedBuilder();

            embed.setColor(LIGHT_SEA_GREEN);
            embed.setTitle("User Timed Out");
            embed.addField("**User:**", user.getAsTag(), true);
            embed.addField("**Timeout End:**", TimeFormat.RELATIVE.format(newTimeoutEnd),
                true);
            embed.addField("**Reason:**", reason, false);
            embed.setFooter("User ID: " + user.getId(), user.getEffectiveAvatarUrl());
            embed.setTimestamp(Instant.now());
            log(entry.getGuild().getIdLong(), entry.getJDA(), embed.build(), moderator);

        } else if (oldTimeoutEnd != null && newTimeoutEnd == null) {
            // Somebody's timeout was removed
            final var embed = new EmbedBuilder();

            embed.setColor(Color.CYAN);
            embed.setTitle("User Timeout Removed");
            embed.addField("**User:**", user.getAsTag(), true);
            embed.addField("**Old Timeout End:**", TimeFormat.RELATIVE.format(oldTimeoutEnd),
                true);
            embed.setFooter("User ID: " + user.getId(), user.getEffectiveAvatarUrl());
            embed.setTimestamp(Instant.now());
            log(entry.getGuild().getIdLong(), entry.getJDA(), embed.build(), moderator);

        }
    }

    private static @Nullable OffsetDateTime parseDateTime(@Nullable String dateTimeString) {
        if (dateTimeString == null) return null;
        return OffsetDateTime.parse(dateTimeString);
    }

    @SubscribeEvent
    public void onWarnAdd(final WarningEvent.Add event) {
        if (getInstance() == null) {
            return;
        }
        final var jda = getInstance().getJDA();
        final var doc = event.getDocument();
        jda.retrieveUserById(doc.userId()).and(jda.retrieveUserById(event.getModeratorId()), (user, moderator) -> {
            final var embed = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("Warning Added")
                .setDescription(user.getAsTag() + " has been given a warning.")
                .addField("Warning Reason:", doc.reason(), false)
                .addField("Warning ID:", doc.warnId(), false)
                .setFooter("User ID: " + user.getId(), user.getEffectiveAvatarUrl())
                .setTimestamp(Instant.now());
            log(event.getGuildId(), jda, embed.build(), moderator);
            return null;
        }).queue();
    }

    @SubscribeEvent
    public void onWarnClear(final WarningEvent.Clear event) {
        if (getInstance() == null) {
            return;
        }
        final var jda = getInstance().getJDA();
        final var warnDoc = event.getDocument();
        jda.retrieveUserById(warnDoc.userId()).and(jda.retrieveUserById(event.getModeratorId()), (user, moderator) -> {
            final var embed = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Warning Removed")
                .setDescription("One of the warnings of " + user.getAsTag() + " has been removed!")
                .setThumbnail(user.getAvatarUrl())
                .addField("Old Warning:", warnDoc.reason(), false)
                .setFooter("User ID: " + user.getId(), user.getEffectiveAvatarUrl())
                .setTimestamp(Instant.now());
            log(event.getGuildId(), jda, embed.build());
            return null;
        }).queue();
    }

    @SubscribeEvent
    public void onWarnClearAll(final WarningEvent.ClearAllWarns event) {
        if (getInstance() == null) {
            return;
        }
        getInstance().getJDA().retrieveUserById(event.getModeratorId())
            .queue(user -> {
                final var embed = new EmbedBuilder()
                    .setColor(java.awt.Color.GREEN)
                    .setTitle("All Warnings Removed")
                    .setDescription("All of ``" + user.getAsTag() + "``'s warnings have been removed!")
                    .setTimestamp(Instant.now())
                    .setFooter("User ID: " + user.getId(), user.getEffectiveAvatarUrl())
                    .build();
                log(event.getGuildId(), user.getJDA(), embed);
            });
    }

    private void log(long guildId, JDA jda, MessageEmbed embed) {
        final var loggingChannels = LoggingType.MODERATION_EVENTS.getChannels(guildId);
        loggingChannels
            .forEach(id -> {
                final var ch = id.resolve(idL -> jda.getChannelById(MessageChannel.class, idL));
                if (ch != null) {
                    ch.sendMessageEmbeds(embed).queue();
                }
            });
    }

    private void log(long guildId, JDA jda, MessageEmbed embed, User author) {
        final var loggingChannels = LoggingType.MODERATION_EVENTS.getChannels(guildId);
        loggingChannels
            .forEach(id -> {
                final var ch = id.resolve(idL -> jda.getChannelById(StandardGuildMessageChannel.class, idL));
                if (ch != null) {
                    WEBHOOKS.getWebhook(ch)
                        .send(com.mcmoddev.mmdbot.core.util.Utils.webhookMessage(embed)
                            .setUsername(author.getName())
                            .setAvatarUrl(author.getEffectiveAvatarUrl())
                            .build());
                }
            });
    }

    private void log(long guildId, JDA jda, String message, User author) {
        final var loggingChannels = LoggingType.MODERATION_EVENTS.getChannels(guildId);
        loggingChannels
            .forEach(id -> {
                final var ch = id.resolve(idL -> jda.getChannelById(StandardGuildMessageChannel.class, idL));
                if (ch != null) {
                    WEBHOOKS.getWebhook(ch)
                        .send(com.mcmoddev.mmdbot.core.util.Utils.webhookMessage(message)
                            .setUsername(author.getName())
                            .setAvatarUrl(author.getEffectiveAvatarUrl())
                            .build());
                }
            });
    }
}
