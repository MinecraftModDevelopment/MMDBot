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
import net.dv8tion.jda.api.entities.User;

/**
 * The type Cmd unban.
 *
 * @author Jriwanek
 */
public final class UnbanCommand extends Command {

    /**
     * The constant REQUIRED_PERMISSIONS.
     */
    private static final Permission[] REQUIRED_PERMISSIONS = new Permission[]{
        Permission.BAN_MEMBERS
    };

    /**
     * Instantiates a new Cmd unban.
     */
    public UnbanCommand() {
        super();
        name = "unban";
        help = "Unbans a user.";
        category = new Category("Moderators");
        arguments = "<userID/mention>";
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
        final var guild = event.getGuild();
        final var userId = event.getArgs().split(" ")[0];
        guild.unban(User.fromId(userId))
            .flatMap($ -> event.getChannel().sendMessageFormat("Unbanned user <@%s>.", userId))
            .queue(null, e -> event.getChannel().sendMessageFormat("User with ID '%s' could not be found.", userId).queue());
    }
}
