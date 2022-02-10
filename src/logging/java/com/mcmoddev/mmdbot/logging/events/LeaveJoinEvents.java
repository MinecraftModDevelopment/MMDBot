package com.mcmoddev.mmdbot.logging.events;

import com.mcmoddev.mmdbot.logging.LoggingBot;
import com.mcmoddev.mmdbot.logging.util.ListenerAdapter;
import com.mcmoddev.mmdbot.logging.util.LoggingType;
import com.mcmoddev.mmdbot.logging.util.Utils;
import com.oracle.truffle.api.ArrayUtils;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.PartialMember;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.util.ArrayUtil;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Arrays;

public final class LeaveJoinEvents extends ListenerAdapter {

    @Override
    public void onMemberJoin(final MemberJoinEvent event) {
        final var embed = EmbedCreateSpec.builder()
            .timestamp(Instant.now())
            .title("User Joined")
            .footer("User ID: " + event.getMember().getId().asLong(), null)
            .addField("User:", event.getMember().getTag(), true)
            .thumbnail(event.getMember().getAvatarUrl())
            // TODO add a discord join time if we find that
            .build();

        Utils.executeInLoggingChannel(event.getGuildId(), LoggingType.LEAVE_JOIN_EVENTS, c ->
            c.createMessage(embed.asRequest()).subscribe());
    }

    @Override
    public void onMemberLeave(final MemberLeaveEvent event) {
        final var embed = EmbedCreateSpec.builder()
            .timestamp(Instant.now())
            .title("User Left")
            .footer("User ID: " + event.getUser().getId().asLong(), null)
            .addField("User:", event.getUser().getTag(), true)
            .addField("Join Time:", event.getMember().flatMap(PartialMember::getJoinTime)
                .map(i -> "<t:%s:f>".formatted(i.getEpochSecond())).orElse("Join time could not be determined!"), true)
            .addField("Roles", Arrays.toString(event.getMember().map(Member::getRoles)
                .map(roleFlux -> roleFlux.map(Role::getMention)).orElse(Flux.empty()).toStream().toArray(String[]::new)), false)
            .thumbnail(event.getUser().getAvatarUrl())
            .build();

        Utils.executeInLoggingChannel(event.getGuildId(), LoggingType.LEAVE_JOIN_EVENTS, c ->
            c.createMessage(embed.asRequest()).subscribe());
    }
}
