package com.mcmoddev.mmdbot.core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mcmoddev.mmdbot.MMDBot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.Role;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

/**
 *
 */
public final class Utils {

	public static final String STICKY_ROLES_FILE_PATH = "mmdbot_sticky_roles.json";
	public static final String USER_JOIN_TIMES_FILE_PATH = "mmdbot_user_join_times.json";

	/**
	 *
	 */
	private Utils() {
		// Shut up Sonarqube warns
	}

	/**
	 * A sleep timer to help with getting some information from the audit log by
	 * delaying the running code before we get the info from the audit log.
	 * <p>
	 * Helps prevent some NullPointerExceptions that we where getting sometimes.
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
		}
	}

	/**
	 * Borrowed from Darkhax's BotBase, all credit for the below methods go to him.
	 * The Bot Base repo and code is now deleted if you need it for reference Proxy has a copy. Dark may also have one.
	 */
	public static LocalDateTime getLocalTime(final Instant instant) {
		return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
	}

	/**
	 * @param fromTime The starting time.
	 * @param toTime   The end time.
	 * @return The difference between the two times.
	 */
	public static String getTimeDifference(final LocalDateTime fromTime, final LocalDateTime toTime) {
		return getTimeDifference(fromTime, toTime, ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.DAYS);
	}

	/**
	 * @param fromTime The starting time.
	 * @param toTime   The end time.
	 * @param units
	 * @return The difference between the two times.
	 */
	public static String getTimeDifference(final LocalDateTime fromTime, final LocalDateTime toTime,
										   final ChronoUnit... units) {
		final StringJoiner joiner = new StringJoiner(", ");
		LocalDateTime temp = LocalDateTime.from(fromTime);
		for (final ChronoUnit unit : units) {
			final long time = temp.until(toTime, unit);
			if (time > 0) {
				temp = temp.plus(time, unit);
				final String unitName = unit.toString();
				joiner.add(time + " " + (time < 2 && unitName.endsWith("s") ? unitName.substring(0, unitName.length() - 1) : unitName));
			}
		}
		return joiner.toString();
	}

	/**
	 * @param text The text to display for the link.
	 * @param url  The URL the text points to.
	 * @return The new hyperlink.
	 */
	public static String makeHyperlink(final String text, final String url) {
		return String.format("[%s](%s)", text, url);
	}

	/**
	 * @param memberString The members string name or ID.
	 * @param guild        The guild we are currently in.
	 * @return
	 */
	public static Member getMemberFromString(final String memberString, final Guild guild) {
		final Matcher matcher = Message.MentionType.USER.getPattern().matcher(memberString);
		if (matcher.matches()) {
			return guild.getMemberById(matcher.group(1));
		} else if (memberString.contains("#")) {
			return guild.getMemberByTag(memberString);
		} else {
			return guild.getMemberById(memberString);
		}
	}

	/**
	 * @param message   The message we are getting the number of matching reactions from.
	 * @param predicate
	 * @return The amount of matching reactions.
	 */
	public static int getNumberOfMatchingReactions(final Message message, final Predicate<Long> predicate) {
		return message
			.getReactions()
			.stream()
			.filter(messageReaction -> messageReaction.getReactionEmote().isEmote())
			.filter(messageReaction -> predicate.test(messageReaction.getReactionEmote().getIdLong()))
			.mapToInt(MessageReaction::getCount)
			.sum();
	}

	/**
	 * @param emoteID The ID of the good request emoticon.
	 * @return
	 */
	public static boolean isReactionGood(final Long emoteID) {
		return Arrays.asList(MMDBot.getConfig().getEmoteIDsGood()).contains(emoteID);
	}

	/**
	 * @param emoteID The ID of the request bad emoticon.
	 * @return
	 */
	public static boolean isReactionBad(final Long emoteID) {
		return Arrays.asList(MMDBot.getConfig().getEmoteIDsBad()).contains(emoteID);
	}

	/**
	 * @param emoteID The ID of the needs improvement emoticon.
	 * @return
	 */
	public static boolean isReactionNeedsImprovement(final Long emoteID) {
		return Arrays.asList(MMDBot.getConfig().getEmoteIDsNeedsImprovement()).contains(emoteID);
	}

	/**
	 * Get the users roles when they leave the guild with the user leave event.
	 *
	 * @param guild  The guild we are in.
	 * @param userID The users ID.
	 * @return
	 */
	public static List<Role> getOldUserRoles(final Guild guild, final Long userID) {
		Map<String, List<String>> roles = getUserToRoleMap();
		if (roles == null)
			return null;

		return roles.get(userID.toString()).stream().map(guild::getRoleById).collect(Collectors.toList());
	}

