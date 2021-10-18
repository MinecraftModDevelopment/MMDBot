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
package com.mcmoddev.mmdbot.modules.commands.server.tricks;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.tricks.Tricks;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 * Fetch and execute a given trick.
 * Takes one parameter, the trick name.
 * <p>
 * Takes the form:
 * /trick test
 * /trick [name]
 *
 * @author williambl
 * @author Curle
 */
public final class CmdRunTrick extends SlashCommand {

    /**
     * Instantiates a new Cmd run trick.
     */
    public CmdRunTrick() {
        super();
        name = "trick";
        help = "Invoke a specific trick by name.";
        category = new Category("Fun");
        guildOnly = true;

        options = Collections.singletonList(new OptionData(OptionType.STRING, "name", "The name of the trick to run").setRequired(true));
    }

    /**
     * Execute.
     *
     * @param event the event
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        Tricks.getTrick(event.getOption("name").getAsString()).ifPresentOrElse(
            trick -> event.reply(trick.getMessage(new String[]{})).setEphemeral(false).queue(),
            () -> event.reply("No trick with that name was found.").setEphemeral(true).queue()
        );
    }
}
