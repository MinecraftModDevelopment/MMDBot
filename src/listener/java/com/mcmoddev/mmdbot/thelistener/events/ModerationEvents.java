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

import static com.mcmoddev.mmdbot.thelistener.TheListener.getClient;
import static com.mcmoddev.mmdbot.thelistener.util.Utils.mentionAndID;
import com.mcmoddev.mmdbot.core.event.moderation.WarningEvent;
import com.mcmoddev.mmdbot.core.util.Pair;
import com.mcmoddev.mmdbot.thelistener.TheListener;
import com.mcmoddev.mmdbot.thelistener.util.ListenerAdapter;
import com.mcmoddev.mmdbot.thelistener.util.LoggingType;
import com.mcmoddev.mmdbot.thelistener.util.Utils;
import discord4j.common.util.Snowflake;
import discord4j.common.util.TimestampFormat;
import discord4j.core.event.domain.guild.BanEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.event.domain.guild.UnbanEvent;
import discord4j.core.object.audit.ActionType;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.PartialMember;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Color;
import io.github.matyrobbrt.eventdispatcher.SubscribeEvent;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;

public final class ModerationEvents extends ListenerAdapter {
    public static final ModerationEvents INSTANCE = new ModerationEvents();
    private ModerationEvents() {}

    @Override
    public void onBan(final BanEvent event) {
        event.getGuild().subscribe(guild -> {
            Utils.getAuditLog(guild, event.getUser().getId().asLong(), log -> log
                .withLimit(5)
                .withActionType(ActionType.MEMBER_BAN_ADD)
                .withGuild(guild), log -> {
                final var embed = EmbedCreateSpec.builder();
                final var bannedUser = event.getUser();
                final var bannedBy = log.getResponsibleUser();

                embed.color(Color.RED);
                embed.title("User Banned.");
                embed.thumbnail(bannedUser.getAvatarUrl());
                embed.addField("**Name:**", bannedUser.getUsername(), false);
                embed.addField("**User ID:**", bannedUser.getId().asString(), false);
                embed.addField("**Profile:**", bannedUser.getMention(), false);
                embed.addField("**Profile Age**", TimestampFormat.RELATIVE_TIME
                    .format(bannedUser.getId().getTimestamp()), false);

                embed.addField("**Ban reason:**", log.getReason().orElse("Reason for ban was not provided or could not be found, please contact "
                    + bannedBy.map(User::getMention).orElse("the banner.")), false);

                final var targetId = (long) log.getTargetId().map(Snowflake::asLong).orElse(0L);

                if (targetId != bannedUser.getId().asLong()) {
                    TheListener.LOGGER.warn("Inconsistency between target of retrieved audit log "
                            + "entry and actual ban event target: retrieved is {}, but target is {}",
                        targetId, bannedUser);
                } else {
                    embed.addField("Banned By: ", bannedBy.map(u -> "<@%s> (%s)".formatted(u.getId().asString(), u.getId().asLong())).orElse("Unknown"), false);
                }

                bannedBy.ifPresent(u -> embed.footer("Moderator ID: " + u.getId().asString(), u.getAvatarUrl()));

                embed.timestamp(Instant.now());

                Utils.executeInLoggingChannel(event.getGuildId(), LoggingType.MODERATION_EVENTS, c -> c
                    .createMessage(embed.build().asRequest()).subscribe());
            });
        });
    }

    @Override
    public void onUnban(final UnbanEvent event) {
        event.getGuild().subscribe(guild -> {
            Utils.getAuditLog(guild, event.getUser().getId().asLong(), log -> log
                .withActionType(ActionType.MEMBER_BAN_REMOVE)
                .withLimit(5)
                .withGuild(guild), log -> {
                final var embed = EmbedCreateSpec.builder();
                final var bannedUser = event.getUser();
                final var bannedBy = log.getResponsibleUser();

                embed.color(Color.GREEN);
                embed.title("User Un-banned.");
                embed.thumbnail(bannedUser.getAvatarUrl());
                embed.addField("**Name:**", bannedUser.getUsername(), false);
                embed.addField("**User ID:**", bannedUser.getId().asString(), false);
                embed.addField("**Profile:**", bannedUser.getMention(), false);
                embed.addField("**Profile Age**", TimestampFormat.RELATIVE_TIME
                    .format(bannedUser.getId().getTimestamp()), false);

                final var targetId = (long) log.getTargetId().map(Snowflake::asLong).orElse(0L);

                if (targetId != bannedUser.getId().asLong()) {
                    TheListener.LOGGER.warn("Inconsistency between target of retrieved audit log "
                            + "entry and actual unban event target: retrieved is {}, but target is {}",
                        targetId, bannedUser);
                } else {
                    embed.addField("Un-banned By: ", bannedBy.map(u -> "<@%s> (%s)".formatted(u.getId().asString(), u.getId().asLong())).orElse("Unknown"), false);
                }

                bannedBy.ifPresent(u -> embed.footer("Moderator ID: " + u.getId().asString(), u.getAvatarUrl()));

                embed.timestamp(Instant.now());

                Utils.executeInLoggingChannel(event.getGuildId(), LoggingType.MODERATION_EVENTS, c -> c
                    .createMessage(embed.build().asRequest()).subscribe());
            });
        });
    }

