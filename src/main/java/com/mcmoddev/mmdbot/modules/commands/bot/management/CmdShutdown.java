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
import com.mcmoddev.mmdbot.MMDBot;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Shut down the bot and the JDA instance gracefully.
 *
 * @author ProxyNeko
 * @author Curle
 */
public class CmdShutdown extends SlashCommand {

    /**
     * Instantiates a new Cmd.
     */
    public CmdShutdown() {
        super();
        name = "shutdown";
        help = "Shuts the bot down without restarting it. (Only usable by KiriCattus)";
        category = new Category("Management");
        ownerCommand = true;
        guildOnly = true;
    }

    /**
     * Shut down the bot on command.
     *
     * @param event The event.
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        event.reply("Shutting down the bot!").queue();
        //Shut down the JDA instance gracefully.
        event.getJDA().shutdown();
        MMDBot.LOGGER.warn("Shutting down the bot by request of " + event.getUser().getName() + " via Discord!");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(0);
            }
        }, 1000);
    }
}
