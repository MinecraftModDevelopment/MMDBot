package com.mcmoddev.bot.commands.unlocked.search;

import java.util.Locale;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.MMDBot;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 *
 */
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
