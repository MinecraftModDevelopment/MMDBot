package com.mcmoddev.bot.commands.unlocked.search;

import java.util.Locale;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.MMDBot;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 *
 */
public final class CmdDuckDuckGo extends Command {

	/**
	 *
	 */
    public CmdDuckDuckGo() {
        super();
        name = "duckduckgo";
        help = "Search for something with a bit more privacy using Duck Duck Go";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        final TextChannel channel = event.getTextChannel();
        final String searchTerm = event.getMessage().getContentRaw().toLowerCase(Locale.ENGLISH)
        		.replace(MMDBot.getConfig().getPrefix() + "duckduckgo ", "");
        final String searchQuery = "<https://duckduckgo.com/?q=" + searchTerm.replace(" ", "+") + ">";

        channel.sendMessage(searchQuery).queue();
    }
}