    @Override
    public void onMemberUpdate(final MemberUpdateEvent event) {
        event.getMember().map(m -> Pair.makeOptional(event.getOld(), Optional.of(m))).subscribe(pairO -> pairO.ifPresent(pair -> pair.accept((oldMember, newMember) -> {
            Pair.makeOptional(oldMember.getNickname(), newMember.getNickname())
                .ifPresent(p -> p.accept((oldNick, newNick) -> {
                    if (!oldNick.equals(newNick)) {
                        onNickChanged(event, newMember, oldNick, newNick);
                    }
                }));
        })));
    }

    private void onNickChanged(final MemberUpdateEvent event, final Member newMember, final String oldNick, final String newNick) {
        event.getGuild().subscribe(guild -> {
            Utils.getAuditLog(guild, newMember.getId().asLong(), log -> log
                .withActionType(ActionType.MEMBER_UPDATE)
                .withLimit(5)
                .withGuild(guild), entry -> {
                final var embed = EmbedCreateSpec.builder();
                final var targetUser = new User(newMember.getClient(), newMember.getUserData());
                final var editor = entry.getResponsibleUser();

                embed.color(Color.YELLOW);
                embed.title("Nickname Changed");
                embed.thumbnail(newMember.getAvatarUrl());
                embed.addField("User:", targetUser.getMention() + " (" + newMember.getId().asLong() + ")", true);
                embed.timestamp(Instant.now());

                final var targetId = (long) entry.getTargetId().map(Snowflake::asLong).orElse(0L);

                if (targetId != newMember.getId().asLong()) {
                    TheListener.LOGGER.warn("Inconsistency between target of retrieved audit log "
                            + "entry and actual nickname event target: retrieved is {}, but target is {}",
                        targetId, newMember);
                } else {
                    embed.addField("Nickname Editor: ", editor.map(u -> "<@%s> (%s)".formatted(u.getId().asString(), u.getId().asLong())).orElse("Unknown"), false);
                }

                embed.addField("Old Nickname:", oldNick, true);
                embed.addField("New Nickname:", newNick, true);

                Utils.executeInLoggingChannel(event.getGuildId(), LoggingType.MODERATION_EVENTS, c -> c.createMessage(embed.build().asRequest()).subscribe());
            }, () -> {
                final var embed = EmbedCreateSpec.builder();
                final var targetUser = new User(newMember.getClient(), newMember.getUserData());

                embed.color(Color.YELLOW);
                embed.title("Nickname Changed");
                embed.thumbnail(newMember.getAvatarUrl());
                embed.addField("User:", targetUser.getMention() + " (" + newMember.getId().asLong() + ")", true);
                embed.timestamp(Instant.now());
                embed.addField("Nickname Editor: ", "<@%s> (%s)".formatted(targetUser.getId().asString(), targetUser.getId().asLong()), false);

                embed.addField("Old Nickname:", oldNick, true);
                embed.addField("New Nickname:", newNick, true);

                Utils.executeInLoggingChannel(event.getGuildId(), LoggingType.MODERATION_EVENTS, c -> c.createMessage(embed.build().asRequest()).subscribe());
            });
        });
    }

