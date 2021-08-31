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

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.modules.commands.bot.info.CmdAbout;
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
import com.mcmoddev.mmdbot.modules.commands.server.quotes.CmdAddQuote;
import com.mcmoddev.mmdbot.modules.commands.server.quotes.CmdGetQuote;
import com.mcmoddev.mmdbot.modules.commands.server.quotes.CmdRemoveQuote;
import com.mcmoddev.mmdbot.modules.commands.server.tricks.CmdAddTrick;
import com.mcmoddev.mmdbot.modules.commands.server.tricks.CmdListTricks;
import com.mcmoddev.mmdbot.modules.commands.server.tricks.CmdRemoveTrick;
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
            .addCommand(new CmdGuild())
            .addCommand(new CmdAbout())
            .addCommand(new CmdMe())
            .addCommand(new CmdUser())
            .addCommand(new CmdRoles())
            .addCommand(new CmdCatFacts())
            .addCommand(new CmdSearch("google", "https://www.google.com/search?q=", "goog"))
            .addCommand(new CmdSearch("bing", "https://www.bing.com/search?q="))
            .addCommand(new CmdSearch("duckduckgo", "https://duckduckgo.com/?q=", "ddg"))
            .addCommand(new CmdSearch("lmgtfy", "https://lmgtfy.com/?q=", "let-me-google-that-for-you"))
            .addCommand(new CmdToggleMcServerPings())
            .addCommand(new CmdToggleEventPings())
            .addCommand(new CmdForgeVersion())
            .addCommand(new CmdMinecraftVersion())
            .addCommand(new CmdFabricVersion())
            .addCommand(new CmdMute())
            .addCommand(new CmdUnmute())
            .addCommand(new CmdCommunityChannel())
            .addCommand(new CmdOldChannels())
            .addCommand(new CmdAvatar())
            .addCommand(new CmdRename())
            .addCommand(new CmdUptime())
            //TODO Setup DB storage for tricks and polish them off/add permission restrictions for when needed.
            .addCommand(new CmdAddTrick())
            .addCommand(new CmdListTricks())
            .addCommand(new CmdRemoveTrick())
            .addCommands(Tricks.createTrickCommands().toArray(new Command[0]))
            .addCommand(new CmdShutdown())
            .addCommands(CmdMappings.createCommands())
            .addCommands(CmdTranslateMappings.createCommands())
            .addCommand(new CmdAddQuote())
            .addCommand(new CmdGetQuote())
            .addCommand(new CmdRemoveQuote())
            .setHelpWord("help")
            .build();

        if (MMDBot.getConfig().isCommandModuleEnabled()) {
            MMDBot.getInstance().addEventListener(commandClient);
            MMDBot.getInstance().addEventListener(CmdMappings.ButtonListener.INSTANCE);
            MMDBot.getInstance().addEventListener(CmdTranslateMappings.ButtonListener.INSTANCE);
            MMDBot.getInstance().addEventListener(new CmdListTricks.ButtonListener());
            MMDBot.LOGGER.warn("Command module enabled and loaded.");
        } else {
            MMDBot.LOGGER.warn("Command module disabled via config, commands will not work at this time!");
        }
    }
}
