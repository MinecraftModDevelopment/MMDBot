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
package com.mcmoddev.mmdbot.modules.commands.server.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.EnumSet;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;
import static com.mcmoddev.mmdbot.utilities.console.MMDMarkers.MUTING;

/**
 * The type Cmd unmute.
 *
 * @author
 */
public final class CmdUnmute extends Command {

    private static final EnumSet<Permission> REQUIRED_PERMISSIONS = EnumSet.of(Permission.MANAGE_ROLES);

    /**
     * Instantiates a new Cmd unmute.
     */
    public CmdUnmute() {
        super();
        name = "unmute";
        help = "Un-mutes a user.";
        category = new Category("Moderation");
        arguments = "<userID/mention";
        requiredRole = "Moderators";
        guildOnly = true;
        botPermissions = REQUIRED_PERMISSIONS.toArray(new Permission[0]);
    }

    /**
     * Execute.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        final var guild = event.getGuild();
        final var author = guild.getMember(event.getAuthor());
        if (author == null) {
            return;
        }

        final MessageChannel channel = event.getChannel();
        final String[] args = event.getArgs().split(" ");
        final var member = Utils.getMemberFromString(args[0], event.getGuild());
        final var mutedRoleID = getConfig().getRole("muted");
        final var mutedRole = guild.getRoleById(mutedRoleID);

        if (member == null) {
            channel.sendMessage(String.format("User %s not found.", event.getArgs())).queue();
            return;
        }

        if (mutedRole == null) {
            LOGGER.error(MUTING, "Unable to find muted role {}", mutedRoleID);
            return;
        }

        guild.removeRoleFromMember(member, mutedRole).queue();
        channel.sendMessageFormat("Un-muted user %s.", member.getAsMention()).queue();
        LOGGER.info(MUTING, "User {} was un-muted by {}", member, author);
    }
}
