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

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.tricks.Tricks;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.requests.Route.Roles;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author williambl
 * <p>
 * The type Cmd add trick.
 */
public final class CmdAddTrick extends Command {

    /**
     * Instantiates a new Cmd add trick.
     */
    public CmdAddTrick() {
        super();
        name = "addtrick";
        help = "Adds a new trick, either a string or an embed, if a string you only need the <names] and <body>.";
        category = new Category("Info");
        arguments = "<string> (or) <embed> <name1> [name2 name3] | <trick content body> (or) <title> "
            + "| <description> | <colour-as-hex-code>";
        aliases = new String[]{"add-trick"};
        //TODO Convert to a slash command and setup multiple role use.
        requiredRole = "bot maintainer";
        guildOnly = true;
    }

    /**
     * Execute.
     *
     * @param event the event
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        final var channel = event.getMessage();
        var args = event.getArgs();
        var firstSpace = args.indexOf(" ");

        try {
            Tricks.addTrick(Tricks.getTrickType(args.substring(0, firstSpace))
                .createFromArgs(args.substring(firstSpace + 1)));
            channel.reply("Added trick!").mentionRepliedUser(false).queue();
        } catch (IllegalArgumentException e) {
            channel.reply("A command with that name already exists!").mentionRepliedUser(false).queue();
            MMDBot.LOGGER.warn("Failure adding trick: {}", e.getMessage());
        }
    }
}
