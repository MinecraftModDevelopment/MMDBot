/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2021 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
package com.mcmoddev.mmdbot.modules.commands.general.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.Utils;
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
        help = "Search for something using " + name + ".";
        category = new Category("Info");
        arguments = "<search query required>";
        this.baseUrl = baseUrlIn;
        guildOnly = true;
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
        final var channel = event.getMessage();
        if (event.getArgs().isEmpty()) {
            channel.reply("No arguments given!").mentionRepliedUser(false).queue();
            return;
        }

        try {
            final String query = URLEncoder.encode(event.getArgs(), StandardCharsets.UTF_8.toString());
            channel.reply(baseUrl + query).mentionRepliedUser(false).queue();
        } catch (UnsupportedEncodingException ex) {
            MMDBot.LOGGER.error("Error processing search query {}: {}", event.getArgs(), ex);
            channel.reply("There was an error processing your command.").mentionRepliedUser(false).queue();
        }

    }
}
