package com.mcmoddev.mmdbot.commands.search;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Locale;

/**
 *
 */
//TODO Remove when the new CmdSearch class has been fixed to replace this.
public final class CmdGoogle extends Command {

	/**
	 *
	 */
	public CmdGoogle() {
		super();
		name = "google";
		help = "Google something rather than load a browser manually then Google it.";
	}

	/**
	 *
	 */
	@Override
	protected void execute(final CommandEvent event) {
		final TextChannel channel = event.getTextChannel();
		final String searchTerm = event.getMessage().getContentRaw().toLowerCase(Locale.ENGLISH)
			.replace(MMDBot.getConfig().getPrefix() + "google ", "");
		final String searchQuery = "<https://google.com/search?q=" + searchTerm.replace(" ", "+") + ">";

		channel.sendMessage(searchQuery).queue();
	}
}
