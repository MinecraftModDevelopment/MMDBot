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
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.tricks.Trick;

import java.util.List;

/**
 * @author williambl
 *
 * The type Cmd run trick.
 */
public final class CmdRunTrick extends Command {

    /**
     * The Trick.
     */
    private final Trick trick;

    /**
     * Instantiates a new Cmd run trick.
     *
     * @param trick the trick
     */
    public CmdRunTrick(final Trick trick) {
        super();
        this.trick = trick;
        List<String> trickNames = trick.getNames();
        name = trickNames.get(0);
        aliases = trickNames.size() > 1 ? trickNames.subList(1, trickNames.size())
            .toArray(new String[0]) : new String[0];
        hidden = true;
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

        channel.sendMessage(trick.getMessage(event.getArgs().split(" "))).queue();
    }
}
