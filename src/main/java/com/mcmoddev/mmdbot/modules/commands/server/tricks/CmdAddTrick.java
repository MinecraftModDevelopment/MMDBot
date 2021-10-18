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
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.tricks.Tricks;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

/**
 * Adds a trick to the list.
 * <p>
 * Has two subcommands;
 *  - string;
 *      - Takes one parameter - the content of the string trick.
 *  - embed;
 *      - Takes three parameters; name, description and color. All used for constructing the embed.
 *
 * @author williambl
 * @author Curle
 */
public final class CmdAddTrick extends SlashCommand {

    /**
     * Instantiates a new Cmd add trick.
     */
    public CmdAddTrick() {
        super();
        name = "addtrick";
        help = "Adds a new trick, either a string or an embed, if a string you only need the <names> and <body>.";
        category = new Category("Info");
        arguments = "(<string> <trick content body> (or) <embed> <title> "
            + "<description> <colour-as-hex-code>";
        aliases = new String[]{"add-trick"};
        requiredRole = "bot maintainer";
        guildOnly = true;

        children = new SlashCommand[] {
            new AddStringTrick("string", "Create a string-type Trick.", true,
                new OptionData(OptionType.STRING, "names", "Name(s) for the trick. Separate with spaces.").setRequired(true),
                new OptionData(OptionType.STRING, "content", "The content of the string-type Trick.").setRequired(true)),
            new AddEmbedTrick("embed", "Create an embed-type Trick.", true,
                new OptionData(OptionType.STRING, "names", "Name(s) for the trick. Separate with spaces.").setRequired(true),
                new OptionData(OptionType.STRING, "title", "Title of the embed.").setRequired(true),
                new OptionData(OptionType.STRING, "description", "Description of the embed.").setRequired(true),
                new OptionData(OptionType.STRING, "color", "Hex color string in #AABBCC format, used for the embed.").setRequired(true))
        };
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

        String command = event.getSubcommandName();
        String arguments = command.equals("string") ?
            event.getOption("names").getAsString() + " | " + event.getOption("content").getAsString() :
            event.getOption("names").getAsString() + " | " + event.getOption("title").getAsString() + " | " + event.getOption("description").getAsString() + " | " + event.getOption("color").getAsString();


        try {
            Tricks.addTrick(Tricks.getTrickType(command).createFromArgs(arguments));
            event.reply("Added trick!").mentionRepliedUser(false).setEphemeral(true).queue();
        } catch (IllegalArgumentException e) {
            event.reply("A command with that name already exists!").mentionRepliedUser(false).setEphemeral(true).queue();
            MMDBot.LOGGER.warn("Failure adding trick: {}", e.getMessage());
        }
    }

    /**
     * A child command of AddTrick.
     * Handles adding string tricks.
     *
     * Takes the form:
     *  /addtrick string name1 name2 test something
     *  /addtrick string [name <name...> ] [content]
     *
     * @author Curle
     */
    private class AddStringTrick extends SlashCommand {
        public AddStringTrick(String name, String help, boolean guildOnly, OptionData... options) {
            this.name = name;
            this.help = help;
            this.guildOnly = guildOnly;
            this.options = List.of(options);
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!Utils.checkCommand(this, event)) {
                return;
            }

            String arguments = event.getOption("names").getAsString() + " | " + event.getOption("content").getAsString();

            try {
                Tricks.addTrick(Tricks.getTrickType("string").createFromArgs(arguments));
                event.reply("Added trick!").mentionRepliedUser(false).setEphemeral(true).queue();
            } catch (IllegalArgumentException e) {
                event.reply("A command with that name already exists!").mentionRepliedUser(false).setEphemeral(true).queue();
                MMDBot.LOGGER.warn("Failure adding trick: {}", e.getMessage());
            }
        }
    }

    /**
     * A child command of AddTrick.
     * Handles adding embed tricks.
     *
     * Takes the form:
     *  /addtrick embed name1 name2 test something #AABBCC
     *  /addtrick embed [name <name...> ] [title] [description] [color]
     *
     * TODO: Fields.
     *
     * @author Curle
     */
    private class AddEmbedTrick extends SlashCommand {
        public AddEmbedTrick(String name, String help, boolean guildOnly, OptionData... options) {
            this.name = name;
            this.help = help;
            this.guildOnly = guildOnly;
            this.options = List.of(options);
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!Utils.checkCommand(this, event)) {
                return;
            }

            String arguments = event.getOption("names").getAsString() + " | " + event.getOption("title").getAsString() + " | " + event.getOption("description").getAsString() + " | " + event.getOption("color").getAsString();

            try {
                Tricks.addTrick(Tricks.getTrickType("embed").createFromArgs(arguments));
                event.reply("Added trick!").mentionRepliedUser(false).setEphemeral(true).queue();
            } catch (IllegalArgumentException e) {
                event.reply("A command with that name already exists!").mentionRepliedUser(false).setEphemeral(true).queue();
                MMDBot.LOGGER.warn("Failure adding trick: {}", e.getMessage());
            }
        }
    }
}
