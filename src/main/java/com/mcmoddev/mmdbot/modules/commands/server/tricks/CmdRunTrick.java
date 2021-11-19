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
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.modules.commands.CommandModule;
import com.mcmoddev.mmdbot.modules.commands.server.DeletableCommand;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.tricks.Trick;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 * Runs a certain trick.
 * Takes zero or one parameters, the argument string to be passed to the trick.
 * <p>
 * Takes the form:
 * /test
 * /[trickname] [args]
 *
 * @author Will BL
 * @author Curle
 */
public final class CmdRunTrick extends SlashCommand implements DeletableCommand {

    private final Trick trick;
    private boolean deleted = false;

    /**
     * Instantiates a new command for a certain trick.
     */
    public CmdRunTrick(Trick trick) {
        super();
        this.trick = trick;
        name = trick.getNames().get(0);
        aliases = trick.getNames().toArray(new String[0]);
        help = "Invoke the trick " + trick.getNames().get(0);
        category = new Category("Fun");
        guildOnly = true;
        // we need to use this unfortunately :( can't create more than one commandclient
        guildId = Long.toString(MMDBot.getConfig().getGuildID());

        options = Collections.singletonList(new OptionData(OptionType.STRING, "args", "The arguments for the trick, if any").setRequired(false));
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

        event.reply(trick.getMessage(Utils.getOrEmpty(event, "args").split(" "))).setEphemeral(false).queue();
    }

    @Override
    public void delete() {
        deleted = true;
    }

    @Override
    public void restore() {
        deleted = false;
        CommandModule.upsertCommand(this);
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }
}
