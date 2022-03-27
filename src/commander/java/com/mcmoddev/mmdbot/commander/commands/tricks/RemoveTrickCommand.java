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

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.tricks.Tricks;
import com.mcmoddev.mmdbot.commander.util.TheCommanderUtilities;
import com.mcmoddev.mmdbot.core.event.Events;
import com.mcmoddev.mmdbot.core.event.customlog.TrickEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
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
public final class RemoveTrickCommand extends SlashCommand {

    /**
     * Instantiates a new Cmd remove trick.
     */
    public RemoveTrickCommand() {
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
        if (!event.isFromGuild()) {
            event.deferReply(true).setContent("This command only works in a guild!").queue();
            return;
        }

        if (!TheCommander.getInstance().getGeneralConfig().features().tricks().tricksEnabled()) {
            event.deferReply(true).setContent("Tricks are not enabled!").queue();
            return;
        }

        if (!TheCommanderUtilities.memberHasRoles(event.getMember(), AddTrickCommand.BOT_MAINTAINERS_GETTER.get())) {
            event.deferReply(true).setContent("Only Bot Maintainers can use this command.").queue();
            return;
        }
        final var name = event.getOption("trick", "", OptionMapping::getAsString);

        Tricks.getTrick(name).ifPresentOrElse(trick -> {
            Tricks.removeTrick(trick);
            event.reply("Removed trick!").setEphemeral(false).queue();
            Events.CUSTOM_AUDIT_LOG_BUS.post(new TrickEvent.Remove(
                event.getGuild().getIdLong(),
                event.getUser().getIdLong(),
                Tricks.getTrickTypeName(trick.getType()),
                trick.getNames(),
                trick.getRaw()
            ));
        }, () -> event.deferReply(true).setContent("Unknown trick: " + name).queue());
    }

    @Override
    public void onAutoComplete(final CommandAutoCompleteInteractionEvent event) {
        final var currentChoice = event.getInteraction().getFocusedOption().getValue().toLowerCase(Locale.ROOT);
        event.replyChoices(RunTrickCommand.getNamesStartingWith(currentChoice, 5)).queue();
    }
}
