package com.mcmoddev.mmdbot.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * The bulk of the Search commands functions live here to be shared between all other commands.
 */
public class CmdSearch extends Command {

    /**
     * The search provider we want to generate a url for.
     */
    private final String baseUrl;

    /**
     * @param name    The command's/search engine's name.
     * @param baseUrl The base url of the search provider.
     */
    public CmdSearch(String name, String baseUrl, String... aliases) {
        super();
        this.name = name.toLowerCase(Locale.ROOT);
        this.aliases = aliases;
        this.help = "Search for something using " + name + ".";
        this.baseUrl = baseUrl;
    }

    /**
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    protected void execute(final CommandEvent event) {
		if (!Utils.checkCommand(this, event)) return;
        if (event.getArgs().isEmpty()) {
            event.getChannel().sendMessage("No arguments given!").queue();
            return;
        }

        try {
            final String query = URLEncoder.encode(event.getArgs(), StandardCharsets.UTF_8.toString());
            event.getChannel().sendMessage(baseUrl + query).queue();
        } catch (UnsupportedEncodingException e) {
            MMDBot.LOGGER.error("Error processing search query {}: {}", event.getArgs(), e);
            event.getChannel().sendMessage("There was an error processing your command.").queue();
        }

    }
}
