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
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.tricks.Tricks;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.Locale;

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
 * @author matyrobbrt
 */
public final class CmdRemoveTrick extends SlashCommand {

    /**
     * Instantiates a new Cmd remove trick.
     */
    public CmdRemoveTrick() {
        super();
        name = "remove";
        help = "Removes a trick";
        category = new Category("Management");
        arguments = "<trick_name>";
        guildOnly = true;
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

        if (!event.isFromGuild()) {
            event.deferReply(true).setContent("This command only works in a guild!").queue();
            return;
        }

        if (!Utils.memberHasRole(event.getMember(), MMDBot.getConfig().getRole("bot_maintainer"))) {
            event.deferReply(true).setContent("Only Bot Maintainers can use this command.").queue();
            return;
        }

        Tricks.getTrick(Utils.getOrEmpty(event, "trick")).ifPresent(Tricks::removeTrick);
        event.reply("Removed trick!").setEphemeral(true).queue();
    }

    @Override
    public void onAutoComplete(final CommandAutoCompleteInteractionEvent event) {
        final var currentChoice = event.getInteraction().getFocusedOption().getValue().toLowerCase(Locale.ROOT);
        event.replyChoices(CmdRunTrick.getNamesStartingWith(currentChoice, 5)).queue();
    }
}
