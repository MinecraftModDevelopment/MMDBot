package com.mcmoddev.mmdbot.events.users;

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
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
        final TextChannel channel = guild.getTextChannelById(MMDBot.getConfig().getChannel("events.basic"));
        if (channel == null) return;
        final Member member = guild.getMember(user);

        if (MMDBot.getConfig().getGuildID() == guildId) {
            final List<Role> roles = Utils.getOldUserRoles(guild, user.getIdLong());
            if (member != null) {
                for (Role role : roles) {
                    try {
                        guild.addRoleToMember(member, role).queue();
                    } catch (final HierarchyException e) {
                        MMDBot.LOGGER.info("Unable to give member {} role {}: {}", member.getId(), role.getId(), e.getMessage());
                        final Message consoleMessage = new MessageBuilder().appendFormat("Unable to give member %s role %s: %s", member.getAsMention(), role.getAsMention(), e.getMessage()).build();
                        final TextChannel consoleChannel = guild.getTextChannelById(MMDBot.getConfig().getChannel("console"));
                        if (consoleChannel != null) {
							consoleChannel.sendMessage(consoleMessage).queue();
						}
                    }
                }
            }
            embed.setColor(Color.GREEN);
            embed.setTitle("User Joined");
            embed.setThumbnail(user.getEffectiveAvatarUrl());
            embed.addField("User:", user.getAsTag(), true);
            embed.addField("User ID:", user.getId(), true);
            //TODO Check if this works.
			embed.addField("Roles:", roles.stream().map(IMentionable::getAsMention).collect(Collectors.joining()), true);
			embed.setTimestamp(Instant.now());

            channel.sendMessage(embed.build()).queue();
        }
    }
}
