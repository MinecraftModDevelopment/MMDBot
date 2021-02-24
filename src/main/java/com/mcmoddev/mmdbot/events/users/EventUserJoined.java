package com.mcmoddev.mmdbot.events.users;

import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;
import static com.mcmoddev.mmdbot.logging.MMDMarkers.EVENTS;

/**
 *
 */
public final class EventUserJoined extends ListenerAdapter {

    /**
     *
     */
    @Override
    public void onGuildMemberJoin(final GuildMemberJoinEvent event) {
        final User user = event.getUser();
        final EmbedBuilder embed = new EmbedBuilder();
        final Guild guild = event.getGuild();
        final long guildId = guild.getIdLong();
        final TextChannel channel = guild.getTextChannelById(getConfig().getChannel("events.basic"));
        if (channel == null) return;
        final Member member = guild.getMember(user);

        if (getConfig().getGuildID() == guildId) {
            LOGGER.info(EVENTS, "User {} joined the guild", user);
            final List<Role> roles = Utils.getOldUserRoles(guild, user.getIdLong());
            if (member != null && !roles.isEmpty()) {
                LOGGER.info(EVENTS, "Giving old roles to user {}: {}", user, roles);
                for (Role role : roles) {
                    try {
                        guild.addRoleToMember(member, role).queue();
                    } catch (final HierarchyException e) {
                        LOGGER.warn(EVENTS, "Unable to give member {} role {}: {}", member, role, e.getMessage());
                    }
                }
            }
            embed.setColor(Color.GREEN);
            embed.setTitle("User Joined");
            embed.setThumbnail(user.getEffectiveAvatarUrl());
            embed.addField("User:", user.getAsTag(), true);
            embed.addField("User ID:", user.getId(), true);
            //TODO Check if this works.
            if (!roles.isEmpty()) {
                embed.addField("Roles:", roles.stream().map(IMentionable::getAsMention).collect(Collectors.joining()), true);
            }
            embed.setTimestamp(Instant.now());

            channel.sendMessage(embed.build()).queue();
        }
    }
}
