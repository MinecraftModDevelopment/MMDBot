package com.mcmoddev.mmdbot.modules.commands.general.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * The bulk of the Search commands functions live here to be shared between all other commands.
 *
 * @author
 */
public final class CmdSearch extends Command {

    /**
     * The search provider we want to generate a URL for.
     */
    private final String baseUrl;

    /**
     * Instantiates a new Cmd search.
     *
     * @param name      The command's/search engine's name.
     * @param baseUrlIn The base URL of the search provider.
     * @param aliases   the aliases
     */
    public CmdSearch(final String name, final String baseUrlIn, final String... aliases) {
        super();
        this.name = name.toLowerCase(Locale.ROOT);
        this.aliases = aliases;
        this.help = "Search for something using " + name + ".";
        this.baseUrl = baseUrlIn;
    }

    /**
     * Execute.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }
        final MessageChannel channel = event.getChannel();
        if (event.getArgs().isEmpty()) {
            channel.sendMessage("No arguments given!").queue();
            return;
        }

        try {
            final String query = URLEncoder.encode(event.getArgs(), StandardCharsets.UTF_8.toString());
            channel.sendMessage(baseUrl + query).queue();
        } catch (UnsupportedEncodingException ex) {
            MMDBot.LOGGER.error("Error processing search query {}: {}", event.getArgs(), ex);
            channel.sendMessage("There was an error processing your command.").queue();
        }

    }
}
