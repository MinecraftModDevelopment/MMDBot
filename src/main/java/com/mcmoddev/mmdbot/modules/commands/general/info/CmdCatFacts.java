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

import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * The type Cmd cat facts.
 *
 * @author
 */
public final class CmdCatFacts extends Command {

    /**
     * The constant random.
     */
    private static final Random RANDOM = new Random();

    /**
     * Instantiates a new Cmd cat facts.
     */
    public CmdCatFacts() {
        super();
        name = "catfacts";
        aliases = new String[]{"catfact", "cat-fact", "cat-facts"};
        help = "Get a random fact about cats, you learn something new every day!";
        guildOnly = false;
    }

    /**
     * Gets fact.
     *
     * @return String. fact
     */
    public static String getFact() {
        try {
            final var url = new URL("https://catfact.ninja/fact");
            final URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10 * 1000);
            final var reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            final String inputLine = reader.readLine();
            reader.close();
            final var objectArray = JsonParser.parseString(inputLine).getAsJsonObject();
            return ":cat:  " + objectArray.get("fact").toString();

        } catch (final RuntimeException ex) {
            throw ex;
        } catch (final Exception ex) {
            MMDBot.LOGGER.error("Error getting cat fact...", ex);
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * Execute.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }
        final var embed = new EmbedBuilder();
        final var fact = getFact();
        if (!"".equals(fact)) {
            embed.setColor(RANDOM.nextInt(0x1000000));
            embed.appendDescription(fact);
            embed.setFooter("Puwerrd by https://catfact.ninja");

            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        }
    }
}