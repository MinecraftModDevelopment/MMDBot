package com.mcmoddev.bot.misc;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static final SimpleDateFormat DATE = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    public static void sleepTimer() {
        //Sleep for a moment to let Discord fill the audit log with the required information.
        //Helps avoid npe's with some events like getting the ban reason of a user from time to time.
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException exception) {
        }
    }

    //Borrowed from Darkhax's BotBase, all credit for the below methods go to him (Should be replaced or used from the lib itself in future updates)
    public static LocalDateTime getLocalTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    public static String getTimeDifference(LocalDateTime from, LocalDateTime to) {
        return getTimeDifference(from, to, ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.DAYS);
    }

    public static String getTimeDifference(LocalDateTime from, LocalDateTime to, ChronoUnit... units) {
        final StringJoiner joiner = new StringJoiner(", ");
        LocalDateTime temp = LocalDateTime.from(from);
        for (final ChronoUnit unit : units) {
            final long time = temp.until(to, unit);
            if (time > 0) {
                temp = temp.plus(time, unit);
                final String unitName = unit.toString();
                joiner.add(time + " " + (time < 2 && unitName.endsWith("s") ? unitName.substring(0, unitName.length() - 1) : unitName));
            }
        }

        return joiner.toString();
    }

    public static String makeHyperlink(String text, String url) {
        return String.format("[%s](%s)", text, url);
    }
}
