package com.mcmoddev.mmdbot.commands.search;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;

import java.util.Locale;

/**
 * The bulk of the Search commands functions live here to be shared between all other commands.
 */
//TODO Fix up and finish this class before removing the old ones.
public class CmdSearch extends Command {

    /**
     * The search commands name used when triggering the command.
     */
    private String name;

    /**
     * The search provider we want to generate a url for.
     */
    private String searchProvider;

    /**
     * @param name The search commands name used when triggering the command.
     * @param searchProvider The search provider we want to generate a url for.
     */
    public CmdSearch(String name, String searchProvider) {
        super();
        this.name = name;
        this.help = "Search for something using " + name;
        this.searchProvider = searchProvider;
    }

    /**
     * @param event The {@link com.jagrosh.jdautilities.command.CommandEvent CommandEvent} that triggered this Command.
     */
    protected void execute(final CommandEvent event) {
        //The raw command sent by the user.
        final String command = event.getMessage().getContentRaw().toLowerCase(Locale.ROOT);
        //The main command prefix for the bot.
        final String prefix = MMDBot.getConfig().getPrefix();
        //The alternative command prefix for the bot.
        final String alternativePrefix = MMDBot.getConfig().getAlternativePrefix();
        //The thing we are searching for. e.g. Minecraft Mod Development website.

        String searchTerm;

        event.getChannel().sendMessage(command).queue();
        if (command.startsWith(prefix + name)) {
            searchTerm = command.substring(prefix.length() + name.length() + 1)
            .replace(" ", "+");
        } else if (command.startsWith(alternativePrefix + name)) {
            searchTerm = command.substring(alternativePrefix.length() + name.length() + 1)
                    .replace(" ", "+");
        } else {
            event.getChannel().sendMessage(prefix).queue();
            event.getChannel().sendMessage(command).queue();
            return;
        }

        event.getChannel().sendMessage(searchProvider + searchTerm).queue();
    }
}
