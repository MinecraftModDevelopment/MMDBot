package com.mcmoddev.mmdbot.events.users;

import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;
import static com.mcmoddev.mmdbot.logging.MMDMarkers.EVENTS;

/**
 *
 */
public final class EventUserLeft extends ListenerAdapter {

    /**
     *
     */
    @Override
    public void onGuildMemberRemove(final GuildMemberRemoveEvent event) {
        final User user = event.getUser();
        final EmbedBuilder embed = new EmbedBuilder();
        final Guild guild = event.getGuild();
        final long guildId = guild.getIdLong();
        final TextChannel channel = guild.getTextChannelById(getConfig().getChannel("events.basic"));
        if (channel == null) return;
        final Member member = event.getMember();

        if (getConfig().getGuildID() == guildId) {
            LOGGER.info(EVENTS, "User {} left the guild", user.getId());
            List<Role> roles = new ArrayList<>();
            if (member != null) {
                roles = member.getRoles();
            } else {
                LOGGER.warn(EVENTS, "Could not get roles of leaving user " + user.getAsTag() + ": " + user.getId());
            }
            Utils.writeUserRoles(user.getIdLong(), roles);
            if (member != null) {
                Utils.writeUserJoinTimes(user.getId(), member.getTimeJoined().toInstant());
            }
            embed.setColor(Color.RED);
            embed.setTitle("User Left");
            embed.setThumbnail(user.getEffectiveAvatarUrl());
            embed.addField("User:", user.getAsTag(), true);
            embed.addField("User ID:", user.getId(), true);
            //TODO Check if this works.
            if (!roles.isEmpty()) {
                embed.addField("Roles:", roles.stream().map(IMentionable::getAsMention).collect(Collectors.joining()), true);
            } else {
                embed.addField("Roles:", "Users roles currently unobtainable.", true);
            }
            embed.setTimestamp(Instant.now());

            channel.sendMessage(embed.build()).queue();
        }
    }
}
