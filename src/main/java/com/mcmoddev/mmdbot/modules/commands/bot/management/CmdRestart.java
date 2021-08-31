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

/**
 * Restart the bot on command rather than via console.
 *
 * @author ProxyNeko
 */
public class CmdRestart extends Command {

    /**
     * Instantiates a new Cmd.
     */
    public CmdRestart() {
        super();
        name = "restart";
        help = "Restarts the bot.";
        ownerCommand = true;
        hidden = true;
        guildOnly = false;
    }

    /**
     * Try to restart the command from Discord rather than having to get someone with actual console access.
     *
     * @param event The event.
     */
    @Override
    protected void execute(final CommandEvent event) {
        //TODO Work on restart code, attempt to make it platform agnostic. -Proxy
    }
}
