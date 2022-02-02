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
package com.mcmoddev.mmdbot.modules.commands.community.server.tricks;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.utilities.tricks.Tricks;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class is the parent of all the trick commands.
 * @author matyrobbrt
 */
public final class CmdTrick extends SlashCommand {

    public CmdTrick() {
        name = "trick";
        help = "Does stuff regarding tricks";

        final List<SlashCommand> child = new ArrayList<>();
        child.add(new CmdRunTrick());
        Tricks.getTrickTypes().entrySet().stream().map(entry -> new CmdAddTrick(entry.getKey(), entry.getValue())).forEach(child::add);
        child.add(new CmdRemoveTrick());
        child.add(new CmdListTricks());
        guildOnly = false;

        children = child.toArray(SlashCommand[]::new);
    }

    @Override
    protected void execute(final SlashCommandEvent event) {

    }

    @Override
    public void onAutoComplete(final CommandAutoCompleteInteractionEvent event) {
        if (Objects.equals(event.getSubcommandName(), "run")) {
            children[0].onAutoComplete(event);
        }
    }
}
