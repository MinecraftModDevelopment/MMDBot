package com.mcmoddev.mmdbot.commands.staff;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 *
 */
public class CmdUser extends Command {

	/**
	 *
	 */
	public CmdUser() {
		super();
		name = "user";
		aliases = new String[]{"whois", "userinfo"};
		help = "Get information about another user with their user ID.";
		hidden = true;
	}

	/**
	 *
	 */
	@Override
	protected void execute(final CommandEvent event) {
		final TextChannel channel = event.getTextChannel();
		final Member member = Utils.getMemberFromString(event.getArgs(), event.getGuild());
		final EmbedBuilder embed = createMemberEmbed(member);
		channel.sendMessage(embed.build()).queue();
	}

	protected EmbedBuilder createMemberEmbed(final Member member) {
		final User user = member.getUser();
		final EmbedBuilder embed = new EmbedBuilder();
		final Instant dateJoinedDiscord = member.getTimeCreated().toInstant();
		final Instant dateJoinedMMD = Utils.getMemberJoinTime(member);

		embed.setTitle("User info");
		embed.setColor(Color.WHITE);
		embed.setThumbnail(user.getEffectiveAvatarUrl());
		embed.addField("Username:", user.getName(), true);
		embed.addField("Users discriminator:", "#" + user.getDiscriminator(), true);
		embed.addField("Users id:", member.getId(), true);

		if (member.getNickname() != null) {
			embed.addField("Users nickname:", member.getNickname(), true);
		} else {
			embed.addField("Users nickname:", "No nickname applied.", true);
		}

		final SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
		embed.addField("Joined Discord:", date.format(dateJoinedDiscord.toEpochMilli()), true);
		embed.addField("Joined MMD:", date.format(dateJoinedMMD.toEpochMilli()), true);
		embed.addField("Member for:", Utils.getTimeDifference(Utils.getLocalTime(dateJoinedMMD), LocalDateTime.now()), true);
		embed.setTimestamp(Instant.now());

		return embed;
	}
}
