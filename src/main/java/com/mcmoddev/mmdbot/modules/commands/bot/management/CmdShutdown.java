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
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Shut down the bot and the JDA instance gracefully.
 *
 * @author KiriCattus
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
        guildOnly = false;
        options = List.of(new OptionData(OptionType.BOOLEAN, "clear_global", "If the shutdown should clear global commands."),
            new OptionData(OptionType.BOOLEAN, "clear_guild", "If the shutdown should clear guild commands."));
    }

    /**
     * Shut down the bot on command.
     *
     * @param event The event.
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        final var clearGlobal = Utils.getArgumentOr(event, "clear_global", OptionMapping::getAsBoolean, false);
        final var clearGuild = Utils.getArgumentOr(event, "clear_guild", OptionMapping::getAsBoolean, false);
        if (clearGuild && !event.isFromGuild()) {
            event.reply("You cannot clear guild commands if you are not in a guild!").queue();
            return;
        }
        if (clearGuild) {
            try {
                AtomicReference<InteractionHook> msg = new AtomicReference<>(
                    event.getInteraction().reply("Waiting for command deletion...").submit().get());
                MMDBot.LOGGER.warn(
                    "Deleting the guild commands of the guild with the id {} at the request of {} via Discord!",
                    event.getGuild().getIdLong(), event.getUser().getName());
                new Thread(() -> {
                    Utils.clearGuildCommands(event.getGuild(), () -> {
                        msg.get().editOriginal("Shutting down the bot!").queue();
                        executeShutdown(event);
                    });
                }, "GuildCommandClearing").start();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                event.reply("Error! Shutdown cancelled!").queue();
            }
            return;
        }
        if (clearGlobal) {
            try {
                AtomicReference<InteractionHook> msg = new AtomicReference<>(
                    event.getInteraction().reply("Waiting for command deletion...").submit().get());
                MMDBot.LOGGER.warn(
                    "Deleting the global commands at the request of {} via Discord!", event.getUser().getName());
                new Thread(() -> {
                    Utils.clearGlobalCommands(() -> {
                        msg.get().editOriginal("Shutting down the bot!").queue();
                        executeShutdown(event);
                    });
                }, "GlobalCommandClearing").start();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                event.reply("Error! Shutdown cancelled!").queue();
            }
            return;
        }
        event.deferReply().setContent("Shutting down the bot!").mentionRepliedUser(false).queue();
        executeShutdown(event);
    }

    private void executeShutdown(final SlashCommandEvent event) {
        //Shut down the JDA instance gracefully.
        MMDBot.getInstance().shutdown();
        MMDBot.LOGGER.warn("Shutting down the bot by request of " + event.getUser().getName() + " via Discord!");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(0);
            }
        }, 1000);
    }
}
