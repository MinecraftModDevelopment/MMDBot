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
import com.jagrosh.jdautilities.command.ContextMenu;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.modules.commands.bot.info.CmdAbout;
import com.mcmoddev.mmdbot.modules.commands.bot.info.CmdHelp;
import com.mcmoddev.mmdbot.modules.commands.bot.info.CmdUptime;
import com.mcmoddev.mmdbot.modules.commands.bot.management.CmdAvatar;
import com.mcmoddev.mmdbot.modules.commands.bot.management.CmdRefreshScamLinks;
import com.mcmoddev.mmdbot.modules.commands.bot.management.CmdRename;
import com.mcmoddev.mmdbot.modules.commands.bot.management.CmdShutdown;
import com.mcmoddev.mmdbot.modules.commands.contextmenu.message.ContextMenuAddQuote;
import com.mcmoddev.mmdbot.modules.commands.contextmenu.message.ContextMenuGist;
import com.mcmoddev.mmdbot.modules.commands.contextmenu.user.ContextMenuUser;
import com.mcmoddev.mmdbot.modules.commands.general.info.CmdCatFacts;
import com.mcmoddev.mmdbot.modules.commands.general.info.CmdFabricVersion;
import com.mcmoddev.mmdbot.modules.commands.general.info.CmdForgeVersion;
import com.mcmoddev.mmdbot.modules.commands.general.info.CmdMe;
import com.mcmoddev.mmdbot.modules.commands.general.info.CmdMinecraftVersion;
import com.mcmoddev.mmdbot.modules.commands.general.info.CmdSearch;
import com.mcmoddev.mmdbot.modules.commands.general.mappings.CmdMappings;
import com.mcmoddev.mmdbot.modules.commands.general.mappings.CmdTranslateMappings;
import com.mcmoddev.mmdbot.modules.commands.server.CmdGuild;
import com.mcmoddev.mmdbot.modules.commands.server.CmdRoles;
import com.mcmoddev.mmdbot.modules.commands.server.CmdToggleEventPings;
import com.mcmoddev.mmdbot.modules.commands.server.CmdToggleMcServerPings;
import com.mcmoddev.mmdbot.modules.commands.server.DeletableCommand;
import com.mcmoddev.mmdbot.modules.commands.server.moderation.CmdCommunityChannel;
import com.mcmoddev.mmdbot.modules.commands.server.moderation.CmdMute;
import com.mcmoddev.mmdbot.modules.commands.server.moderation.CmdOldChannels;
import com.mcmoddev.mmdbot.modules.commands.server.moderation.CmdReact;
import com.mcmoddev.mmdbot.modules.commands.server.moderation.CmdRolePanel;
import com.mcmoddev.mmdbot.modules.commands.server.moderation.CmdUnmute;
import com.mcmoddev.mmdbot.modules.commands.server.moderation.CmdUser;
import com.mcmoddev.mmdbot.modules.commands.server.moderation.CmdWarning;
import com.mcmoddev.mmdbot.modules.commands.server.quotes.CmdQuote;
import com.mcmoddev.mmdbot.modules.commands.server.tricks.CmdAddTrick;
import com.mcmoddev.mmdbot.modules.commands.server.tricks.CmdEditTrick;
import com.mcmoddev.mmdbot.modules.commands.server.tricks.CmdListTricks;
import com.mcmoddev.mmdbot.modules.commands.server.tricks.CmdRemoveTrick;
import com.mcmoddev.mmdbot.modules.commands.server.tricks.CmdRunTrick;
import com.mcmoddev.mmdbot.modules.commands.server.tricks.CmdRunTrickExplicitly;
import com.mcmoddev.mmdbot.utilities.ThreadedEventListener;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.tricks.Tricks;
import me.shedaniel.linkie.Namespaces;
import net.dv8tion.jda.api.hooks.EventListener;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * This is the main class for setting up commands before they are loaded in by the bot,
 * this way we can disable and enable them at will. Or at least that is the hope.
 *
 * @author ProxyNeko
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
        Namespaces.INSTANCE.init(CmdMappings.Companion.getMappings());

        commandClient = new CommandClientBuilder()
            .setOwnerId(MMDBot.getConfig().getOwnerID())
            .setPrefix(MMDBot.getConfig().getMainPrefix())
            .setAlternativePrefix(MMDBot.getConfig().getAlternativePrefix())
            .useHelpBuilder(false)
            .addSlashCommand(new CmdHelp())
            .addSlashCommand(new CmdGuild())
            .addSlashCommand(new CmdAbout())
            .addSlashCommand(new CmdMe())
            .addSlashCommand(new CmdUser())
            .addSlashCommand(new CmdRoles())
            .addSlashCommand(new CmdCatFacts())
            .addSlashCommand(new CmdSearch("google", "https://www.google.com/search?q=", "goog"))
            .addSlashCommand(new CmdSearch("bing", "https://www.bing.com/search?q="))
            .addSlashCommand(new CmdSearch("duckduckgo", "https://duckduckgo.com/?q=", "ddg"))
            .addSlashCommand(new CmdSearch("lmgtfy", "https://lmgtfy.com/?q=", "let-me-google-that-for-you"))
            .addSlashCommand(new CmdToggleMcServerPings())
            .addSlashCommand(new CmdToggleEventPings())
            .addSlashCommand(new CmdForgeVersion())
            .addSlashCommand(new CmdMinecraftVersion())
            .addSlashCommand(new CmdFabricVersion())
            .addSlashCommand(new CmdMute())
            .addSlashCommand(new CmdUnmute())
            .addSlashCommand(new CmdCommunityChannel())
            .addSlashCommand(new CmdOldChannels())
            .addSlashCommand(new CmdAvatar())
            .addSlashCommand(new CmdRename())
            .addSlashCommand(new CmdUptime())
            //TODO Setup DB storage for tricks and polish them off/add permission restrictions for when needed.
            .addSlashCommand(new CmdAddTrick())
            .addSlashCommand(new CmdEditTrick())
            .addSlashCommand(new CmdListTricks())
            .addSlashCommand(new CmdRemoveTrick())
            .addSlashCommand(new CmdRunTrickExplicitly())
            .addSlashCommands(Tricks.getTricks().stream().map(CmdRunTrick::new).toArray(SlashCommand[]::new))
            .addSlashCommand(new CmdShutdown())
            .addSlashCommands(CmdMappings.createCommands()) // TODO: This is broken beyond belief. Consider moving away from linkie. - Curle
            .addSlashCommands(CmdTranslateMappings.createCommands())
            .addSlashCommand(new CmdQuote())
            .addSlashCommand(new CmdRolePanel())
            .addSlashCommand(new CmdWarning())
            .addCommand(new CmdRefreshScamLinks())
            .addCommand(new CmdReact())
            // Context menus
            .addContextMenu(new ContextMenuAddQuote())
            .addContextMenu(new ContextMenuGist())
            .addContextMenu(new ContextMenuUser())
            .build();

        if (MMDBot.getConfig().isCommandModuleEnabled()) {
            // Wrap the command and button listener in another thread, so that if a runtime exception
            // occurs while executing a command, the event thread will not be stopped
            // Commands and buttons are separated so that they do not interfere with each other
            MMDBot.getInstance().addEventListener(new ThreadedEventListener((EventListener) commandClient, COMMAND_LISTENER_THREAD_POOL));
            MMDBot.getInstance().addEventListener(buttonListener(CmdMappings.ButtonListener.INSTANCE));
            MMDBot.getInstance().addEventListener(buttonListener(CmdTranslateMappings.ButtonListener.INSTANCE));
            MMDBot.getInstance().addEventListener(buttonListener(CmdRoles.getListener()));
            MMDBot.getInstance().addEventListener(buttonListener(CmdHelp.getListener()));
            MMDBot.getInstance().addEventListener(buttonListener(CmdListTricks.getListener()));
            MMDBot.getInstance().addEventListener(buttonListener(CmdQuote.ListQuotes.getListener()));
            MMDBot.LOGGER.warn("Command module enabled and loaded.");
        } else {
            MMDBot.LOGGER.warn("Command module disabled via config, commands will not work at this time!");
        }
    }

    private static EventListener buttonListener(final EventListener listener) {
        return new ThreadedEventListener(listener, BUTTON_LISTENER_THREAD_POOL);
    }

    /**
     * Removes a slash command. If the command is a {@link DeletableCommand}, this also marks it as deleted so that if
     * somehow it is run (which should be impossible) nothing will happen.
     *
     * @param name the name of the command to remove
     */
    public static void removeCommand(final String name) {
        var guild = MMDBot.getInstance().getGuildById(MMDBot.getConfig().getGuildID());
        if (guild == null) {
            throw new NullPointerException("No Guild found!");
        }

        commandClient.getSlashCommands().stream()
            .filter(cmd -> cmd.getName().equals(name))
            .filter(cmd -> cmd instanceof DeletableCommand)
            .map(cmd -> (DeletableCommand) cmd)
            .forEach(DeletableCommand::delete);

        guild.retrieveCommands()
            .flatMap(list -> list.stream().filter(cmd -> cmd.getName().equals(name)).findAny().map(cmd -> guild.deleteCommandById(cmd.getId())).orElseThrow())
            .queue();
    }

    /**
     * Adds and upserts a slash command.
     *
     * @param cmd the command
     */
    public static void addSlashCommand(final SlashCommand cmd) {
        commandClient.addSlashCommand(cmd);
        upsertCommand(cmd);
    }

    /**
     * Upserts a slash command.
     *
     * @param cmd the command
     */
    public static void upsertCommand(final SlashCommand cmd) {
        if (cmd.isGuildOnly()) {
            var guild = MMDBot.getInstance().getGuildById(MMDBot.getConfig().getGuildID());
            if (guild == null) {
                throw new NullPointerException("No Guild found!");
            }

            guild.upsertCommand(cmd.buildCommandData()).queue(cmd1 -> cmd1.updatePrivileges(guild, cmd.buildPrivileges(commandClient)).queue());
        } else {
            MMDBot.getInstance().upsertCommand(cmd.buildCommandData()).queue();
        }
    }

    /**
     * Upserts a context menu.
     *
     * @param menu the menu
     */
    public static void upsertContextMenu(final ContextMenu menu, final boolean guildOnly) {
        if (guildOnly) {
            var guild = MMDBot.getInstance().getGuildById(MMDBot.getConfig().getGuildID());
            if (guild == null) {
                throw new NullPointerException("No Guild found!");
            }

            guild.upsertCommand(menu.buildCommandData()).queue(cmd1 -> cmd1.updatePrivileges(guild, menu.buildPrivileges(commandClient)).queue());
        } else {
            MMDBot.getInstance().upsertCommand(menu.buildCommandData()).queue();
        }
    }
}
