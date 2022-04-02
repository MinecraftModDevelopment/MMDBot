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
package com.mcmoddev.mmdbot.commander.commands.tricks;

import com.google.common.base.Suppliers;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.commander.tricks.Tricks;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * This class is the parent of all the trick commands. <br>
 *
 * @author matyrobbrt
 */
public final class TrickCommand extends SlashCommand {

    @RegisterSlashCommand // lazy as it has role requirements
    public static final Supplier<SlashCommand> COMMAND = Suppliers.memoize(TrickCommand::new);

    private TrickCommand() {
        name = "trick";
        help = "Does stuff regarding tricks";

        final List<SlashCommand> child = new ArrayList<>();
        child.add(new RunTrickCommand());
        child.add(new RemoveTrickCommand());
        child.add(new RawTrickCommand());
        Tricks.getTrickTypes().entrySet().stream().map(entry -> new AddTrickCommand(entry.getKey(), entry.getValue())).forEach(child::add);
        child.add(new ListTricksCommand());
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
        if (Objects.equals(event.getSubcommandName(), "remove")) {
            children[1].onAutoComplete(event);
        }
        if (Objects.equals(event.getSubcommandName(), "raw")) {
            children[2].onAutoComplete(event);
        }
    }
}
