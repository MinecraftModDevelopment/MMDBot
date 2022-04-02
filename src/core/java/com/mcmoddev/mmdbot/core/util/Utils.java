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
package com.mcmoddev.mmdbot.core.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class Utils {

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

    /**
     * @return the current public IP address of the machine.
     */
    public static String getPublicIPAddress() {
        try {
            URL url = new URL("https://api.ipify.org");
            try (InputStreamReader sr = new InputStreamReader(url.openStream());
                 BufferedReader sc = new BufferedReader(sr)) {
                return sc.readLine().trim();
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the {@link OffsetDateTime} from an Instant value, relative to UTC+0 / GMT+0.
     * Overtakes the last system which used LocalDateTime which was unpredictable and caused confusion among developers.
     *
     * @param instant the instant
     * @return OffsetDateTime. Offset from UTC+0.
     */
    public static OffsetDateTime getTimeFromUTC(final Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public static String uppercaseFirstLetter(final String string) {
        return string.substring(0, 1).toUpperCase(Locale.ROOT) + string.substring(1);
    }

    public static String makeHyperlink(String masked, String link) {
        return "[%s](%s)".formatted(masked, link);
    }

    /**
     * Schedules a task to run at the specified {@code date}.
     * @param service the service to schedule the task on.
     * @param task the task to schedule.
     * @param date the time at which to schedule the task.
     */
    public static void scheduleTask(final ScheduledExecutorService service, final Runnable task, final Instant date) {
        service.schedule(task, date.atOffset(ZoneOffset.UTC).toEpochSecond() -
            Instant.now().atOffset(ZoneOffset.UTC).toEpochSecond(), TimeUnit.SECONDS);
    }

    /**
     * Checks if a word starts with a vowel.
     * @param word the word to check
     * @return if the word starts with a vowel
     */
    public static boolean startWithVowel(String word) {
        if (word.length() < 1) return false;
        switch (word.charAt(0)) {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
            case 'A':
            case 'E':
            case 'I':
            case 'O':
            case 'U':
                return true;
            default:
                return false;
        }
    }
}
