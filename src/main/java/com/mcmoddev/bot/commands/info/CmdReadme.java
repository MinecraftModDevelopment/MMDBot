package com.mcmoddev.bot.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.MMDBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.time.Instant;

/**
 *
 */
public final class CmdReadme extends Command {

	/**
	 *
	 */
	private static final String BODY =
		"Please give <#" + MMDBot.getConfig().getChannelIDReadme() + "> a thorough read, **including** "
			+ "the full text of the Code of Conduct, which is linked there. "
			+ "Having everyone read and understand these rules and guidelines helps keep this server "
			+ "functioning well as a space for collaboration and discussion. Thank you.";

	/**
	 *
	 */
	public CmdReadme() {
		super();
		name = "readme";
		aliases = new String[]{"read-me"};
		help = "Tells you to read the readme.";
	}

	/**
	 *
	 */
	@Override
	protected void execute(final CommandEvent event) {
		final EmbedBuilder embed = new EmbedBuilder();
		final TextChannel channel = event.getTextChannel();

		embed.setTitle("Please read the readme.");
		embed.setDescription(BODY);
		embed.setColor(Color.RED);
		embed.setTimestamp(Instant.now());
		channel.sendMessage(embed.build()).queue();
	}
}
