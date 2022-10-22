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
package com.mcmoddev.mmdbot.thelistener.events;

import club.minnced.discord.webhook.send.AllowedMentions;
import com.mcmoddev.mmdbot.core.event.moderation.WarningEvent;
import com.mcmoddev.mmdbot.core.util.webhook.WebhookManager;
import com.mcmoddev.mmdbot.thelistener.util.LoggingType;
import com.mcmoddev.mmdbot.thelistener.util.Utils;
import io.github.matyrobbrt.eventdispatcher.SubscribeEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogKey;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static com.mcmoddev.mmdbot.thelistener.TheListener.getInstance;

public final class ModerationEvents extends ListenerAdapter {

    public static final ModerationEvents INSTANCE = new ModerationEvents();

    private static final String WEBHOOK_NAME = "ModerationLogs";
    private static final WebhookManager WEBHOOKS = WebhookManager.of(e
        -> e.trim().equals(WEBHOOK_NAME), WEBHOOK_NAME, AllowedMentions.none());

    public static final Color RUBY = new Color(0xE91E63);
    public static final Color LIGHT_SEA_GREEN = new Color(0x1ABC9C);

    private ModerationEvents() {
    }

    @Override
    public void onGuildBan(@NotNull final GuildBanEvent event) {
        Utils.getAuditLog(event.getGuild(), event.getUser().getIdLong(), log -> log
            .limit(5)
            .type(ActionType.BAN), log -> {
            final var embed = new EmbedBuilder();
            final var bannedUser = event.getUser();
            final var bannedBy = Optional.ofNullable(log.getUser());
            embed.setColor(Color.RED);
            embed.setTitle("User Banned.");
            embed.addField("**User:**", bannedUser.getAsTag(), true);
            if (log.getReason() != null) {
                embed.addField("**Ban reason:**", log.getReason(), false);
            } else {
                embed.addField("**Ban reason:**", "Reason for ban was not provided or could not be found "
                    + "please contact a member of staff for more information about this ban ban.", false);
            }
            embed.setFooter("User ID: " + bannedUser.getId(), bannedUser.getEffectiveAvatarUrl());
            embed.setTimestamp(Instant.now());
            log(event.getGuild().getIdLong(), event.getJDA(), embed.build(), bannedBy);
        });
    }

    @Override
    public void onGuildUnban(@NotNull final GuildUnbanEvent event) {
        Utils.getAuditLog(event.getGuild(), event.getUser().getIdLong(), log -> log
            .limit(5)
            .type(ActionType.UNBAN), log -> {
            final var embed = new EmbedBuilder();
            final var unBannedUser = event.getUser();
            final var bannedBy = Optional.ofNullable(log.getUser());
            embed.setColor(Color.GREEN);
            embed.setTitle("User Un-banned.");
            embed.addField("**User:**", unBannedUser.getAsTag(), true);
            embed.setFooter("User ID: " + unBannedUser.getId(), unBannedUser.getEffectiveAvatarUrl());
            embed.setTimestamp(Instant.now());
            log(event.getGuild().getIdLong(), event.getJDA(), embed.build(), bannedBy);
        });
    }

    @Override
    public void onGuildMemberUpdateNickname(@NotNull final GuildMemberUpdateNicknameEvent event) {
        final var embed = new EmbedBuilder();
        final var targetUser = event.getUser();
        embed.setColor(Color.YELLOW);
        embed.setTitle("Nickname Changed");
        embed.addField("User:", targetUser.getAsTag(), true);
        embed.addField("Old Nickname:", event.getOldNickname() == null
            ? "*None*" : event.getOldNickname(), true);
        embed.addField("New Nickname:", event.getNewNickname() == null
            ? "*None*" : event.getNewNickname(), true);
        embed.setFooter("User ID: " + event.getUser().getId(), event.getUser().getEffectiveAvatarUrl());
        embed.setTimestamp(Instant.now());
        logWithWebhook(event.getGuild().getIdLong(), event.getJDA(), embed.build(), event.getUser());
    }

