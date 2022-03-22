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
package com.mcmoddev.mmdbot.modules.commands;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.modules.commands.bot.info.CmdAbout;
import com.mcmoddev.mmdbot.modules.commands.bot.management.CmdAvatar;
import com.mcmoddev.mmdbot.modules.commands.bot.management.CmdRename;
import com.mcmoddev.mmdbot.modules.commands.bot.management.CmdRestart;
import com.mcmoddev.mmdbot.modules.commands.bot.management.CmdShutdown;
import com.mcmoddev.mmdbot.modules.commands.community.information.CmdInvite;
import com.mcmoddev.mmdbot.modules.commands.moderation.CmdCommunityChannel;
import com.mcmoddev.mmdbot.modules.commands.moderation.CmdMute;
import com.mcmoddev.mmdbot.modules.commands.moderation.CmdOldChannels;
import com.mcmoddev.mmdbot.modules.commands.moderation.CmdReact;
import com.mcmoddev.mmdbot.modules.commands.moderation.CmdRolePanel;
import com.mcmoddev.mmdbot.modules.commands.moderation.CmdUnmute;
import com.mcmoddev.mmdbot.modules.commands.moderation.CmdWarning;
import com.mcmoddev.mmdbot.utilities.ThreadedEventListener;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * This is the main class for setting up commands before they are loaded in by the bot,
 * this way we can disable and enable them at will. Or at least that is the hope.
 *
 * @author KiriCattus
 */
public class CommandModule {

    /**
     * The constant commandClient.
     */
    private static CommandClient commandClient;

    /**
     * Gets command client.
     *
     * @return the command client
     */
    public static CommandClient getCommandClient() {
        return commandClient;
    }

    public static final Executor COMMAND_LISTENER_THREAD_POOL = Executors.newFixedThreadPool(2, r -> Utils.setThreadDaemon(new Thread(r, "CommandListener"), true));
    public static final Executor BUTTON_LISTENER_THREAD_POOL = Executors.newSingleThreadExecutor(r -> Utils.setThreadDaemon(new Thread(r, "ButtonListener"), true));

    /**
     * Setup and load the bots command module.
     */
    public static void setupCommandModule() {

        commandClient = new CommandClientBuilder()
            .setOwnerId(MMDBot.getConfig().getOwnerID())
            .setPrefix(MMDBot.getConfig().getMainPrefix())
            .setAlternativePrefix(MMDBot.getConfig().getAlternativePrefix())
            .useHelpBuilder(false).setManualUpsert(true).build();

        addSlashCommand(
            new CmdAbout(),
            new CmdMute(),
            new CmdUnmute(),
            new CmdCommunityChannel(),
            new CmdOldChannels(),
            new CmdAvatar(),
            new CmdRename(),
            new CmdShutdown(),
            new CmdRestart(),
            new CmdRolePanel(),
            new CmdWarning(),
            new CmdInvite());

        commandClient.addCommand(new CmdReact());

        if (MMDBot.getConfig().isCommandModuleEnabled()) {
            // Wrap the command and button listener in another thread, so that if a runtime exception
            // occurs while executing a command, the event thread will not be stopped
            // Commands and buttons are separated so that they do not interfere with each other
            MMDBot.getJDA().addEventListener(new ThreadedEventListener((EventListener) commandClient, COMMAND_LISTENER_THREAD_POOL));
            MMDBot.getJDA().addEventListener(buttonListener(CmdInvite.ListCmd.getButtonListener()));
            MMDBot.getJDA().addEventListener(buttonListener(new DismissListener()));
        } else {
            MMDBot.LOGGER.warn("Command module disabled via config, commands will not work at this time!");
        }
    }

    private static EventListener buttonListener(final EventListener listener) {
        return new ThreadedEventListener(listener, BUTTON_LISTENER_THREAD_POOL);
    }

    /**
     * Adds and upserts a slash command.
     *
     * @param cmds the command(s) to upsert
     */
    public static void addSlashCommand(final SlashCommand... cmds) {
        for (final var cmd : cmds) {
            commandClient.addSlashCommand(cmd);
            upsertCommand(cmd.buildCommandData(), cmd.isGuildOnly());
        }
    }

    public static void upsertCommand(final CommandData data, boolean guildOnly) {
        if (guildOnly) {
            var guild = MMDBot.getJDA().getGuildById(MMDBot.getConfig().getGuildID());
            if (guild == null) {
                throw new NullPointerException("No Guild found!");
            }

            guild.upsertCommand(data).queue();
        } else {
            MMDBot.getJDA().upsertCommand(data).queue();
        }
    }
}
