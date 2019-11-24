package com.mcmoddev.bot.commands.unlocked.search;

import java.util.Locale;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.MMDBot;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 *
 */
public final class CmdLmgtfy extends Command {

	/**
     *
     */
    public CmdLmgtfy() {
        super();
        name = "lmgtfy";
        aliases = new String[0];
        help = "Assist someone of the restful variety in searching for something.";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        final TextChannel channel = event.getTextChannel();
        final String searchTerm = event.getMessage().getContentRaw().toLowerCase(Locale.ENGLISH)
        		.replace(MMDBot.getConfig().getPrefix() + "lmgtfy", "");
        final String searchQuery = "<https://lmgtfy.com/?q=" + searchTerm.replace(" ", "+") + ">";

        channel.sendMessage(searchQuery).queue();
    }
}