    @Override
    public void onGuildMemberRemove(@NotNull final GuildMemberRemoveEvent event) {
        Utils.getAuditLog(event.getGuild(), event.getUser().getIdLong(), log -> log
            .type(ActionType.KICK)
            .limit(5), log -> {
            if (log.getTimeCreated().toInstant().isBefore(Instant.now().minus(2, ChronoUnit.MINUTES))) {
                return;
            }

            final var embed = new EmbedBuilder();
            final var kicker = Optional.ofNullable(log.getUser());
            final var kickedUser = event.getUser();

            if (kicker.isPresent() && kicker.get().isBot()) {
                var botKickMessage = kickedUser.getAsTag() + " was kicked! Kick Reason: " + log.getReason();
                log(event.getGuild().getIdLong(), event.getJDA(), botKickMessage, kicker);
            } else {
                embed.setColor(RUBY);
                embed.setTitle("User Kicked");
                embed.addField("**Name:**", kickedUser.getAsTag(), true);
                embed.addField("**Kick reason:**", log.getReason() != null ? log.getReason() :
                    ("Reason for kick was not provided or could not be found, please contact "
                        + "a member of staff for more information about this kick."), false);
                embed.setFooter("User ID: " + kickedUser.getId(), kickedUser.getAvatarUrl());
                embed.setTimestamp(Instant.now());

                log(event.getGuild().getIdLong(), event.getJDA(), embed.build(), kicker);
            }
        });
    }

    @Override
    public void onGuildMemberUpdateTimeOut(@NotNull final GuildMemberUpdateTimeOutEvent event) {
        if (event.getOldTimeOutEnd() == null && event.getNewTimeOutEnd() != null) {
            // Somebody was timed out!
            Utils.getAuditLog(event.getGuild(), event.getUser().getIdLong(), log -> log.type(ActionType.MEMBER_UPDATE)
                .limit(5), log -> {
                if (log.getChangeByKey(AuditLogKey.MEMBER_TIME_OUT) == null) return;
                final var embed = new EmbedBuilder();
                final var moderator = Optional.ofNullable(log.getUser());
                final var user = event.getUser();

                embed.setColor(LIGHT_SEA_GREEN);
                embed.setTitle("User Timed Out");
                embed.addField("**User:**", user.getAsTag(), true);
                embed.addField("**Timeout End:**", TimeFormat.RELATIVE.format(event.getNewTimeOutEnd()),
                    true);
                embed.addField("**Reason:**", log.getReason() != null ? log.getReason() :
                    "Reason for timeout was not provided or could not be found, please ask a member of staff for "
                        + "information about this timeout.", false);
                embed.setFooter("User ID: " + user.getId(), user.getEffectiveAvatarUrl());
                embed.setTimestamp(Instant.now());
                log(event.getGuild().getIdLong(), event.getJDA(), embed.build(), moderator);
            });
        } else if (event.getOldTimeOutEnd() != null && event.getNewTimeOutEnd() == null) {
            // Somebody's timeout was removed
            Utils.getAuditLog(event.getGuild(), event.getUser().getIdLong(), log -> log.type(ActionType.MEMBER_UPDATE)
                .limit(5), log -> {
                if (log.getChangeByKey(AuditLogKey.MEMBER_TIME_OUT) == null) return;
                final var embed = new EmbedBuilder();
                final var moderator = Optional.ofNullable(log.getUser());
                final var user = event.getUser();
                embed.setColor(Color.CYAN);
                embed.setTitle("User Timeout Removed");
                embed.addField("**User:**", user.getAsTag(), true);
                embed.addField("**Old Timeout End:**", TimeFormat.RELATIVE.format(event.getOldTimeOutEnd()),
                    true);
                embed.setFooter("User ID: " + user.getId(), user.getEffectiveAvatarUrl());
                embed.setTimestamp(Instant.now());
                log(event.getGuild().getIdLong(), event.getJDA(), embed.build(), moderator);
            });
        }
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
            logWithWebhook(event.getGuildId(), jda, embed.build(), moderator);
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

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void log(long guildId, JDA jda, MessageEmbed embed, Optional<User> owner) {
        owner.ifPresentOrElse(user -> logWithWebhook(guildId, jda, embed, user), () -> log(guildId, jda, embed));
    }

    private void log(long guildId, JDA jda, String message, Optional<User> owner) {
        owner.ifPresentOrElse(user -> logWithWebhook(guildId, jda, message, user), () -> log(guildId, jda, message));
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

    private void log(long guildId, JDA jda, String message) {
        final var loggingChannels = LoggingType.MODERATION_EVENTS.getChannels(guildId);
        loggingChannels
            .forEach(id -> {
                final var ch = id.resolve(idL -> jda.getChannelById(MessageChannel.class, idL));
                if (ch != null) {
                    ch.sendMessage(message).queue();
                }
            });
    }

    private void logWithWebhook(long guildId, JDA jda, MessageEmbed embed, User author) {
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

    private void logWithWebhook(long guildId, JDA jda, String message, User author) {
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
