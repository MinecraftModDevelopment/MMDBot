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
package com.mcmoddev.mmdbot.modules.commands.bot.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.References;
import com.mcmoddev.mmdbot.modules.commands.CommandModule;
import com.mcmoddev.mmdbot.modules.commands.community.PaginatedCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.time.Instant;
import java.util.Collections;
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
public class CmdHelp extends PaginatedCommand {

    private List<Command> commands;
    private static ButtonListener helpListener;

    public CmdHelp() {
        super("help",
            "Show all commands, or detailed information about a particular command.",
            false,
            Collections.singletonList(new OptionData(OptionType.STRING, "command", "A command to get detailed information on").setRequired(false)),
            25);

        arguments = "[command]";
        this.listener = new PaginatedCommand.ButtonListener();
        helpListener = this.listener;
    }

    /**
     * Returns the instance of our button listener.
     * Used for handling the pagination buttons.
     */
    public static ButtonListener getListener() {
        return helpListener;
    }

    /**
     * Prepare the potential scrolling buttons for a help command,
     * and send the message with the proper embeds.
     * <p>
     * See {@link #getEmbed(int)} for the implementation.
     */
    public void execute(SlashCommandEvent e) {
        OptionMapping commandName = e.getOption("command");

        commands = CommandModule.getCommandClient().getCommands();
        commands.addAll(CommandModule.getCommandClient().getSlashCommands());
        updateMaximum(commands.size());

        // If no command specified, show all.
        if (commandName == null) {
            sendPaginatedMessage(e);
        } else {
            Command command = CommandModule.getCommandClient().getCommands().stream()
                .filter(com -> com.getName().equals(commandName.getAsString())) // Find the command with the matching name
                .findFirst() // Get the first (should be only) entry
                .orElseGet(CmdHelp::new); // And return it as Command.

            // Build the embed that summarises the command.
            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor(References.NAME, References.ISSUE_TRACKER, MMDBot.getInstance().getSelfUser().getAvatarUrl());
            embed.setDescription("Command help:");

            embed.addField(command.getName(), command.getHelp(), false);

            // If we have arguments defined and there's content, add it to the embed
            if (command.getArguments() != null && command.getArguments().length() > 0) {
                embed.addField("Arguments", command.getArguments(), false);
            }

            embed.setFooter(References.NAME).setTimestamp(Instant.now());

            e.replyEmbeds(embed.build()).queue();
        }
    }

    /**
     * Given a starting index, build an embed that we can display for users
     * to summarise all available commands.
     * Intended to be used with pagination in the case of servers with LOTS of commands.
     */
    @Override
    protected EmbedBuilder getEmbed(int index) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(References.NAME, References.ISSUE_TRACKER, MMDBot.getInstance().getSelfUser().getAvatarUrl());
        embed.setDescription("All registered commands:");

        // Embeds have a 25 field limit. We need to make sure we don't exceed that.
        if (commands.size() < items_per_page) {
            for (Command c : commands)
                embed.addField(c.getName(), c.getHelp(), true);
        } else {
            // Make sure we only go up to the limit.
            for (int i = index; i < index + items_per_page; i++)
                if (i < commands.size())
                    embed.addField(commands.get(i).getName(), commands.get(i).getHelp(), true);
        }

        embed.setFooter(References.NAME).setTimestamp(Instant.now());

        return embed;
    }
}

