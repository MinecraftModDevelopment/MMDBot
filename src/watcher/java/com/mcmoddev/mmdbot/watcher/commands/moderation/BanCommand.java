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

import java.util.EnumSet;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * The type Cmd ban.
 *
 * @author Jriwanek
 */
public final class BanCommand extends Command {

    /**
     * The constant REQUIRED_PERMISSIONS.
     */
    private static final EnumSet<Permission> REQUIRED_PERMISSIONS = EnumSet.of(Permission.BAN_MEMBERS);

    /**
     * Instantiates a new Cmd ban.
     */
    public BanCommand() {
        super();
        name = "ban";
        help = "Ban or temp ban a user from the server.";
        category = new Category("Moderation");
        arguments = "<userID/mention> <ban reason> [time, otherwise forever] [time unit, otherwise minutes] "
            + "[-d (deletes messages from the past 7 days)]";
        userPermissions = new Permission[]{
            Permission.BAN_MEMBERS
        };
        botPermissions = new Permission[]{
            Permission.BAN_MEMBERS
        };
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
        if (event.getArgs().isEmpty()) {
            event.reply("No arguments provided, please use the following arguments with this command: "
                + "``" + getArguments() + "``");
        } else {
            final String[] args = event.getArgs().split(" ");
            event.getGuild().retrieveMemberById(args[0]).queue(member -> {
                final String banReason;
                if (!(args.length > 1)) {
                    banReason = "Reason for ban could not be found or was not provided, please contact "
                        + event.getMember().getUser().getAsTag() + " - (" + event.getMember().getId() + ")";
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
                    event.getGuild().ban(member, 0, TimeUnit.DAYS).reason(banReason).queue();
                } else {
                    event.getGuild().ban(member, 7, TimeUnit.DAYS).reason(banReason).queue();
                }

                if (time > 0) {
                    event.getGuild().unban(User.fromId(member.getUser().getIdLong())).queueAfter(time, unit);
                }

                final String timeString;
                if (time > 0) {
                    timeString = " " + time + " " + unit.toString().toLowerCase(Locale.ROOT);
                } else {
                    timeString = "ever";
                }

                event.reply("Banned: %s, Reason: %s for%s.".formatted(member.getAsMention(), banReason, timeString));
            }, e -> event.reply(String.format("User %s not found.", args[0])));
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
