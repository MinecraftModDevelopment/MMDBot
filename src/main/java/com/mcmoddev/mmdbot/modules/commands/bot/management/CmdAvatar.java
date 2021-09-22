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

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import net.dv8tion.jda.api.entities.Icon;

import java.io.IOException;

/**
 * @author ProxyNeko
 *
 * Set the avatar of the bot, requires bot admin permissions.
 */
public class CmdAvatar extends Command {

    /**
     * Instantiates a new Cmd avatar.
     */
    public CmdAvatar() {
        super();
        name = "avatar";
        help = "Set the avatar of the bot.";
        category = new Category("Management");
        arguments = "(No args required, just upload a suitable square .png image that can be used as an avatar)";
        ownerCommand = true;
        guildOnly = true;
    }

    /**
     * Try to set a new avatar for the bot.
     *
     * @param event the event
     */
    @Override
    protected void execute(final CommandEvent event) {
        final var commandArgs = event.getArgs();
        final var channel = event.getMessage();
        final var attachment = event.getMessage().getAttachments();
        final var newAvatar = attachment.get(0);

        if (commandArgs.length() <= 1) {
            if (attachment.isEmpty()) {
                channel.reply("No image attachment provided, I need a new avatar!")
                    .mentionRepliedUser(false).queue();
                return;
            }

            if (!newAvatar.isImage()) {
                channel.reply("This attachment is not an image! "
                        + "Please provide a valid image to use as my avatar!")
                    .mentionRepliedUser(false).queue();
                return;
            }

            newAvatar.retrieveInputStream().thenAccept(setIcon -> {
                try {
                    MMDBot.getInstance().getSelfUser().getManager().setAvatar(Icon.from(setIcon)).queue();
                    channel.reply("New avatar set, how do I look?").mentionRepliedUser(false).queue();
                } catch (IOException exception) {
                    channel.reply("Failed to set a new avatar... Please see logs for more info!")
                        .mentionRepliedUser(false).queue();
                    MMDBot.LOGGER.error("Failed to set a new avatar... ", exception);
                }
            });
        }
    }
}
