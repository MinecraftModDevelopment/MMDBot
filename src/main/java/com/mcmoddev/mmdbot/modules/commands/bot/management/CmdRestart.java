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
package com.mcmoddev.mmdbot.modules.commands.bot.management;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.TaskScheduler;

import java.util.concurrent.TimeUnit;

/**
 * Restart the bot on command rather than via console.
 *
 * @author KiriCattus
 * @author matyrobbrt
 */
public class CmdRestart extends SlashCommand {

    /**
     * Instantiates a new Cmd.
     */
    public CmdRestart() {
        super();
        name = "restart";
        help = "Restarts the bot. (Only usable by KiriCattus)";
        category = new Category("Management");
        ownerCommand = true;
        hidden = true;
        guildOnly = false;
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        // TODO: make an alternative mechanism to restart bots
        if (true) {
            event.reply("This command is currently unavailable. Sorry.").queue();
            return;
        }

        event.reply("Restarting the bot!").queue();
        event.getJDA().shutdown();
        MMDBot.LOGGER.warn("Restarting the bot by request of {} via Discord!", event.getUser().getName());
        TaskScheduler.scheduleTask(() -> {
            // TODO some other things may need to be nullified for this to restart with no exceptions!
            // TODO: make an alternative mechanism to restart bots
            //MMDBot.BOT_TYPE.createBot(MMDBot.getInstance().getRunPath()).start(token);
        }, 3, TimeUnit.SECONDS);
    }
}