	/**
	 * Saves the roles and a user ID to a file so that if the user comes back in the future we can re-apply
	 * the roles rather than have them go through the role-request channel again.
	 * Saves us Moderators some time.
	 *
	 * @param userID The user ID of the user leaving the guild.
	 * @param roles  The roles the user had before they left.
	 */
	public static void writeUserRoles(final Long userID, final List<Role> roles) {
		final File roleFile = new File(STICKY_ROLES_FILE_PATH);
		Map<String, List<String>> userToRoleMap = getUserToRoleMap();
		if (userToRoleMap == null) {
			userToRoleMap = new HashMap<>();
		}
		userToRoleMap.put(userID.toString(), roles.stream().map(ISnowflake::getId).collect(Collectors.toList()));
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(roleFile), StandardCharsets.UTF_8)) {
			new Gson().toJson(userToRoleMap, writer);
		} catch (final FileNotFoundException exception) {
			MMDBot.LOGGER.error("An FileNotFound occurred saving sticky roles...", exception);
		} catch (final IOException exception) {
			MMDBot.LOGGER.error("An IOException occurred saving sticky roles...", exception);
		}
	}

	/**
	 * @return
	 */
	public static Map<String, List<String>> getUserToRoleMap() {
		final File roleFile = new File(STICKY_ROLES_FILE_PATH);
		if (!roleFile.exists())
			return null;
		Map<String, List<String>> roles = null;
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(roleFile), StandardCharsets.UTF_8)) {
			Type typeOfHashMap = new TypeToken<Map<String, List<String>>>() {
			}.getType();
			roles = new Gson().fromJson(reader, typeOfHashMap);
		} catch (final IOException exception) {
			MMDBot.LOGGER.trace("Failed to read sticky roles file...", exception);
		}
		return roles;
	}

	/**
	 * Write the users join time to a file so that users don't loose the first join time when they leave the guild.
	 *
	 * @param userID   The users ID.
	 * @param joinTime The join time of the user.
	 */
	public static void writeUserJoinTimes(final String userID, final Instant joinTime) {
		final File userJoinTimesFile = new File(USER_JOIN_TIMES_FILE_PATH);
		Map<String, Instant> userJoinTimes = getUserJoinTimeMap();
		if (userJoinTimes == null) {
			userJoinTimes = new HashMap<>();
		}
		userJoinTimes.put(userID, joinTime);
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(userJoinTimesFile), StandardCharsets.UTF_8)) {
			new Gson().toJson(userJoinTimes, writer);
		} catch (final FileNotFoundException exception) {
			MMDBot.LOGGER.error("An FileNotFound occurred saving user join times...", exception);
		} catch (final IOException exception) {
			MMDBot.LOGGER.error("An IOException occurred saving user join times...", exception);
		}
	}

	/**
	 * @return
	 */
	public static Map<String, Instant> getUserJoinTimeMap() {
		final File joinTimesFile = new File(USER_JOIN_TIMES_FILE_PATH);
		if (!joinTimesFile.exists())
			return null;
		Map<String, Instant> userJoinTimes = null;
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(joinTimesFile), StandardCharsets.UTF_8)) {
			Type typeOfHashMap = new TypeToken<Map<String, Instant>>() {
			}.getType();
			userJoinTimes = new Gson().fromJson(reader, typeOfHashMap);
		} catch (final IOException exception) {
			MMDBot.LOGGER.trace("Failed to read user join times file...", exception);
		}
		return userJoinTimes;
	}

	/**
	 * @param member The user.
	 * @return The users join time.
	 */
	public static Instant getMemberJoinTime(Member member) {
		final Map<String, Instant> userJoinTimes = getUserJoinTimeMap();
		final String memberID = member.getId();
		return userJoinTimes != null && userJoinTimes.containsKey(memberID) ?
			userJoinTimes.get(memberID) :
			member.getTimeJoined().toInstant();
	}

	/**
	 * Allows a map to be sorted.
	 *
	 * @param map The map to sort.
	 * @param invert Whether or not order should be inverted.
	 * @return The sorted map.
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue (Map<K, V> map, boolean invert) {

		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, Comparator.comparing(Map.Entry<K, V>::getValue));

		if (invert) {
			list = Lists.reverse(list);
		}

		final Map<K, V> result = new LinkedHashMap<>();

		for (final Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}

	/**
	 * Converts a map into a multi-line string.
	 *
	 * @param map The map to print.
	 * @return The string value of the map.
	 */
	public static <K, V> String mapToString (Map<K, V> map) {

		String output = "";

		for (final Map.Entry<K, V> entry : map.entrySet()) {
			output += entry.getKey().toString() + ": " + entry.getValue().toString() + System.lineSeparator();
		}

		return output;
	}
}
