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

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;
import static com.mcmoddev.mmdbot.utilities.console.MMDMarkers.KICKING;

/**
 * The type Cmd kick.
 *
 * @author
 */
public final class CmdKick extends Command {

    /**
     * Instantiates a new Cmd kick.
     */
    public CmdKick() {
        super();
        name = "kick";
        help = "Kicks a user. Usage: !mmd-kick <userID/mention>";
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
        final String[] args = event.getArgs().split(" ");
        final var member = Utils.getMemberFromString(args[0], event.getGuild());
        final long kickedRoleID = getConfig().getRole("kicked");
        final var kickedRole = guild.getRoleById(kickedRoleID);
        final MessageChannel channel = event.getChannel();

        if (author.hasPermission(Permission.KICK_MEMBERS)) {
            if (member == null) {
                channel.sendMessage(String.format("User %s not found.", event.getArgs())).queue();
                return;
            }

            if (kickedRole == null) {
                LOGGER.error(KICKING, "Unable to find kicked role {}", kickedRoleID);
                return;
            }

            guild.addRoleToMember(member, kickedRole).queue();

            channel.sendMessageFormat("Kicked user %s.", member.getAsMention());
            LOGGER.info(MUTING, "User {} was kicked by {}", member, author);
        } else {
            channel.sendMessage("You do not have permission to use this command.").queue();
        }
    }

    /**
     * Parse time long.
     *
     * @param timeIn the time in
     * @return The Time formatted from a {@code String}.
     */
    long parseTime(final String timeIn) {
        long time;
        try {
            time = Long.parseLong(timeIn);
        } catch (NumberFormatException ex) {
            time = -1;
        }
        return time;
    }
}
