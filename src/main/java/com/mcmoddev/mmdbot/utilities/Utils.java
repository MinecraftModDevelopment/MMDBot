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
package com.mcmoddev.mmdbot.utilities;

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.database.dao.PersistedRoles;
import com.mcmoddev.mmdbot.utilities.database.dao.UserFirstJoins;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.ObjLongConsumer;
import java.util.regex.Pattern;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
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
     *
     * @param roleName the name of the role to fetch
     * @return the Role found if exists, null otherwise.
     */
    public static Role getRoleInHomeGuild(final String roleName) {
        JDA bot = MMDBot.getJDA();
        Guild homeGuild = bot.getGuildById(MMDBot.getConfig().getGuildID());
        if (homeGuild == null)
            return null;
        List<Role> roles = homeGuild.getRolesByName(roleName, false);
        if (roles.size() == 0)
            return null;
        return roles.get(0);
    }

    /**
     * Gets a role with the specified {@code roleId} from the specifed {@code guild}
     *
     * @param guild     the guild to get the role from
     * @param roleId    the id of the role to get
     * @param ifPresent a consumer accepting the role, if it exists
     */
    public static void getRoleIfPresent(final Guild guild, final long roleId, final Consumer<Role> ifPresent) {
        final var role = guild.getRoleById(roleId);
        if (role != null) {
            ifPresent.accept(role);
        }
    }

    /**
     * Gets user from string.
     *
     * @param userString The members string name or ID.
     * @param guild      The guild we are currently in.
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
        for (MessageReaction react : reactions) {
            Emote emote = react.getReactionEmote().isEmote() ? react.getReactionEmote().getEmote() : null;
            if (emote != null)
                if (predicate.test(emote.getIdLong()))
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
     * Calls the given consumer only if the text channel with the given ID is known to the bot.
     *
     * @param channelID The channel ID
     * @param consumer  The consumer of the text channel
     * @see net.dv8tion.jda.api.JDA#getTextChannelById(long)
     */
    public static void getChannelIfPresent(final long channelID, final Consumer<TextChannel> consumer) {
        final var channel = MMDBot.getJDA().getTextChannelById(channelID);
        if (channel != null) {
            consumer.accept(channel);
        } else {
            MMDBot.LOGGER.error("Could not find channel with ID " + channelID);
        }
    }

    /**
     * Uses the text channel for a configured channel key.
     *
     * @param channelKey      the channel key
     * @param channelCallback callback for when the channel is configured and exists
     * @throws NullPointerException if either the channel key or channel callback is {@code null}
     * @see com.mcmoddev.mmdbot.core.BotConfig#getChannel(String)
     * @see #useTextChannel(String, Consumer, ObjLongConsumer)
     */
    public static void useTextChannel(final String channelKey, final Consumer<TextChannel> channelCallback) {
        useTextChannel(channelKey, channelCallback,
            (key, id) -> LOGGER.warn("Channel with ID {} configured as '{}' does not exist", id, key));
    }

    /**
     * Uses the text channel for a configured channel key, with a callback for a configured but missing channel.
     *
     * @param channelKey             the channel key
     * @param channelCallback        callback for when the channel is configured and exists
     * @param missingChannelCallback callback for when the channel is configured but does not exist, may be {@code null}
     * @throws NullPointerException if either the channel key or channel callback is {@code null}
     * @see com.mcmoddev.mmdbot.core.BotConfig#getChannel(String)
     * @see #useTextChannel(String, Consumer, ObjLongConsumer, Consumer)
     */
    public static void useTextChannel(final String channelKey, final Consumer<TextChannel> channelCallback,
                                      @Nullable final ObjLongConsumer<String> missingChannelCallback) {
        useTextChannel(channelKey, channelCallback, missingChannelCallback, null);
    }

    /**
     * Uses the text channel for a configured channel key, with callbacks for a configured but missing channel and a
     * non-configured channel.
     *
     * @param channelKey             the channel key
     * @param channelCallback        callback for when the channel is configured and exists
     * @param missingChannelCallback callback for when the channel is configured but does not exist, may be {@code null}
     * @param noChannelCallback      callback for when the channel is not configured, may be {@code null}
     * @throws NullPointerException if either the channel key or channel callback is {@code null}
     * @see com.mcmoddev.mmdbot.core.BotConfig#getChannel(String)
     */
    public static void useTextChannel(final String channelKey, final Consumer<TextChannel> channelCallback,
                                      @Nullable final ObjLongConsumer<String> missingChannelCallback,
                                      @Nullable final Consumer<String> noChannelCallback) {
        useChannel(channelKey, MMDBot.getJDA()::getTextChannelById, channelCallback, missingChannelCallback, noChannelCallback);
    }

    private static <C extends Channel> void useChannel(final String channelKey, final LongFunction<C> channelGetter,
                                                       final Consumer<? super C> channelCallback,
                                                       @Nullable final ObjLongConsumer<String> missingChannelCallback,
                                                       @Nullable final Consumer<String> noChannelCallback) {
        Objects.requireNonNull(channelKey, "channel key must not be null");
        Objects.requireNonNull(channelGetter, "channel getter must not be null");
        Objects.requireNonNull(channelCallback, "channel callback must not be null");
        final long channelID = getConfig().getChannel(channelKey);
        if (channelID != 0L) {
            final C channel = channelGetter.apply(channelID);
            if (channel != null) {
                channelCallback.accept(channel);
            } else if (missingChannelCallback != null) {
                missingChannelCallback.accept(channelKey, channelID);
            }
        } else if (noChannelCallback != null) {
            noChannelCallback.accept(channelKey);
        }
    }

    /**
     * Creates a discord link pointing to the specified message
     *
     * @param guildId   the ID of the guild of the message
     * @param channelId the ID of the channel of the message
     * @param messageId the message ID
     * @return the message link
     */
    public static String makeMessageLink(final long guildId, final long channelId, final long messageId) {
        return "https://discord.com/channels/%s/%s/%s".formatted(guildId, channelId, messageId);
    }

    /**
     * Sets the thread's daemon property to the specified {@code isDaemon} and returns it
     *
     * @param thread   the thread to modify
     * @param isDaemon if the thread should be daemon
     * @return the modified thread
     */
    public static Thread setThreadDaemon(final Thread thread, final boolean isDaemon) {
        thread.setDaemon(isDaemon);
        return thread;
    }

    public static void executeInDMs(final long userId, Consumer<PrivateChannel> consumer) {
        final var user = MMDBot.getJDA().getUserById(userId);
        if (user != null) {
            user.openPrivateChannel().queue(consumer::accept, e -> {
            });
        }
    }

    public static String uppercaseFirstLetter(final String string) {
        return string.substring(0, 1).toUpperCase(Locale.ROOT) + string.substring(1);
    }

    /**
     * Checks if the given member has the given role
     *
     * @param member the member to check
     * @param roleId the id of the role to search for
     * @return if the member has the role
     */
    public static boolean memberHasRole(final Member member, final long roleId) {
        if (member == null) {
            return false;
        }
        return member.getRoles().stream().anyMatch(r -> r.getIdLong() == roleId);
    }

    /**
     * Gets a message from a link
     *
     * @param link the link to decode
     * @return the message
     * @throws MessageLinkException if an exception occured in the meantime
     */
    public static Message getMessageByLink(final String link) throws MessageLinkException {
        final AtomicReference<Message> returnAtomic = new AtomicReference<>(null);
        decodeMessageLink(link, (guildId, channelId, messageId) -> {
            final var guild = MMDBot.getJDA().getGuildById(guildId);
            if (guild != null) {
                final var channel = guild.getTextChannelById(channelId);
                if (channel != null) {
                    returnAtomic.set(channel.retrieveMessageById(messageId).complete());
                }
            }
        });
        return returnAtomic.get();
    }

    public static final Pattern MESSAGE_LINK_PATTERN = Pattern.compile("https://discord.com/channels/");

    public static void decodeMessageLink(final String link, MessageInfo consumer)
        throws MessageLinkException {
        final var matcher = MESSAGE_LINK_PATTERN.matcher(link);
        if (matcher.find()) {
            try {
                var originalWithoutLink = matcher.replaceAll("");
                if (originalWithoutLink.indexOf('/') > -1) {
                    final long guildId = Long
                        .parseLong(originalWithoutLink.substring(0, originalWithoutLink.indexOf('/')));
                    originalWithoutLink = originalWithoutLink.substring(originalWithoutLink.indexOf('/') + 1);
                    if (originalWithoutLink.indexOf('/') > -1) {
                        final long channelId = Long
                            .parseLong(originalWithoutLink.substring(0, originalWithoutLink.indexOf('/')));
                        originalWithoutLink = originalWithoutLink.substring(originalWithoutLink.indexOf('/') + 1);
                        final long messageId = Long.parseLong(originalWithoutLink);
                        consumer.accept(guildId, channelId, messageId);
                    } else {
                        throw new MessageLinkException("Invalid Link");
                    }
                } else {
                    throw new MessageLinkException("Invalid Link");
                }
            } catch (NumberFormatException e) {
                throw new MessageLinkException(e);
            }
        } else {
            throw new MessageLinkException("Invalid Link");
        }
    }

    public static class MessageLinkException extends Exception {

        @Serial
        private static final long serialVersionUID = -2805786147679905681L;

        public MessageLinkException(Throwable e) {
            super(e);
        }

        public MessageLinkException(String message) {
            super(message);
        }

    }

    @FunctionalInterface
    public interface MessageInfo {

        void accept(final long guildId, final long channelId, final long messageId);

    }
}
