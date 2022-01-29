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
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

/**
 * Set the avatar of the bot.
 * Requires bot admin permissions.
 * <p>
 * Takes a single parameter of the URL of the image to use.
 * <p>
 * Takes the form:
 * /avatar https://media.discordapp.net/attachments/899012022006579220/899281929629728788/guineverebolb.png
 * /avatar [image]
 *
 * @author ProxyNeko
 * @author Curle
 */
public class CmdAvatar extends SlashCommand {

    /**
     * Instantiates a new Cmd avatar.
     */
    public CmdAvatar() {
        super();
        name = "avatar";
        help = "Set the avatar of the bot. (Only usable by KiriCattus)";
        category = new Category("Management");
        arguments = "<Image URL>";
        ownerCommand = true;
        guildOnly = true;

        options = Collections.singletonList(new OptionData(OptionType.STRING, "image", "The URL to download the new avatar image from.").setRequired(true));
    }

    /**
     * Try to set a new avatar for the bot.
     *
     * @param event the event
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        URL link;
        try {
            link = new URL(event.getOption("image").getAsString());
        } catch (MalformedURLException e) {
            event.reply("That's not a valid URL.").setEphemeral(true).queue();
            return;
        }

        try {
            MMDBot.getInstance().getSelfUser().getManager().setAvatar(Icon.from(link.openStream())).queue();
            event.reply("New avatar set, how do I look?").mentionRepliedUser(false).setEphemeral(true).queue();
        } catch (IOException exception) {
            event.reply("Failed to set a new avatar... Please see logs for more info!")
                .mentionRepliedUser(false).setEphemeral(true).queue();
            MMDBot.LOGGER.error("Failed to set a new avatar... ", exception);
        }
    }

}
