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
package com.mcmoddev.mmdbot.modules.commands.bot.management;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.Utils;

/**
 * Rename the bot, not a nickname, but it's actual name.
 *
 * @author ProxyNeko
 */
public class CmdRename extends Command {

    /**
     * Instantiates a new Cmd avatar.
     */
    public CmdRename() {
        super();
        name = "rename";
        help = "Set the name of the bot. Name can only be used twice an hour, has a 35 min cooldown each time used.";
        ownerCommand = true;
        hidden = true;
        guildOnly = false;
        cooldown = 2100;
    }

    /**
     * Try to set a new username for the bot.
     *
     * @param event The event
     */
    @Override
    protected void execute(final CommandEvent event) {
        final var commandArgs = event.getArgs();
        final var channel = event.getChannel();
        final var trigger = event.getMessage();
        final var newName = event.getArgs();
        final var selfUser = MMDBot.getInstance().getSelfUser();

        if (commandArgs.isEmpty()) {
            channel.sendMessage("No new name provided! Please provide me with a new name!")
                .reference(trigger).queue();
            return;
        }

        //TODO Better handling of the twice an hour name change limit... -ProxyNeko
        try {
            selfUser.getManager().setName(newName).queue();
            Utils.sleepTimer();
            channel.sendMessage("I shall henceforth be known as... **" + selfUser.getAsMention() + "**!")
                .reference(trigger).queue();
        } catch (Exception exception) {
            channel.sendMessage("Failed to set a new username... Please see logs for more info!").queue();
            MMDBot.LOGGER.error("Failed to set a new username... ", exception);
        }
    }
}
