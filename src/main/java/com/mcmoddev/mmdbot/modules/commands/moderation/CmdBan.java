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
package com.mcmoddev.mmdbot.modules.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.CommandUtilities;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.console.MMDMarkers;
import net.dv8tion.jda.api.Permission;

import java.util.EnumSet;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * The type Cmd ban.
 *
 * @author Jriwanek
 */
public final class CmdBan extends Command {

    /**
     * The constant REQUIRED_PERMISSIONS.
     */
    private static final EnumSet<Permission> REQUIRED_PERMISSIONS = EnumSet.of(Permission.BAN_MEMBERS);

    /**
     * Instantiates a new Cmd ban.
     */
    public CmdBan() {
        super();
        name = "ban";
        help = "Ban or temp ban a user from the server.";
        category = new Category("Moderation");
        arguments = "<userID/mention> <ban reason> [time, otherwise forever] [time unit, otherwise minutes] "
            + "[-d (deletes messages from the past 7 days)]";
        requiredRole = "Staff";
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
        if (!CommandUtilities.checkCommand(this, event)) {
            return;
        }

        final String banReason;
        final var channel = event.getMessage();
        final var author = event.getGuild().getMember(event.getAuthor());

        if (author == null) {
            return;
        }

        if (event.getArgs().isEmpty()) {
            channel.reply("No arguments provided, please use the following arguments with this command: "
                + "``" + getArguments() + "``").queue();
        } else {
            final String[] args = event.getArgs().split(" ");
            final var member = Utils.getUserFromString(args[0], event.getGuild());

            if (member == null) {
                channel.reply(String.format("User %s not found.", args[0])).queue();
                return;
            }

            if (!(args.length > 1)) {
                banReason = "Reason for ban could not be found or was not provided, please contact "
                    + author.getUser().getAsTag() + " - (" + author.getId() + ")";
            } else {
                banReason = event.getArgs().substring(args[1].length() + 1);
            }

            //TODO ignore the -d if no time is specified
            final long time;
            if (args.length > 2) {
                time = parseTime(args[2]);
            } else {
                time = -1;
            }

            //TODO ignore the -d if no time is specified
            final TimeUnit unit;
            if (args.length > 3) {
                unit = parseTimeUnit(args[3]);
            } else {
                unit = TimeUnit.MINUTES;
            }

            if (!event.getArgs().contains("-d")) {
                event.getGuild().ban(member, 0, banReason).queue();
            } else {
                event.getGuild().ban(member, 7, banReason).queue();
            }

            if (time > 0) {
                event.getGuild().unban(member).queueAfter(time, unit);
            }

            final String timeString;
            if (time > 0) {
                timeString = " " + time + " " + unit.toString().toLowerCase(Locale.ROOT);
            } else {
                timeString = "ever";
            }

            channel.replyFormat("Banned: %s, Reason: %s for%s.", member.getAsMention(), banReason, timeString).queue();
            MMDBot.LOGGER.info(MMDMarkers.BANNING, "User {} was banned by {}. Reason: {} Time: for{}",
                member, author, banReason, timeString);
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
     * Parse time unit.
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
