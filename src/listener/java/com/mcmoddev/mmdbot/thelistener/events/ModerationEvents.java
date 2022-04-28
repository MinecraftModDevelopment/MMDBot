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

import static com.mcmoddev.mmdbot.thelistener.TheListener.getInstance;
import static com.mcmoddev.mmdbot.thelistener.util.Utils.mentionAndID;
import com.mcmoddev.mmdbot.core.event.moderation.WarningEvent;
import com.mcmoddev.mmdbot.thelistener.TheListener;
import com.mcmoddev.mmdbot.thelistener.util.LoggingType;
import com.mcmoddev.mmdbot.thelistener.util.Utils;
import io.github.matyrobbrt.eventdispatcher.SubscribeEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogKey;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

// TODO add timeout events
public final class ModerationEvents extends ListenerAdapter {
    public static final ModerationEvents INSTANCE = new ModerationEvents();

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
            embed.setThumbnail(bannedUser.getAvatarUrl());
            embed.addField("**Name:**", bannedUser.getName(), false);
            embed.addField("**User ID:**", bannedUser.getId(), false);
            embed.addField("**Profile:**", bannedUser.getAsMention(), false);
            embed.addField("**Profile Age**", TimeFormat.RELATIVE
                .format(bannedUser.getTimeCreated()), false);

            if (log.getReason() != null) {
                embed.addField("**Ban reason:**", log.getReason(), false);
            } else {
                embed.addField("**Ban reason:**", "Reason for ban was not provided or could not be found, please contact " + bannedBy.map(User::getAsMention).orElse("the banner."), false);
            }

            final var targetId = log.getTargetIdLong();

            if (targetId != bannedUser.getIdLong()) {
                TheListener.LOGGER.warn("Inconsistency between target of retrieved audit log "
                        + "entry and actual ban event target: retrieved is {}, but target is {}",
                    targetId, bannedUser);
            } else {
                embed.addField("Banned By: ", bannedBy.map(u -> "<@%s> (%s)".formatted(u.getId(), u.getId())).orElse("Unknown"), false);
            }

            bannedBy.ifPresent(u -> embed.setFooter("Moderator ID: " + u.getId(), u.getAvatarUrl()));

            embed.setTimestamp(Instant.now());

