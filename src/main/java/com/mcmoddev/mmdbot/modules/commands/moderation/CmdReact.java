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
package com.mcmoddev.mmdbot.modules.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.CommandUtilities;
import net.dv8tion.jda.api.Permission;

import java.util.EnumSet;

/**
 * Reacts to the referenced message with an emote.
 * <p>
 * This is a normal (prefix) command in order to be able to reply to a message if you want to bot to react to it.
 *
 * @author matyrobbrt
 */
public final class CmdReact extends Command {

    private static final EnumSet<Permission> USER_PERMISSIONS = EnumSet.of(Permission.MANAGE_CHANNEL);

    public CmdReact() {
        name = "react";
        help = "Reacts with an emote to the specified message.";
        category = new Category("Moderation");
        arguments = "<emote> [replyToMessage/messageId]";
        guildOnly = true;
        userPermissions = USER_PERMISSIONS.toArray(new Permission[0]);
    }

    @Override
    protected void execute(final CommandEvent event) {
        if (!CommandUtilities.checkCommand(this, event)) {
            return;
        }

        try {
            final var args = event.getArgs().split(" ");
            long toReactMsg = 0;
            if (args.length >= 2) {
                toReactMsg = Long.parseLong(args[1]);
            } else if (args.length == 1) {
                if (event.getMessage().getMessageReference() == null) {
                    event.getMessage().reply("Please provide a message to react to.").mentionRepliedUser(false).queue();
                    return;
                }
                toReactMsg = event.getMessage().getMessageReference().getMessageIdLong();
            } else {
                event.getMessage().reply("Please provide an emote to reply with.").mentionRepliedUser(false).queue();
                return;
            }
            event.getChannel().addReactionById(toReactMsg, args[0].replaceAll("[<>]*", "")).queue(s -> {
            }, t ->
                event.getMessage().replyFormat("There was an exception while executing that command: **%s**", t.getLocalizedMessage()).queue());
        } catch (Exception e) {
            event.getMessage().replyFormat("I encountered an exception while trying to execute that command: **%s**", e.getLocalizedMessage()).queue();
            MMDBot.LOGGER.error("Exception while using the react command.", e);
        }
    }
}
