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
package com.mcmoddev.mmdbot.modules.commands.bot.management;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 * Rename the bot's User.
 * Takes a name parameter.
 * <p>
 * Takes the form:
 * /rename NewUsername
 * /rename [name]
 * <p>
 * TODO: Figure out the cooldown system.
 *
 * @author ProxyNeko
 * @author Curle
 */
public class CmdRename extends SlashCommand {

    /**
     * Instantiates a new Cmd avatar.
     */
    public CmdRename() {
        super();
        name = "rename";
        help = "Set the name of the bot. Name can only be used twice an hour, has a 35 min cool down each time used.";
        category = new Category("Management");
        arguments = "<username>";
        ownerCommand = true;
        guildOnly = true;
        cooldown = 2100;

        options = Collections.singletonList(new OptionData(OptionType.STRING, "name", "The new name to set.").setRequired(true));
    }

    /**
     * Try to set a new username for the bot.
     *
     * @param event The event
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        final var theBot = MMDBot.getInstance().getSelfUser();

        try {
            theBot.getManager().setName(event.getOption("name").getAsString()).queue();
            Utils.sleepTimer();
            event.reply("I shall henceforth be known as... **" + theBot.getAsMention() + "**!")
                .mentionRepliedUser(false).setEphemeral(true).queue();
        } catch (Exception exception) {
            event.reply("Failed to set a new username... Please see logs for more info! "
                + "(You can only change the bots username twice an hour, please wait before trying again)")
                .mentionRepliedUser(false).setEphemeral(true).queue();
            MMDBot.LOGGER.error("Failed to set a new username... ", exception);
        }
    }
}
