package com.mcmoddev.bot.misc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

import com.mcmoddev.bot.MMDBot;

/**
*
*/
public final class Utils {

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

    //Borrowed from Darkhax's BotBase, all credit for the below methods go to him (Should be replaced or used from the lib itself in future updates)
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
}
