package com.mcmoddev.bot.misc;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mcmoddev.bot.MMDBot;
import net.dv8tion.jda.api.entities.*;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
     *
     */
    public static void sleepTimer() {
        //Sleep for a moment to let Discord fill the audit log with the required information.
        //Helps avoid npe's with some events like getting the ban reason of a user from time to time.
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (final InterruptedException exception) {
            MMDBot.LOGGER.trace("InterruptedException", exception);
        }
    }

    //Borrowed from Darkhax's BotBase, all credit for the below methods go to him.
    /**
     *
     */
    public static LocalDateTime getLocalTime(final Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    /**
     *
     * @param fromTime
     * @param toTime
     * @return
     */
    public static String getTimeDifference(final LocalDateTime fromTime, final LocalDateTime toTime) {
        return getTimeDifference(fromTime, toTime, ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.DAYS);
    }

    /**
     *
     * @param from
     * @param to
     * @param units
     * @return
     */
    public static String getTimeDifference(final LocalDateTime fromTime, final LocalDateTime toTime, final ChronoUnit... units) {
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
     *
     * @param text
     * @param url
     * @return
     */
    public static String makeHyperlink(final String text, final String url) {
        return String.format("[%s](%s)", text, url);
    }

    public static Member getMemberFromString(final String memberString, final Guild guild) {
        if (memberString.contains("#")) {
            return guild.getMemberByTag(memberString);
        } else {
            return guild.getMemberById(memberString);
        }
    }

    public static int getNumberOfMatchingReactions(final Message message, final Predicate<Long> predicate) {
        return message
                .getReactions()
                .stream()
                .filter(messageReaction -> messageReaction.getReactionEmote().isEmote())
                .filter(messageReaction -> predicate.test(messageReaction.getReactionEmote().getIdLong()))
                .mapToInt(MessageReaction::getCount)
                .sum();
    }

    public static boolean isReactionGood(final Long emoteID) {
        return Arrays.asList(MMDBot.getConfig().getEmoteIDsGood()).contains(emoteID);
    }

    public static boolean isReactionBad(final Long emoteID) {
        return Arrays.asList(MMDBot.getConfig().getEmoteIDsBad()).contains(emoteID);
    }

    public static boolean isReactionNeedsImprovement(final Long emoteID) {
        return Arrays.asList(MMDBot.getConfig().getEmoteIDsNeedsImprovement()).contains(emoteID);
    }

    public static List<Role> getOldUserRoles(final Guild guild, final Long userID) {
        Map<String, List<String>> roles = getUserToRoleMap();
        if (roles == null)
            return null;

        return roles.get(userID.toString()).stream().map(guild::getRoleById).collect(Collectors.toList());
    }

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

    public static Map<String, List<String>> getUserToRoleMap() {
        final File roleFile = new File(STICKY_ROLES_FILE_PATH);
        if (!roleFile.exists())
            return null;
        Map<String, List<String>> roles = null;
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(roleFile), StandardCharsets.UTF_8)) {
            Type typeOfHashMap = new TypeToken<Map<String, List<String>>>() {}.getType();
            roles = new Gson().fromJson(reader, typeOfHashMap);
        } catch (final IOException exception) {
            MMDBot.LOGGER.trace("Failed to read sticky roles file...", exception);
        }
        return roles;
    }

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

	public static Map<String, Instant> getUserJoinTimeMap() {
		final File joinTimesFile = new File(USER_JOIN_TIMES_FILE_PATH);
		if (!joinTimesFile.exists())
			return null;
		Map<String, Instant> userJoinTimes = null;
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(joinTimesFile), StandardCharsets.UTF_8)) {
			Type typeOfHashMap = new TypeToken<Map<String, Instant>>() {}.getType();
			userJoinTimes = new Gson().fromJson(reader, typeOfHashMap);
		} catch (final IOException exception) {
			MMDBot.LOGGER.trace("Failed to read user join times file...", exception);
		}
		return userJoinTimes;
	}

	public static Instant getMemberJoinTime(Member member) {
    	final Map<String, Instant> userJoinTimes = getUserJoinTimeMap();
    	final String memberID = member.getId();
    	return userJoinTimes != null && userJoinTimes.containsKey(memberID) ?
			userJoinTimes.get(memberID) :
			member.getTimeJoined().toInstant();
	}

}
