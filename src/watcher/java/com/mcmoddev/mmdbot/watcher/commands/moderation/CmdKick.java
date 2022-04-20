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
package com.mcmoddev.mmdbot.watcher.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

import java.util.EnumSet;

/**
 * The type Cmd kick.
 *
 * @author Jriwanek
 */
public final class CmdKick extends Command {

    /**
     * The constant REQUIRED_PERMISSIONS.
     */
    private static final Permission[] REQUIRED_PERMISSIONS = {
        Permission.KICK_MEMBERS
    };

    /**
     * Instantiates a new Cmd kick.
     */
    public CmdKick() {
        super();
        name = "kick";
        help = "Kicks a user.";
        category = new Category("Moderation");
        arguments = "<userID/Mention> [Reason for kick]";
        guildOnly = true;
        botPermissions = REQUIRED_PERMISSIONS;
        userPermissions = REQUIRED_PERMISSIONS;
    }

    /**
     * Execute.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        final var channel = event.getMessage();
        final var author = event.getGuild().getMember(event.getAuthor());

        if (author == null) {
            return;
        }

        if (event.getArgs().isEmpty()) {
            channel.reply("No arguments provided, please use the following arguments with this command: "
                + "``" + getArguments() + "``").queue();
        } else {
            final var mentioned = !event.getMessage().getMentionedMembers(event.getGuild()).isEmpty();
            final String[] args = event.getArgs().split(" ");
            event.getGuild().retrieveMemberById(mentioned ? event.getMessage().getMentionedMembers(event.getGuild()).get(0).getId() : args[0]).queue(member -> {
                final String kickReason;
                if (!mentioned && !(args.length > 1)) {
                    kickReason = "Reason for kick could not be found or was not provided, please contact "
                        + author.getUser().getAsTag() + " - (" + author.getId() + ")";
                } else {
                    kickReason = mentioned ? event.getArgs() : event.getArgs().substring(args[0].length() + 1);
                }

                event.getGuild().kick(member, kickReason).queue();
                channel.replyFormat("Kicked user %s.", member.getAsMention()).queue();
            }, e -> channel.replyFormat("User %s not found.", args[0]).queue());
        }
    }
}