            log(event.getGuild().getIdLong(), event.getJDA(), embed.build());
        });
    }

    @Override
    public void onGuildUnban(@NotNull final GuildUnbanEvent event) {
        Utils.getAuditLog(event.getGuild(), event.getUser().getIdLong(), log -> log
            .limit(5)
            .type(ActionType.UNBAN), log -> {
            final var embed = new EmbedBuilder();
            final var bannedUser = event.getUser();
            final var bannedBy = Optional.ofNullable(log.getUser());

            embed.setColor(Color.GREEN);
            embed.setTitle("User Un-banned.");
            embed.setThumbnail(bannedUser.getAvatarUrl());
            embed.addField("**Name:**", bannedUser.getName(), false);
            embed.addField("**User ID:**", bannedUser.getId(), false);
            embed.addField("**Profile:**", bannedUser.getAsMention(), false);
            embed.addField("**Profile Age**", TimeFormat.RELATIVE
                .format(bannedUser.getTimeCreated()), false);

            final var targetId = log.getTargetIdLong();

            if (targetId != bannedUser.getIdLong()) {
                TheListener.LOGGER.warn("Inconsistency between target of retrieved audit log "
                        + "entry and actual unban event target: retrieved is {}, but target is {}",
                    targetId, bannedUser);
            } else {
                embed.addField("Un-banned By: ", bannedBy.map(u -> "%s (%s)".formatted(u.getAsMention(), u.getId())).orElse("Unknown"), false);
            }

            bannedBy.ifPresent(u -> embed.setFooter("Moderator ID: " + u.getId(), u.getAvatarUrl()));
            embed.setTimestamp(Instant.now());
            log(event.getGuild().getIdLong(), event.getJDA(), embed.build());
        });
    }

    @Override
    public void onGuildMemberUpdateNickname(@NotNull final GuildMemberUpdateNicknameEvent event) {
        Utils.getAuditLog(event.getGuild(), event.getMember().getIdLong(), log -> log
            .type(ActionType.MEMBER_UPDATE)
            .limit(5), entry -> {
            if (entry.getChangeByKey(AuditLogKey.MEMBER_NICK) == null) {
                onNickNoAudit(event);
            } else {
                final var embed = new EmbedBuilder();
                final var editor = Optional.ofNullable(entry.getUser());

                embed.setColor(Color.YELLOW);
                embed.setTitle("Nickname Changed");
                embed.setThumbnail(event.getUser().getAvatarUrl());
                embed.addField("User:", event.getUser().getAsMention() + " (" + event.getUser().getId() + ")", true);
                embed.setTimestamp(Instant.now());
                embed.addField("Nickname Editor: ", editor.map(u -> "%s (%s)".formatted(u.getAsMention(), u.getId())).orElse("Unknown"), false);

                embed.addField("Old Nickname:", event.getOldNickname() == null ? "*None*" : event.getOldNickname(), true);
                embed.addField("New Nickname:", event.getNewNickname() == null ? "*None*" : event.getNewNickname(), true);

                log(event.getGuild().getIdLong(), event.getJDA(), embed.build());
            }
        }, () -> onNickNoAudit(event));
    }

    private void onNickNoAudit(final GuildMemberUpdateNicknameEvent event) {
        final var embed = new EmbedBuilder();
        final var targetUser = event.getUser();

        embed.setColor(Color.YELLOW);
        embed.setTitle("Nickname Changed");
        embed.setThumbnail(targetUser.getAvatarUrl());
        embed.addField("User:", targetUser.getAsMention() + " (" + targetUser.getId() + ")", true);
        embed.setTimestamp(Instant.now());

        embed.addField("Old Nickname:", event.getOldNickname() == null ? "*None*" : event.getOldNickname(), true);
        embed.addField("New Nickname:", event.getNewNickname() == null ? "*None*" : event.getNewNickname(), true);

        log(event.getGuild().getIdLong(), event.getJDA(), embed.build());
    }

    @Override
    public void onGuildMemberRemove(@NotNull final GuildMemberRemoveEvent event) {
        Utils.getAuditLog(event.getGuild(), event.getUser().getIdLong(), log -> log
            .type(ActionType.KICK)
            .limit(5), log -> {
            if (log.getTimeCreated().toInstant()
                .isBefore(Instant.now()
                    .minus(2, ChronoUnit.MINUTES))) {
                return;
            }

            final var embed = new EmbedBuilder();
            final var kicker = Optional.ofNullable(log.getUser());
            final var kickedUser = event.getUser();

            embed.setColor(RUBY);
            embed.setTitle("User Kicked");
            embed.setThumbnail(kickedUser.getAvatarUrl());
            embed.addField("**Name:**", kickedUser.getName(), false);
            embed.addField("**User ID:**", kickedUser.getId(), false);
            embed.addField("**Profile:**", kickedUser.getAsMention(), false);
            embed.addField("**Profile Age**", TimeFormat.RELATIVE
                .format(kickedUser.getTimeCreated()), false);

            embed.addField("Guild Join Time:", Optional.ofNullable(event.getMember()).map(Member::getTimeJoined)
                .map(TimeFormat.DATE_TIME_SHORT::format).orElse("Join time could not be determined!"), true);

            embed.addField("**Kick reason:**", log.getReason() != null ? log.getReason() :
                ("Reason for kick was not provided or could not be found, please contact "
                + kicker.map(User::getAsMention).orElse("the kicker.")), false);

            final var targetId = log.getTargetIdLong();

            if (targetId != event.getUser().getIdLong()) {
                TheListener.LOGGER.warn("Inconsistency between target of retrieved audit log "
                        + "entry and actual kick event target: retrieved is {}, but target is {}",
                    targetId, event.getUser());
            } else {
                embed.addField("Kicked By: ", kicker.map(u -> "<@%s> (%s)".formatted(u.getId(), u.getId())).orElse("Unknown"), false);
            }

            kicker.ifPresent(u -> embed.setFooter("Moderator ID: " + u.getId(), u.getAvatarUrl()));

            log(event.getGuild().getIdLong(), event.getJDA(), embed.build());
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
                embed.setThumbnail(user.getAvatarUrl());
                embed.addField("**User:**", "%s (%s)".formatted(user.getAsMention(), user.getId()), false);
                embed.addField("**Timeout End:**", TimeFormat.RELATIVE.format(event.getNewTimeOutEnd()), false);

                embed.addField("**Reason:**", log.getReason() != null ? log.getReason() :
                    ("Reason for timeout was not provided or could not be found, please contact "
                        + moderator.map(User::getAsMention).orElse("the moderator.")), false);

                final var targetId = log.getTargetIdLong();

                if (targetId != event.getUser().getIdLong()) {
                    TheListener.LOGGER.warn("Inconsistency between target of retrieved audit log "
                            + "entry and actual kick event target: retrieved is {}, but target is {}",
                        targetId, event.getUser());
                } else {
                    embed.addField("Timed Out By: ", moderator.map(u -> "<@%s> (%s)".formatted(u.getId(), u.getId())).orElse("Unknown"), false);
                }

                moderator.ifPresent(u -> embed.setFooter("Moderator ID: " + u.getId(), u.getAvatarUrl()));

                log(event.getGuild().getIdLong(), event.getJDA(), embed.build());
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
                embed.setThumbnail(user.getAvatarUrl());
                embed.addField("**User:**", "%s (%s)".formatted(user.getAsMention(), user.getId()), false);
                embed.addField("**Old Timeout End:**", TimeFormat.RELATIVE.format(event.getOldTimeOutEnd()), false);

                final var targetId = log.getTargetIdLong();

                if (targetId != event.getUser().getIdLong()) {
                    TheListener.LOGGER.warn("Inconsistency between target of retrieved audit log "
                            + "entry and actual kick event target: retrieved is {}, but target is {}",
                        targetId, event.getUser());
                } else {
                    embed.addField("Timeout Removed By: ", moderator.map(u -> "<@%s> (%s)".formatted(u.getId(), u.getId())).orElse("Unknown"), false);
                }

                moderator.ifPresent(u -> embed.setFooter("Moderator ID: " + u.getId(), u.getAvatarUrl()));

                log(event.getGuild().getIdLong(), event.getJDA(), embed.build());
            });
        }
    }

    @SubscribeEvent
    public void onWarnAdd(final WarningEvent.Add event) {
        if (getInstance() == null) return;
        final var jda = getInstance().getJDA();
        final var doc = event.getDocument();
        jda.retrieveUserById(doc.userId()).and(jda.retrieveUserById(event.getModeratorId()), (user, moderator) -> {
            final var embed = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Warning Cleared")
                .setDescription("One of the warnings of " + mentionAndID(doc.userId()) + " has been removed!")
                .setThumbnail(user.getAvatarUrl())
                .addField("Old warning reason:", doc.reason(), false)
                .addField("Old warner:", mentionAndID(doc.userId()), false)
                .setTimestamp(Instant.now())
                .setFooter("Moderator ID: " + event.getModeratorId(), moderator.getAvatarUrl());
            log(event.getGuildId(), jda, embed.build());
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
                .setTitle("Warning Cleared")
                .setDescription("One of the warnings of " + mentionAndID(warnDoc.userId()) + " has been removed!")
                .setThumbnail(user.getAvatarUrl())
                .addField("Old warning reason:", warnDoc.reason(), false)
                .addField("Old warner:", mentionAndID(warnDoc.userId()), false)
                .setTimestamp(Instant.now())
                .setFooter("Moderator ID: " + event.getModeratorId(), moderator.getAvatarUrl());
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
                   .setTitle("Warnings Cleared")
                   .setDescription("All of the warnings of " + mentionAndID(event.getTargetId()) + " have been cleared!")
                   .setTimestamp(Instant.now())
                   .setFooter("Moderator ID: " + event.getModeratorId(), user.getAvatarUrl())
                   .build();
               log(event.getGuildId(), user.getJDA(), embed);
            });
    }

    private void log(long guildId, JDA jda, MessageEmbed embed) {
        final var loggingChannels = LoggingType.MODERATION_EVENTS.getChannels(guildId);
        loggingChannels
            .forEach(id -> {
                final var ch = id.resolve(idL -> jda.getChannelById(net.dv8tion.jda.api.entities.MessageChannel.class, idL));
                if (ch != null) {
                    ch.sendMessageEmbeds(embed).queue();
                }
            });
    }
}
