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
    @SuppressWarnings("unused")
    @Override
    protected void execute(final CommandEvent event) {
	final String preview = "&pp=1"; // Use the preview page (For debug only)
        final String google = "";
        final String lmgtfy = "&s=l";
        final String yahoo = "&s=y";
        final String bing = "&s=b";
        final String startpage = "&s=t";
        final String ask = "&s=k";
        final String aol = "&s=a";
        final String duckduckgo = "&s=d";
	final String explainer = "&iie=1"; // Include the internet Explainer
        final TextChannel channel = event.getTextChannel();
        final String searchTerm = event.getMessage().getContentRaw().toLowerCase(Locale.ENGLISH)
        		.replace(MMDBot.getConfig().getPrefix() + "lmgtfy", "");
        final String searchQuery = "<https://lmgtfy.com/?q=" + searchTerm.replace(" ", "+") + ">";

        channel.sendMessage(searchQuery).queue();
    }
}
