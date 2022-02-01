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
package com.mcmoddev.mmdbot.modules.logging.misc;

import com.jagrosh.jdautilities.command.MessageContextMenu;
import com.jagrosh.jdautilities.command.MessageContextMenuEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.UserContextMenu;
import com.jagrosh.jdautilities.command.UserContextMenuEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.References;
import com.mcmoddev.mmdbot.core.TaskScheduler;
import com.mcmoddev.mmdbot.modules.commands.CommandModule;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Misc events.
 *
 * @author KiriCattus
 */
public final class MiscEvents extends ListenerAdapter {

    /**
     * The threshold amount in milliseconds between a {@link DisconnectEvent} and either a {@link ReconnectedEvent} or
     * {@link ResumedEvent}.
     */
    private static final long DISCONNECT_WARN_THRESHOLD_MILLIS = 5 * 1000; // 5 seconds
    /**
     * The amount of milliseconds since the last {@link DisconnectEvent}, or {@code 0} if no disconnect event has been
     * handled yet.
     */
    private long lastDisconnect = 0;

    /**
     * On ready.
     *
     * @param event the event
     */
    @Override
    public void onReady(final @NotNull ReadyEvent event) {
        MMDBot.LOGGER.warn("Bot is online and ready.");
        TaskScheduler.init();
        References.STARTUP_TIME = Instant.now();

        References.BOT_READY = true;
        event.getJDA().updateCommands().addCommands(CommandModule.GLOBAL_CMDS).queue($ -> {}, e -> {});
        event.getJDA().getGuilds().forEach(guild -> guild.updateCommands().addCommands(CommandModule.GUILD_CMDS
            .computeIfAbsent(guild.getIdLong(), k -> new ArrayList<>())).queue(commands -> {
                // TODO privileges should work
            /*Map<String, Collection<CommandPrivilege>> privileges = new HashMap<>();
            for (var cmd : commands) {
                SlashCommand command = CommandModule.SLASH_COMMANDS.get(cmd.getName());
                if (command != null) {
                    privileges.put(cmd.getId(), command.buildPrivileges(CommandModule.getCommandClient()));
                }
                guild.updateCommandPrivileges(privileges).queue();
            }*/
        }));
    }

    /**
     * On disconnect.
     *
     * @param event the event
     */
    @Override
    public void onDisconnect(final @NotNull DisconnectEvent event) {
        MMDBot.LOGGER.debug("Connection to remote Discord server has been terminated");
        lastDisconnect = System.currentTimeMillis();
    }

    /**
     * On resumed.
     *
     * @param event the event
     */
    @Override
    public void onResumed(final @NotNull ResumedEvent event) {
        Utils.sleepTimer();
        MMDBot.LOGGER.debug("Resumed connection to Discord");
        if (lastDisconnect != 0) {
            long disconnectTime = System.currentTimeMillis() - lastDisconnect;
            if (disconnectTime > DISCONNECT_WARN_THRESHOLD_MILLIS) {
                MMDBot.LOGGER.warn(
                    "Resumption of Discord connection took a longer time than expected (took {}s, threshold of {}s)",
                    disconnectTime / 1000.0D, DISCONNECT_WARN_THRESHOLD_MILLIS / 1000.0D);
            }
        }
    }

    /**
     * On reconnected.
     *
     * @param event the event
     */
    @Override
    public void onReconnected(@NotNull final ReconnectedEvent event) {
        Utils.sleepTimer();
        MMDBot.LOGGER.debug("Reconnected back to Discord");
        if (lastDisconnect != 0) {
            long disconnectTime = System.currentTimeMillis() - lastDisconnect;
            if (disconnectTime > DISCONNECT_WARN_THRESHOLD_MILLIS) {
                MMDBot.LOGGER.warn(
                    "Reconnection to Discord took a longer time than expected (took {}s, threshold of {}s)",
                    disconnectTime / 1000.0D, DISCONNECT_WARN_THRESHOLD_MILLIS / 1000.0D);
            }
        }
    }

    @Override
    public void onMessageContextInteraction(@NotNull final MessageContextInteractionEvent event) {
        final var menu = CommandModule.getMenu(event.getName());
        if (menu instanceof MessageContextMenu messageContextMenu) {
            messageContextMenu.run(new MessageContextMenuEvent(event.getJDA(), event.getResponseNumber(), event.getInteraction(), CommandModule.getCommandClient()));
        }
    }

    @Override
    public void onUserContextInteraction(@NotNull final UserContextInteractionEvent event) {
        final var menu = CommandModule.getMenu(event.getName());
        if (menu instanceof UserContextMenu userContextMenu) {
            userContextMenu.run(new UserContextMenuEvent(event.getJDA(), event.getResponseNumber(), event.getInteraction(), CommandModule.getCommandClient()));
        }
    }
}
