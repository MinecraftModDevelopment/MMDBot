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
package com.mcmoddev.mmdbot.commander.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.paginate.PaginatedCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.time.Instant;
import java.util.List;

/**
 * Show every registered command, or information on a single command.
 * <p>
 * Possible forms:
 * !help
 * !help help
 * !help addquote
 * !help [command]
 *
 * @author Curle
 */
public class HelpCommand extends PaginatedCommand {

    @RegisterSlashCommand
    public static final SlashCommand COMMAND = new HelpCommand();

    private List<Command> commands;

    private static final String NAME = "The Commander";
    public static final String ISSUE_TRACKER = "https://github.com/MinecraftModDevelopment/MMDBot/issues";

    private HelpCommand() {
        super(TheCommander.getComponentListener("help-cmd"), Component.Lifespan.TEMPORARY, 25);
        name = "help";
        help = "Show all commands, or detailed information about a particular command.";
        guildOnly = false;
        options = List.of(new OptionData(OptionType.STRING, "command", "A command to get detailed information on").setRequired(false));
        arguments = "[command]";
    }

    @Override
    public void execute(SlashCommandEvent e) {
        OptionMapping commandName = e.getOption("command");
        final var client = e.getClient();

        commands = client.getCommands();
        commands.addAll(client.getSlashCommands());

        // If no command specified, show all.
        if (commandName == null) {
            sendPaginatedMessage(e, commands.size());
        } else {
            Command command = client.getCommands().stream()
                .filter(com -> com.getName().equals(commandName.getAsString())) // Find the command with the matching name
                .findFirst() // Get the first (should be only) entry
                .orElseGet(HelpCommand::new); // And return it as Command.

            // Build the embed that summarises the command.
            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor(NAME, ISSUE_TRACKER, e.getJDA().getSelfUser().getAvatarUrl());
            embed.setDescription("Command help:");

            embed.addField(command.getName(), command.getHelp(), false);

            // If we have arguments defined and there's content, add it to the embed
            if (command.getArguments() != null && command.getArguments().length() > 0) {
                embed.addField("Arguments", command.getArguments(), false);
            }

            embed.setFooter(NAME, ISSUE_TRACKER).setTimestamp(Instant.now());

            e.replyEmbeds(embed.build()).queue();
        }
    }

    @Override
    protected EmbedBuilder getEmbed(final int index, final int max, final List<String> arguments) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(NAME, ISSUE_TRACKER, TheCommander.getJDA().getSelfUser().getAvatarUrl());
        embed.setDescription("All registered commands:");

        // Embeds have a 25 field limit. We need to make sure we don't exceed that.
        if (commands.size() < getItemsPerPage()) {
            for (Command c : commands)
                embed.addField(c.getName(), c.getHelp(), true);
        } else {
            // Make sure we only go up to the limit.
            for (int i = index; i < index + getItemsPerPage(); i++)
                if (i < commands.size())
                    embed.addField(commands.get(i).getName(), commands.get(i).getHelp(), true);
        }

        embed.setFooter(NAME, ISSUE_TRACKER).setTimestamp(Instant.now());

        return embed;
    }
}

