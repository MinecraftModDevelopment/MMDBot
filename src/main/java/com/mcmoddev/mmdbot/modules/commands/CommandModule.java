/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
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
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.commands.CommandUpserter;
import com.mcmoddev.mmdbot.core.commands.component.ComponentListener;
import com.mcmoddev.mmdbot.core.commands.component.ComponentManager;
import com.mcmoddev.mmdbot.core.commands.component.storage.ComponentStorage;
import com.mcmoddev.mmdbot.core.util.event.DismissListener;
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
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.EventListener;

import java.util.ArrayList;
import java.util.List;
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
    private static ComponentManager componentManager;

    /**
     * Gets command client.
     *
     * @return the command client
     */
    public static CommandClient getCommandClient() {
        return commandClient;
    }

    public static ComponentManager getComponentManager() {
        return componentManager;
    }

    public static final Executor COMMAND_LISTENER_THREAD_POOL = Executors.newFixedThreadPool(2, r -> Utils.setThreadDaemon(new Thread(r, "CommandListener"), true));
    public static final Executor BUTTON_LISTENER_THREAD_POOL = Executors.newSingleThreadExecutor(r -> Utils.setThreadDaemon(new Thread(r, "ButtonListener"), true));

    private static final List<ComponentListener> DEFERRED_COMPONENT_LISTENERS = new ArrayList<>();
    public static ComponentListener.Builder getComponentListener(final String featureId) {
        if (getComponentManager() == null) {
            return ComponentListener.builder(featureId, DEFERRED_COMPONENT_LISTENERS::add);
        } else {
            return ComponentListener.builder(featureId, getComponentManager()::addListener);
        }
    }

    /**
     * Setup and load the bots command module.
     */
    public static void setupCommandModule(final JDABuilder jda) {

        final var builder = new CommandClientBuilder()
            .setOwnerId(MMDBot.getConfig().getOwnerID())
            .setPrefix(MMDBot.getConfig().getMainPrefix())
            .setAlternativePrefix(MMDBot.getConfig().getAlternativePrefix())
            .setActivity(Activity.of(MMDBot.getConfig().getActivityType(), MMDBot.getConfig().getActivityName()))
            .useHelpBuilder(false)
            .setManualUpsert(true);

        builder.addSlashCommands(
            new CmdMute(),
            new CmdUnmute(),
            new CmdCommunityChannel(),
            new CmdOldChannels(),
            new CmdShutdown(),
            new CmdRolePanel(),
            new CmdWarning(),
            new CmdInvite());

        commandClient = builder.build();

        commandClient.addCommand(new CmdReact());

        if (MMDBot.getConfig().isCommandModuleEnabled()) {
            // Setup components
            {
                final var storage = ComponentStorage.sql(MMDBot.getDatabaseManager().jdbi(), "components");
                componentManager = new ComponentManager(storage, DEFERRED_COMPONENT_LISTENERS);
                jda.addEventListeners(buttonListener(componentManager));
            }

            final var upserter = new CommandUpserter(commandClient, false, String.valueOf(MMDBot.getConfig().getGuildID()));
            jda.addEventListeners(upserter);
            // Wrap the command and button listener in another thread, so that if a runtime exception
            // occurs while executing a command, the event thread will not be stopped
            // Commands and buttons are separated so that they do not interfere with each other
            jda.addEventListeners(new ThreadedEventListener((EventListener) commandClient, COMMAND_LISTENER_THREAD_POOL),
                 buttonListener(new DismissListener()));
        } else {
            MMDBot.LOGGER.warn("Command module disabled via config, commands will not work at this time!");
        }
    }

    private static EventListener buttonListener(final EventListener listener) {
        return new ThreadedEventListener(listener, BUTTON_LISTENER_THREAD_POOL);
    }
}
