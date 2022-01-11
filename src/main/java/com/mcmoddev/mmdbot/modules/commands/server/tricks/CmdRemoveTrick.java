/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
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
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.tricks.Tricks;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 * Remove a trick, if present.
 * Takes one parameter, the trick name.
 * <p>
 * Takes the form:
 * /removetrick test
 * /removetrick [trick]
 *
 * @author Will BL
 * @author Curle
 */
public final class CmdRemoveTrick extends SlashCommand {

    /**
     * Instantiates a new Cmd remove trick.
     */
    public CmdRemoveTrick() {
        super();
        name = "removetrick";
        help = "Removes a trick";
        category = new Category("Management");
        arguments = "<trick_name>";
        aliases = new String[]{"remove-trick", "remtrick"};
        enabledRoles = new String[]{Long.toString(MMDBot.getConfig().getRole("bot_maintainer"))};
        guildOnly = true;
        // we need to use this unfortunately :( can't create more than one commandclient
        guildId = Long.toString(MMDBot.getConfig().getGuildID());

        options = Collections.singletonList(new OptionData(OptionType.STRING, "trick", "The trick to delete.").setRequired(true));
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

        Tricks.getTrick(Utils.getOrEmpty(event, "trick")).ifPresent(Tricks::removeTrick);
        event.reply("Removed trick!").setEphemeral(true).queue();
    }
}
