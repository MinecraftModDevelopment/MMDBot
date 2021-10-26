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
package com.mcmoddev.mmdbot.utilities;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.database.dao.PersistedRoles;
import com.mcmoddev.mmdbot.utilities.database.dao.UserFirstJoins;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongPredicate;
import java.util.stream.Collectors;

import static com.mcmoddev.mmdbot.MMDBot.getConfig;

/**
 * The type Utils.
 *
 * @author
 */
public final class Utils {

    /**
     * Instantiates a new Utils.
     */
    private Utils() {
        // Shut up Sonarqube warns
    }

    /**
     * A sleep timer to help with getting some information from the audit log by
     * delaying the running code before we get the info from the audit log.
     * <p>
     * Helps prevent some NullPointerExceptions that we were getting sometimes.
     * For example getting the reason a user was banned is not always there right
     * away when the user banned event is fired.
     * <p>
     * Brought forward from the v2 bot.
     */
    public static void sleepTimer() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (final InterruptedException exception) {
            MMDBot.LOGGER.trace("InterruptedException", exception);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Get the OffsetDateTime from an Instant value, relative to UTC+0 / GMT+0.
     * Overtakes the last system which used LocalDateTime which was unpredictable and caused confusion among developers.
     *
     * @param instant the instant
     * @return OffsetDateTime. Offset from UTC+0.
     */
    public static OffsetDateTime getTimeFromUTC(final Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    /**
     * Gets time difference.
     *
     * @param fromTime The starting time.
     * @param toTime   The end time.
     * @return The difference between the two times.
     */
    public static String getTimeDifference(final OffsetDateTime fromTime, final OffsetDateTime toTime) {
        return getTimeDifference(fromTime, toTime, ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.DAYS, ChronoUnit.HOURS);
    }

    /**
     * Gets time difference.
     *
     * @param fromTime The starting time.
     * @param toTime   The end time.
     * @param units    the units
     * @return The difference between the two times.
     */
    public static String getTimeDifference(final OffsetDateTime fromTime, final OffsetDateTime toTime,
                                           final ChronoUnit... units) {
        final var joiner = new StringJoiner(", ");
        var temp = OffsetDateTime.from(fromTime);
        for (final ChronoUnit unit : units) {
            final long time = temp.until(toTime, unit);
            if (time > 0) {
                temp = temp.plus(time, unit);
                final var unitName = unit.toString();
                joiner.add(time + " " + (time < 2 && unitName.endsWith("s") ? unitName.substring(0, unitName.length()
                    - 1) : unitName));
            }
        }

        if (joiner.length() == 0) {
            // No valid representation found.
            joiner.add("a very short amount of time.");
        }

        return joiner.toString();
    }

    /**
     * Make hyperlink string.
     *
     * @param text The text to display for the link.
     * @param url  The URL the text points to.
     * @return The new hyperlink.
     */
    public static String makeHyperlink(final String text, final String url) {
        return String.format("[%s](%s)", text, url);
    }

    /**
     * Gets member from string.
     *
     * @param memberString The members string name or ID.
     * @param guild        The guild we are currently in.
     * @return The guild member.
     */
    @Nullable
    public static Member getMemberFromString(final String memberString, final Guild guild) {
        final var matcher = Message.MentionType.USER.getPattern().matcher(memberString);
        if (matcher.matches()) {
            return guild.getMemberById(matcher.group(1));
        } else if (memberString.contains("#")) {
            return guild.getMemberByTag(memberString);
        } else {
            return guild.getMemberById(memberString);
        }
    }

    /**
     * Get a Role instance from the default configured guild, with a given name.
     * This is primarily used for the advanced checks as part of the new SlashCommand system.
     * @param roleName the name of the role to fetch
     * @return the Role found if exists, null otherwise.
     */
    public static Role getRoleInHomeGuild(final String roleName) {
        JDA bot = MMDBot.getInstance();
        Guild homeGuild = bot.getGuildById(MMDBot.getConfig().getGuildID());
        if(homeGuild == null)
            return null;
        List<Role> roles = homeGuild.getRolesByName(roleName, false);
        if(roles.size() == 0)
            return null;
        return roles.get(0);
    }

    /**
     * Gets user from string.
     *
     * @param userString The members string name or ID.
     * @param guild        The guild we are currently in.
     * @return The guild member.
     */
    @Nullable
    public static User getUserFromString(final String userString, final Guild guild) {
        final var matcher = Message.MentionType.USER.getPattern().matcher(userString);
        if (matcher.matches()) {
            return guild.getJDA().getUserById(matcher.group(1));
        } else if (userString.contains("#")) {
            return guild.getJDA().getUserByTag(userString);
        } else {
            return guild.getJDA().getUserById(userString);
        }
    }

    /**
     * Gets reactions matching a predicate
     *
     * @param message   The message we are getting the matching reactions from.
     * @param predicate The predicate
     * @return The matching reactions.
     */
    public static List<MessageReaction> getMatchingReactions(final Message message, final LongPredicate predicate) {
        List<MessageReaction> reactions = message.getReactions();
        List<MessageReaction> matches = new ArrayList<>();
        for(MessageReaction react : reactions) {
            Emote emote = react.getReactionEmote().isEmote() ? react.getReactionEmote().getEmote() : null;
            if (emote != null)
                if(predicate.test(emote.getIdLong()))
                    matches.add(react);
        }

        return matches;
    }

    /**
     * Gets number of matching reactions.
     *
     * @param message   The message we are getting the number of matching reactions from.
     * @param predicate the predicate
     * @return The amount of matching reactions.
     */
    public static int getNumberOfMatchingReactions(final Message message, final LongPredicate predicate) {
        return message
            .getReactions()
            .stream()
            .filter(messageReaction -> messageReaction.getReactionEmote().isEmote())
            .filter(messageReaction -> predicate.test(messageReaction.getReactionEmote().getIdLong()))
            .mapToInt(MessageReaction::getCount)
            .sum();
    }

    /**
     * Get the users roles when they leave the guild with the user leave event.
     *
     * @param guild  The guild we are in.
     * @param userID The users ID.
     * @return A list of the roles the user had to save to a file for when they return.
     */
    @NotNull
    public static List<Role> getOldUserRoles(final Guild guild, final Long userID) {
        return MMDBot.database().withExtension(PersistedRoles.class, roles -> roles.getRoles(userID))
            .stream()
            .map(guild::getRoleById)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * Gets member join time.
     *
     * @param member The user.
     * @return The users join time.
     */
    public static Instant getMemberJoinTime(final Member member) {
        return MMDBot.database().withExtension(UserFirstJoins.class, joins -> joins.get(member.getIdLong()))
            .orElse(member.getTimeJoined().toInstant());
    }

    /**
     * Checks if the command can run in the given context, and returns if it should continue running.
     * <p>
     * This does the following checks in order (checks prefixed with GUILD will only take effect when ran from a
     * {@linkplain TextChannel guild channel}):
     * <ul>
     *     <li>GUILD; checks if the command is enabled in the guild.</li>
     *     <li>not in GUILD; checks if the command is enabled globally.</li>
     *     <li>GUILD: checks if the command is blocked in the channel/category.</li>
     *     <li>GUILD: checks if the command is allowed in the channel/category.</li>
     * </ul>
     *
     * @param command The command
     * @param event   The command event
     * @return If the command can run in that context
     */
    public static boolean checkCommand(final Command command, final CommandEvent event) {

        if (!isEnabled(command, event)) {
            //Could also send an informational message.
            return false;
        }

        if (event.isFromType(ChannelType.TEXT)) {
            final List<Long> exemptRoles = getConfig().getChannelExemptRoles();
            if (event.getMember().getRoles().stream()
                .map(ISnowflake::getIdLong)
                .anyMatch(exemptRoles::contains)) {
                //The member has a channel-checking-exempt role, bypass checking and allow the command.
                return true;
            }
        }

        if (isBlocked(command, event)) {
            event.getChannel()
                .sendMessage("This command is blocked from running in this channel!")
                .queue();
            return false;
        }

        //Sent from a guild.
        if (event.isFromType(ChannelType.TEXT)) {
            final List<Long> allowedChannels = getConfig().getAllowedChannels(command.getName(),
                event.getGuild().getIdLong());

            //If the allowlist is empty, default allowed.
            if (allowedChannels.isEmpty()) {
                return true;
            }

            final var channelID = event.getChannel().getIdLong();
            @Nullable final var category = event.getTextChannel().getParent();
            boolean allowed;
            if (category == null) {
                allowed = allowedChannels.stream().anyMatch(id -> id == channelID);
            } else { // If there's a category, also check that
                final var categoryID = category.getIdLong();
                allowed = allowedChannels.stream()
                    .anyMatch(id -> id == channelID || id == categoryID);
            }

            if (!allowed) {
                final List<Long> hiddenChannels = getConfig().getHiddenChannels();
                final String allowedChannelStr = allowedChannels.stream()
                    .filter(id -> !hiddenChannels.contains(id))
                    .map(id -> "<#" + id + ">")
                    .collect(Collectors.joining(", "));

                final StringBuilder str = new StringBuilder(84)
                    .append("This command cannot be run in this channel");
                if (!allowedChannelStr.isEmpty()) {
                    str.append(", only in ")
                        .append(allowedChannelStr);
                }
                event.getChannel()
                    //TODO: Remove the allowed channel string?
                    .sendMessage(str.append("!"))
                    .queue();
                return false;
            }
        }

        return true;
    }


    /**
     * Checks if the command can run in the given context, and returns if it should continue running.
     * <p>
     * This does the following checks in order (checks prefixed with GUILD will only take effect when ran from a
     * {@linkplain TextChannel guild channel}):
     * <ul>
     *     <li>GUILD; checks if the command is enabled in the guild.</li>
     *     <li>not in GUILD; checks if the command is enabled globally.</li>
     *     <li>GUILD: checks if the command is blocked in the channel/category.</li>
     *     <li>GUILD: checks if the command is allowed in the channel/category.</li>
     * </ul>
     *
     * For Slash commands only.
     *
     * @param command The command
     * @param event   The command event
     * @return If the command can run in that context
     */
    public static boolean checkCommand(final Command command, final SlashCommandEvent event) {

        if (!isEnabled(command, event)) {
            //Could also send an informational message.
            return false;
        }

        if (event.isFromGuild()) {
            final List<Long> exemptRoles = getConfig().getChannelExemptRoles();
            if (event.getMember().getRoles().stream()
                .map(ISnowflake::getIdLong)
                .anyMatch(exemptRoles::contains)) {
                //The member has a channel-checking-exempt role, bypass checking and allow the command.
                return true;
            }
        }

        if (isBlocked(command, event)) {
            event.reply("This command is blocked from running in this channel!")
                .setEphemeral(true)
                .queue();
            return false;
        }

        //Sent from a guild.
        if (event.isFromGuild()) {
            final List<Long> allowedChannels = getConfig().getAllowedChannels(command.getName(),
                event.getGuild().getIdLong());

            //If the allowlist is empty, default allowed.
            if (allowedChannels.isEmpty()) {
                return true;
            }

            final var channelID = event.getChannel().getIdLong();
            @Nullable final var category = event.getTextChannel().getParent();
            boolean allowed;
            if (category == null) {
                allowed = allowedChannels.stream().anyMatch(id -> id == channelID);
            } else { // If there's a category, also check that
                final var categoryID = category.getIdLong();
                allowed = allowedChannels.stream()
                    .anyMatch(id -> id == channelID || id == categoryID);
            }

            if (!allowed) {
                final List<Long> hiddenChannels = getConfig().getHiddenChannels();
                final String allowedChannelStr = allowedChannels.stream()
                    .filter(id -> !hiddenChannels.contains(id))
                    .map(id -> "<#" + id + ">")
                    .collect(Collectors.joining(", "));

                final StringBuilder str = new StringBuilder(84)
                    .append("This command cannot be run in this channel");
                if (!allowedChannelStr.isEmpty()) {
                    str.append(", only in ")
                        .append(allowedChannelStr);
                }
                event.reply(str.append("!").toString())
                    .setEphemeral(true)
                    .queue();
                return false;
            }
        }

        return true;
    }

    /**
     * Is enabled boolean.
     * For textual commands only.
     *
     * @param command the command
     * @param event   the event
     * @return boolean. boolean
     */
    private static boolean isEnabled(final Command command, final CommandEvent event) {
        if (event.isFromType(ChannelType.TEXT)) { // Sent from a guild
            return getConfig().isEnabled(command.getName(), event.getGuild().getIdLong());
        }
        return getConfig().isEnabled(command.getName());
    }

    /**
     * Is enabled boolean.
     * For Slash commands only.
     *
     * @param command the command
     * @param event   the event
     * @return boolean. boolean
     */
    private static boolean isEnabled(final Command command, final SlashCommandEvent event) {
        if (event.isFromGuild()) { // Sent from a guild
            return getConfig().isEnabled(command.getName(), event.getGuild().getIdLong());
        }
        return getConfig().isEnabled(command.getName());
    }

    /**
     * Is blocked boolean.
     * For textual commands only.
     *
     * @param command the command
     * @param event   the event
     * @return boolean. boolean
     */
    private static boolean isBlocked(final Command command, final CommandEvent event) {
        if (event.isFromType(ChannelType.TEXT)) { // Sent from a guild
            final var channelID = event.getChannel().getIdLong();
            final List<Long> blockedChannels = getConfig().getBlockedChannels(command.getName(),
                event.getGuild().getIdLong());
            @Nullable final var category = event.getTextChannel().getParent();
            if (category != null) {
                final var categoryID = category.getIdLong();
                return blockedChannels.stream()
                    .anyMatch(id -> id == channelID || id == categoryID);
            }
            return blockedChannels.stream().anyMatch(id -> id == channelID);
        }
        return false; // If not from a guild, default not blocked
    }

    /**
     * Is blocked boolean.
     * For Slash commands only.
     *
     * @param command the command
     * @param event   the event
     * @return boolean. boolean
     */
    private static boolean isBlocked(final Command command, final SlashCommandEvent event) {
        if (event.isFromGuild()) { // Sent from a guild
            final var channelID = event.getChannel().getIdLong();
            final List<Long> blockedChannels = getConfig().getBlockedChannels(command.getName(),
                event.getGuild().getIdLong());
            @Nullable final var category = event.getTextChannel().getParent();
            if (category != null) {
                final var categoryID = category.getIdLong();
                return blockedChannels.stream()
                    .anyMatch(id -> id == channelID || id == categoryID);
            }
            return blockedChannels.stream().anyMatch(id -> id == channelID);
        }
        return false; // If not from a guild, default not blocked
    }

    /**
     * Calls the given consumer only if the text channel with the given ID is known to the bot.
     *
     * @param channelID The channel ID
     * @param consumer  The consumer of the text channel
     * @see net.dv8tion.jda.api.JDA#getTextChannelById(long)
     */
    public static void getChannelIfPresent(final long channelID, final Consumer<TextChannel> consumer) {
        final var channel = MMDBot.getInstance().getTextChannelById(channelID);
        if (channel != null) {
            consumer.accept(channel);
        }
    }

    /**
     * Get a non-null string from an OptionMapping.
     *
     * @param option an OptionMapping to get as a string - may be null
     * @return the option mapping as a string, or an empty string if the mapping was null
     */
    public static String getOrEmpty(@Nullable OptionMapping option) {
        return Optional.ofNullable(option).map(OptionMapping::getAsString).orElse("");
    }

    /**
     * Gets an argument from a slash command as a string.
     *
     * @param event the slash command event
     * @param name the name of the option
     * @return the option's value as a string, or an empty string if the option had no value
     */
    public static String getOrEmpty(SlashCommandEvent event, String name) {
        return getOrEmpty(event.getOption(name));
    }
}
