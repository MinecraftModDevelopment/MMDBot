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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The bulk of the Search commands functions live here to be shared between all other commands.
 *
 * @author Unknown
 * @author Curle
 */
public final class CmdSearch extends SlashCommand {

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

        OptionData data = new OptionData(OptionType.STRING, "text", "The text to search").setRequired(true);
        List<OptionData> dataList = new ArrayList<>();
        dataList.add(data);
        this.options = dataList;
    }

    /**
     * Execute.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    protected void execute(final SlashCommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        try {
            final String query = URLEncoder.encode(event.getOption("text").getAsString(), StandardCharsets.UTF_8.toString());
            event.reply(baseUrl + query).mentionRepliedUser(false).queue();
        } catch (UnsupportedEncodingException ex) {
            MMDBot.LOGGER.error("Error processing search query {}: {}", event.getOption("text").getAsString(), ex);
            event.reply("There was an error processing your command.").mentionRepliedUser(false).setEphemeral(true).queue();
        }

    }
}
