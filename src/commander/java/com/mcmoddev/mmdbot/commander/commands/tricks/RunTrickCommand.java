/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.tricks.Trick;
import com.mcmoddev.mmdbot.commander.tricks.TrickContext;
import com.mcmoddev.mmdbot.commander.tricks.Tricks;
import com.mcmoddev.mmdbot.core.util.StringUtilities;
import com.mcmoddev.mmdbot.core.util.event.DismissListener;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
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
public final class RunTrickCommand extends SlashCommand {

    /**
     * Instantiates a new trick-running command
     */
    public RunTrickCommand() {
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
        if (!TheCommander.getInstance().getGeneralConfig().features().tricks().tricksEnabled()) {
            event.deferReply(true).setContent("Tricks are not enabled!").queue();
            return;
        }
        final var name = event.getOption("name", "", OptionMapping::getAsString);
        event.deferReply().queue(hook -> {
            Tricks.getTrick(name).ifPresentOrElse(
                trick -> trick.execute(new TrickContext.Slash(event, hook, event.getOption("args", "", OptionMapping::getAsString).split(" "))),
                () -> {
                    final var closesMatch = StringUtilities.closestMatch(name, Tricks.getAllNames())
                        .map(" Did you mean **%s**?"::formatted)
                        .orElse("");
                    hook.editOriginal("No trick with that name was found.%s".formatted(closesMatch))
                        .setActionRow(DismissListener.createDismissButton(event)).queue();
                }
            );
        });
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

    public static final class Prefix extends com.jagrosh.jdautilities.command.Command {

        private final String trickName;

        public Prefix(Trick trick) {
            this.name = trick.getNames().get(0);
            this.trickName = name;
            this.aliases = trick.getNames().toArray(String[]::new);
            help = "Invokes the trick " + trickName;
        }

        @Override
        protected void execute(final CommandEvent event) {
            if (!TheCommander.getInstance().getGeneralConfig().features().tricks().tricksEnabled()) {
                event.reply("Tricks are not enabled!");
                return;
            }
            Tricks.getTrick(trickName).ifPresentOrElse(trick -> trick.execute(new TrickContext.Normal(event, event.getArgs().split(" "))),
                () -> event.getMessage().reply("This trick does not exist anymore!").queue());
        }
    }
}