    @Override
    public void onMemberLeave(final MemberLeaveEvent event) {
        event.getGuild().subscribe(guild -> {
            Utils.getAuditLog(guild, event.getUser().getId().asLong(), log -> log
                .withActionType(ActionType.MEMBER_KICK)
                .withLimit(5)
                .withGuild(guild), log -> {
                final var embed = EmbedCreateSpec.builder();
                final var kicker = log.getResponsibleUser();
                final var kickedUser = event.getUser();

                embed.color(Color.RUBY);
                embed.title("User Kicked");
                embed.thumbnail(kickedUser.getAvatarUrl());
                embed.addField("**Name:**", kickedUser.getUsername(), false);
                embed.addField("**User ID:**", kickedUser.getId().asString(), false);
                embed.addField("**Profile:**", kickedUser.getMention(), false);
                embed.addField("**Profile Age**", TimestampFormat.RELATIVE_TIME
                    .format(kickedUser.getId().getTimestamp()), false);

                embed.addField("Guild Join Time:", event.getMember().flatMap(PartialMember::getJoinTime)
                    .map(i -> "<t:%s:f>".formatted(i.getEpochSecond())).orElse("Join time could not be determined!"), true);

                embed.addField("**Kick reason:**", log.getReason().orElse("Reason for kick was not provided or could not be found, please contact "
                    + kicker.map(User::getMention).orElse("the kicker.")), false);

                final var targetId = (long) log.getTargetId().map(Snowflake::asLong).orElse(0L);

                if (targetId != event.getUser().getId().asLong()) {
                    TheListener.LOGGER.warn("Inconsistency between target of retrieved audit log "
                            + "entry and actual kick event target: retrieved is {}, but target is {}",
                        targetId, event.getUser());
                } else {
                    embed.addField("Kicked By: ", kicker.map(u -> "<@%s> (%s)".formatted(u.getId().asString(), u.getId().asLong())).orElse("Unknown"), false);
                }

                kicker.ifPresent(u -> embed.footer("Moderator ID: " + u.getId().asString(), u.getAvatarUrl()));

                Utils.executeInLoggingChannel(event.getGuildId(), LoggingType.MODERATION_EVENTS, c -> c.createMessage(embed.build().asRequest()).subscribe());
            });
        });
    }

    @SubscribeEvent
    public void onWarnAdd(final WarningEvent.Add event) {
        if (getClient() == null) {
            return;
        }
        final var doc = event.getDocument();
        Mono.zip(getClient().getUserById(Snowflake.of(doc.userId())).getData(),
                getClient().getUserById(Snowflake.of(doc.moderatorId())).getData())
            .subscribe(t -> {
                final var user = t.getT1();
                final var warner = t.getT2();
                final var embed = EmbedCreateSpec.builder()
                    .color(Color.RED)
                    .title("New Warning")
                    .description("%s warned %s".formatted(mentionAndID(doc.moderatorId()), mentionAndID(doc.userId())))
                    .thumbnail(user.avatar().map(Possible::of).orElse(Possible.absent()))
                    .addField("Reason:", doc.reason(), false)
                    .addField("Warning ID", doc.warnId(), false)
                    .timestamp(Instant.now())
                    .footer("Warner ID: " + doc.moderatorId(), warner.avatar().orElse(null));
                Utils.executeInLoggingChannel(Snowflake.of(doc.guildId()), LoggingType.MODERATION_EVENTS,
                    c -> c.createMessage(embed.build().asRequest()).subscribe());
            });
    }

    @SubscribeEvent
    public void onWarnClear(final WarningEvent.Clear event) {
        if (getClient() == null) {
            return;
        }
        final var warnDoc = event.getDocument();
        Mono.zip(getClient().getUserById(Snowflake.of(warnDoc.userId())).getData(),
                getClient().getUserById(Snowflake.of(event.getModeratorId())).getData())
            .subscribe(t -> {
                final var user = t.getT1();
                final var moderator = t.getT2();
                final var embed = EmbedCreateSpec.builder()
                    .color(Color.GREEN)
                    .title("Warning Cleared")
                    .description("One of the warnings of " + mentionAndID(warnDoc.userId()) + " has been removed!")
                    .thumbnail(user.avatar().map(Possible::of).orElse(Possible.absent()))
                    .addField("Old warning reason:", warnDoc.reason(), false)
                    .addField("Old warner:", mentionAndID(warnDoc.userId()), false)
                    .timestamp(Instant.now())
                    .footer("Moderator ID: " + event.getModeratorId(), moderator.avatar().orElse(null));
                Utils.executeInLoggingChannel(Snowflake.of(warnDoc.guildId()), LoggingType.MODERATION_EVENTS,
                    c -> c.createMessage(embed.build().asRequest()).subscribe());
            });
    }

    @SubscribeEvent
    public void onWarnClearAll(final WarningEvent.ClearAllWarns event) {
        if (getClient() == null) {
            return;
        }
        getClient().getUserById(Snowflake.of(event.getModeratorId())).getData()
            .subscribe(moderator -> {
                final var embed = EmbedCreateSpec.builder()
                    .color(Color.GREEN)
                    .title("Warnings Cleared")
                    .description("All of the warnings of " + mentionAndID(event.getTargetId()) + " have been cleared!")
                    .timestamp(Instant.now())
                    .footer("Moderator ID: " + event.getModeratorId(), moderator.avatar().orElse(null));
                Utils.executeInLoggingChannel(Snowflake.of(event.getGuildId()), LoggingType.MODERATION_EVENTS,
                    c -> c.createMessage(embed.build().asRequest()).subscribe());
            });
    }
}
