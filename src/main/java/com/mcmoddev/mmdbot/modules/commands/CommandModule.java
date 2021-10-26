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
package com.mcmoddev.mmdbot.modules.commands;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.modules.commands.bot.info.CmdAbout;
import com.mcmoddev.mmdbot.modules.commands.bot.info.CmdHelp;
import com.mcmoddev.mmdbot.modules.commands.bot.info.CmdUptime;
import com.mcmoddev.mmdbot.modules.commands.bot.management.CmdAvatar;
import com.mcmoddev.mmdbot.modules.commands.bot.management.CmdRename;
import com.mcmoddev.mmdbot.modules.commands.bot.management.CmdShutdown;
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
import com.mcmoddev.mmdbot.modules.commands.server.moderation.CmdCommunityChannel;
import com.mcmoddev.mmdbot.modules.commands.server.moderation.CmdMute;
import com.mcmoddev.mmdbot.modules.commands.server.moderation.CmdOldChannels;
import com.mcmoddev.mmdbot.modules.commands.server.moderation.CmdUnmute;
import com.mcmoddev.mmdbot.modules.commands.server.moderation.CmdUser;
import com.mcmoddev.mmdbot.modules.commands.server.quotes.CmdQuote;
import com.mcmoddev.mmdbot.modules.commands.server.tricks.CmdAddTrick;
import com.mcmoddev.mmdbot.modules.commands.server.tricks.CmdListTricks;
import com.mcmoddev.mmdbot.modules.commands.server.tricks.CmdRemoveTrick;
import com.mcmoddev.mmdbot.modules.commands.server.tricks.CmdRunTrick;
import com.mcmoddev.mmdbot.modules.commands.server.tricks.CmdRunTrickExplicitly;
import com.mcmoddev.mmdbot.utilities.tricks.Tricks;
import me.shedaniel.linkie.Namespaces;

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
            .forceGuildOnly("619287233551138846")
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
            .addSlashCommand(new CmdListTricks())
            .addSlashCommand(new CmdRemoveTrick())
            .addSlashCommand(new CmdRunTrickExplicitly())
            .addSlashCommands(Tricks.getTricks().stream().map(CmdRunTrick::new).toArray(SlashCommand[]::new))
            .addSlashCommand(new CmdShutdown())
            .addSlashCommands(CmdMappings.createCommands()) // TODO: This is broken beyond belief. Consider moving away from linkie. - Curle
            .addSlashCommands(CmdTranslateMappings.createCommands())
            .addSlashCommand(new CmdQuote())
            .build();

        if (MMDBot.getConfig().isCommandModuleEnabled()) {
            MMDBot.getInstance().addEventListener(commandClient);
            MMDBot.getInstance().addEventListener(CmdMappings.ButtonListener.INSTANCE);
            MMDBot.getInstance().addEventListener(CmdTranslateMappings.ButtonListener.INSTANCE);
            MMDBot.getInstance().addEventListener(CmdRoles.getListener());
            MMDBot.getInstance().addEventListener(CmdHelp.getListener());
            MMDBot.getInstance().addEventListener(CmdListTricks.getListener());
            MMDBot.getInstance().addEventListener(CmdQuote.ListQuotes.getListener());
            MMDBot.LOGGER.warn("Command module enabled and loaded.");
        } else {
            MMDBot.LOGGER.warn("Command module disabled via config, commands will not work at this time!");
        }
    }
}
