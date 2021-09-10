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

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;
import static com.mcmoddev.mmdbot.utilities.console.MMDMarkers.BANNING;

/**
 * The type Cmd unban.
 *
 * @author
 */
public final class CmdUnban extends Command {

    /**
     * Instantiates a new Cmd unban.
     */
    public CmdUnban() {
        super();
        name = "unban";
        help = "Unbans a user. Usage: !mmd-unban <userID/mention>";
        hidden = true;
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
        final long bannedRoleID = getConfig().getRole("banned");
        final var bannedRole = guild.getRoleById(bannedRoleID);

        if (author.hasPermission(Permission.BAN_MEMBERS)) {
            if (member == null) {
                channel.sendMessage(String.format("User %s not found.", event.getArgs())).queue();
                return;
            }

            if (bannedRole == null) {
                LOGGER.error(BANNING, "Unable to find banned role {}", bannedRoleID);
                return;
            }

            guild.removeRoleFromMember(member, bannedRole).queue();
            channel.sendMessageFormat("Unbanned user %s.", member.getAsMention()).queue();
            LOGGER.info(BANNING, "User {} was unbanned by {}", member, author);
        } else {
            channel.sendMessage("You do not have permission to use this command.").queue();
        }
    }
}
