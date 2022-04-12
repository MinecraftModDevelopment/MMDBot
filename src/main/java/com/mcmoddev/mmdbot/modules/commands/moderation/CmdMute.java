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

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.modules.logging.LoggingModule;
import com.mcmoddev.mmdbot.utilities.CommandUtilities;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.console.MMDMarkers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.Color;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
        arguments = "<userID/Mention> [time, otherwise 28] [unit, otherwise days]";
        requiredRole = "Staff";
        guildOnly = true;
        botPermissions = REQUIRED_PERMISSIONS.toArray(new Permission[0]);

        OptionData time = new OptionData(OptionType.INTEGER, "time",
            "The amount of time to mute for. 28 if not specified.").setRequired(false);
        OptionData unit = new OptionData(OptionType.STRING, "unit", "The unit of the time specifier. Days if not specified")
            .setRequired(false).addChoice("seconds", "seconds").addChoice("minutes", "minutes")
            .addChoice("hours", "hours").addChoice("days", "days");
        options = List.of(new OptionData(OptionType.USER, "user", "User to mute", true),
            new OptionData(OptionType.STRING, "reason", "The reason to mute for").setRequired(true), time, unit);
    }

    /**
     * Execute.
     *
     * @param event The {@link SlashCommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        if (!CommandUtilities.checkCommand(this, event)) {
            return;
        }

        final Member toMute = event.getOption("user").getAsMember();
        if (toMute.getIdLong() == event.getMember().getIdLong()) {
            event.deferReply(true).setContent("You cannot mute yourself!").mentionRepliedUser(false).queue();
            return;
        }

        if (!event.getMember().canInteract(toMute)) {
            event.deferReply(true).setContent("You do not have permission to mute this user!").mentionRepliedUser(false)
                .queue();
            return;
        }

        final var botUser = event.getGuild().getMember(event.getJDA().getSelfUser());
        if (!botUser.canInteract(toMute)) {
            event.deferReply(true).setContent("I cannot mute this member!").mentionRepliedUser(false).queue();
            return;
        }

        if (toMute.isTimedOut()) {
            event.deferReply(true).setContent("This user is already muted!").mentionRepliedUser(false).queue();
            return;
        }

        long time = event.getOption("time") == null ? 28 : event.getOption("time").getAsLong();
        TimeUnit unit = event.getOption("unit") == null ? TimeUnit.DAYS
            : parseTimeUnit(event.getOption("unit").getAsString());

        final String reason = event.getOption("reason").getAsString();


        final var author = event.getGuild().getMember(event.getUser());
        if (author == null) {
            return;
        }

        event.deferReply(false)
            .addEmbeds(muteMember(event.getGuild(), event.getMember(), toMute, reason, time, unit))
            .mentionRepliedUser(false).queue();
        MMDBot.LOGGER.info(MMDMarkers.MUTING, "User {} was muted by {} for {}", toMute, author, time + " " + unit.toString());
    }

    public static MessageEmbed muteMember(final Guild guild, final Member muter, final Member member,
                                          final String reason, final long time, final TimeUnit timeUnit) {
        AtomicReference<String> muteTimeStr = new AtomicReference<>(" for " + time + " " + timeUnit);

        guild.timeoutFor(member, time, timeUnit).reason(reason).queue();

        final var muteEmbed = new EmbedBuilder().setColor(Color.DARK_GRAY)
            .setTitle(member.getEffectiveName() + " has been muted" + muteTimeStr.get())
            .addField("Muted User", "%s (%s)".formatted(member.getAsMention(), member.getIdLong()), false)
            .setDescription("**Reason**: " + reason + "\n**Muted By**: " + muter.getAsMention())
            .setTimestamp(Instant.now()).setFooter("Moderator ID: " + muter.getIdLong(), muter.getEffectiveAvatarUrl());

        Utils.executeInDMs(member.getIdLong(), dm -> {
            final var embed = new EmbedBuilder().setColor(Color.RED).setTitle("You have been muted!")
                .setDescription("You have been muted in **" + guild.getName() + "** by " + muter.getAsMention()
                    + muteTimeStr.get())
                .addField("Reason", reason, false).setTimestamp(Instant.now())
                .setFooter("Moderator ID: " + muter.getIdLong(), muter.getEffectiveAvatarUrl());
            dm.sendMessageEmbeds(embed.build()).queue();
        });

        LoggingModule.executeInLoggingChannel(LoggingModule.LoggingType.IMPORTANT, c -> c.sendMessageEmbeds(muteEmbed.build()).queue());

        return muteEmbed.build();
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
