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
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.tricks.Tricks;

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
        aliases = new String[]{"add-trick"};
        help = "Adds a new trick";
        requiredRole = "bot maintainer";
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
        final var channel = event.getTextChannel();

        String args = event.getArgs();
        int firstSpace = args.indexOf(" ");

        try {
            Tricks.addTrick(Tricks.getTrickType(args.substring(0, firstSpace))
                .createFromArgs(args.substring(firstSpace + 1)));
            channel.sendMessage("Added trick!").queue();
        } catch (IllegalArgumentException e) {
            channel.sendMessage("A command with that name already exists!").queue();
            MMDBot.LOGGER.warn("Failure adding trick: {}", e.getMessage());
        }
    }
}
