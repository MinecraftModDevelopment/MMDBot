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
import static com.mcmoddev.mmdbot.utilities.console.MMDMarkers.BANNING;

/**
 * The type Cmd ban.
 *
 * @author
 */
public final class CmdBan extends Command {

    /**
     * Instantiates a new Cmd ban.
     */
    public CmdBan() {
        super();
        name = "ban";
        help = "Bans a user. Usage: !mmd-ban <userID/mention> [time, otherwise forever] [unit, otherwise minutes]";
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
        final long bannedRoleID = getConfig().getRole("banned");
        final var bannedRole = guild.getRoleById(bannedRoleID);
        final MessageChannel channel = event.getChannel();

        if (author.hasPermission(Permission.BAN_MEMBERS)) {
            if (member == null) {
                channel.sendMessage(String.format("User %s not found.", event.getArgs())).queue();
                return;
            }

            if (bannedRole == null) {
                LOGGER.error(BANNING, "Unable to find banned role {}", bannedRoleID);
                return;
            }

            final long time;
            if (args.length > 1) {
                time = parseTime(args[1]);
            } else {
                time = -1;
            }

            final TimeUnit unit;
            if (args.length > 2) {
                unit = parseTimeUnit(args[2]);
            } else {
                unit = TimeUnit.MINUTES;
            }

            guild.addRoleToMember(member, bannedRole).queue();

            if (time > 0) {
                guild.removeRoleFromMember(member, bannedRole).queueAfter(time, unit);
            }

            final String timeString;
            if (time > 0) {
                timeString = " " + time + " " + unit.toString().toLowerCase(Locale.ROOT);
            } else {
                timeString = "ever";
            }

            channel.sendMessageFormat("Banned user %s for%s.", member.getAsMention(), timeString).queue();
            LOGGER.info(MUTING, "User {} was banned by {} for{}", member, author, timeString);
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

    /**
     * Parse time unit time unit.
     *
     * @param timeUnitIn the time unit in
     * @return The {@code TimeUnit} formatted from a {@code String}.
     */
    TimeUnit parseTimeUnit(final String timeUnitIn) {
        TimeUnit unit;
        try {
            unit = TimeUnit.valueOf(timeUnitIn.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            unit = TimeUnit.MINUTES;
        }
        return unit;
    }
}
