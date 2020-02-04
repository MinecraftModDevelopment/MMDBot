package com.mcmoddev.bot.events.users;

import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.misc.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
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
		final Long guildId = guild.getIdLong();
		final TextChannel channel = guild.getTextChannelById(MMDBot.getConfig().getChannelIDBasicEvents().toString());
		final Member member = guild.getMember(user);

		if (MMDBot.getConfig().getGuildID().equals(guildId)) {
			final List<Role> roles = Utils.getOldUserRoles(guild, user.getIdLong());
			if (roles != null && member != null) {
				for (Role role : roles) {
					try {
						guild.addRoleToMember(member, role).queue();
					} catch (final HierarchyException e) {
						MMDBot.LOGGER.info("Unable to give member {} role {}: {}", member.getId(), role.getId(), e.getMessage());
						final Message consoleMessage = new MessageBuilder().appendFormat("Unable to give member %s role %s: %s", member.getAsMention(), role.getAsMention(), e.getMessage()).build();
						final TextChannel consoleChannel = guild.getTextChannelById(MMDBot.getConfig().getChannelIDConsole());
						if (consoleChannel != null) consoleChannel.sendMessage(consoleMessage).queue();
					}
				}
			}
			embed.setColor(Color.GREEN);
			embed.setTitle("User Joined");
			embed.setThumbnail(user.getEffectiveAvatarUrl());
			embed.addField("User:", user.getName() + " #" + user.getDiscriminator(), true);
			embed.addField("User ID:", user.getId(), true);
			embed.addField("Roles:", roles.stream().map(IMentionable::getAsMention).collect(Collectors.joining()), true);
			embed.setTimestamp(Instant.now());

			channel.sendMessage(embed.build()).queue();
		}
	}
}
