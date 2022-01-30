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
package com.mcmoddev.mmdbot.modules.commands.server.moderation;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.console.MMDMarkers;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Mute the given user, by giving the designated muted role.
 * Takes a user and optionally a time and unit.
 * The units are restricted to any of:
 * - minute
 * - hour
 * - day
 * - week
 * <p>
 * Takes the form:
 * /mute KiriCattus
 * /mute KiriCattus 2 minutes
 * /mute KiriCattus 2 hours
 * /mute KiriCattus 2 days
 * /mute KiriCattus 2 weeks
 * /mute [user] [time] [unit]
 *
 * @author KiriCattus
 * @author Curle
 */
public final class CmdMute extends SlashCommand {

    /**
     * The constant REQUIRED_PERMISSIONS.
     */
    private static final EnumSet<Permission> REQUIRED_PERMISSIONS = EnumSet.of(Permission.MANAGE_ROLES);

    /**
     * Instantiates a new Cmd mute.
     */
    public CmdMute() {
        super();
        name = "mute";
        help = "Mute a user either indefinitely or for a set amount of time.";
        category = new Category("Moderation");
        arguments = "<userID/Mention> [time, otherwise forever] [unit, otherwise minutes]";
        requiredRole = "Staff";
        guildOnly = true;
        botPermissions = REQUIRED_PERMISSIONS.toArray(new Permission[0]);

        OptionData user = new OptionData(OptionType.USER, "user", "The user to mute.").setRequired(true);
        OptionData time = new OptionData(OptionType.NUMBER, "time", "The amount of time to mute for. Forever if not specified.").setRequired(false);
        OptionData unit = new OptionData(OptionType.STRING, "unit", "The unit of the time specifier.")
            .setRequired(false)
            .addChoice("minute", "minute")
            .addChoice("hour", "hour")
            .addChoice("day", "day")
            .addChoice("week", "week");
        List<OptionData> dataList = new ArrayList<>();
        dataList.add(user);
        dataList.add(time);
        dataList.add(unit);
        this.options = dataList;
    }

    /**
     * Execute.
     *
     * @param event The {@link SlashCommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        final var author = event.getGuild().getMember(event.getUser());
        if (author == null) {
            return;
        }

        Member user = event.getOption("user").getAsMember();
        OptionMapping time = event.getOption("time");
        OptionMapping unit = event.getOption("time");

        final var mutedRoleID = MMDBot.getConfig().getRole("muted");
        final var mutedRole = event.getGuild().getRoleById(mutedRoleID);

        if (user == null) {
            event.reply(String.format("User %s not found.", user.getEffectiveName())).setEphemeral(true).queue();
            return;
        }

        if (mutedRole == null) {
            MMDBot.LOGGER.error(MMDMarkers.MUTING, "Unable to find muted role {}", mutedRoleID);
            return;
        }

        event.getGuild().addRoleToMember(user, mutedRole).queue();

        if (time != null) {
            event.getGuild().removeRoleFromMember(user, mutedRole).queueAfter((long) time.getAsDouble(), parseTimeUnit(unit.getAsString()));
        }

        final String timeString;
        if (time != null) {
            timeString = " " + time.getAsString() + " " + parseTimeUnit(unit.getAsString()).toString().toLowerCase(Locale.ROOT);
        } else {
            timeString = "ever";
        }

        event.replyFormat("Muted user %s for%s.", user.getAsMention(), timeString).queue();
        MMDBot.LOGGER.info(MMDMarkers.MUTING, "User {} was muted by {} for{}", user, author, timeString);

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
