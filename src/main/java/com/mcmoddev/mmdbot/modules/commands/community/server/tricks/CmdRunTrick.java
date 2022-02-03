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
import com.mcmoddev.mmdbot.modules.commands.DismissListener;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.tricks.Tricks;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.Locale;

/**
 * Fetch and execute a given trick.
 * Takes one or two parameters: the trick name, and optionally any arguments to be passed to the trick.
 * <p>
 * Takes the form:
 * /trick test
 * /trick [name] [args]
 *
 * @author Will BL
 * @author Curle
 * @author matyrobbrt
 */
public final class CmdRunTrick extends SlashCommand {

    /**
     * Instantiates a new trick-running command
     */
    public CmdRunTrick() {
        super();
        name = "run";
        help = "Invoke a specific trick by name.";

        options = List.of(
            new OptionData(OptionType.STRING, "name", "The name of the trick to run").setRequired(true).setAutoComplete(true),
            new OptionData(OptionType.STRING, "args", "The arguments for the trick, if any").setRequired(false)
        );
    }

    /**
     * Executes the command.
     *
     * @param event the slash command event
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        Tricks.getTrick(Utils.getOrEmpty(event, "name")).ifPresentOrElse(
            trick -> event.reply(trick.getMessage(Utils.getOrEmpty(event, "args").split(" "))).addActionRow(DismissListener.createDismissButton(event.getUser())).setEphemeral(false).queue(),
            () -> event.reply("No trick with that name was found.").setEphemeral(true).queue()
        );
    }

    @Override
    public void onAutoComplete(final CommandAutoCompleteInteractionEvent event) {
        final var currentChoice = event.getInteraction().getFocusedOption().getValue().toLowerCase(Locale.ROOT);
        event.replyChoices(getNamesStartingWith(currentChoice, 5)).queue();
    }

    public static List<Command.Choice> getNamesStartingWith(final String currentChoice, final int limit) {
        return Tricks.getTricks().stream().filter(t -> t.getNames().get(0).startsWith(currentChoice))
            .limit(limit).map(t -> new Command.Choice(t.getNames().get(0), t.getNames().get(0))).toList();
    }
}
