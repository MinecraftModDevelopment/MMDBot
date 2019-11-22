package com.mcmoddev.bot.commands.unlocked.search;

import java.util.Locale;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.MMDBot;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 *
 */
public final class CmdBing extends Command {

	/**
	 *
	 */
    public CmdBing() {
        super();
        name = "bing";
        aliases = new String[0];
        help = "Looking for something? Bing! I found it!";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        final TextChannel channel = event.getTextChannel();
        final String searchTerm = event.getMessage().getContentRaw().toLowerCase(Locale.ENGLISH)
        		.replace(MMDBot.getConfig().getPrefix() + "bing ", "");
        final String searchQuery = "<https://www.bing.com/search?q=" + searchTerm.replace(" ", "+") + ">";

        channel.sendMessage(searchQuery).queue();
    }
}
