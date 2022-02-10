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

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class representing Discord Markdown Timestamps.
 * <br>This class implements {@link #toString()} such that it can be directly included in message content.
 *
 * <p>These DiscordTimestamps are rendered by the individual receiving Discord client in a local timezone and language format.
 * Each timestamp can be displayed with different {@link TimeFormat TimeFormats}.
 */
public class DiscordTimestamp {
    private final TimeFormat format;
    private final long timestamp;

    protected DiscordTimestamp(TimeFormat format, long DiscordTimestamp) {
        this.format = format;
        this.timestamp = DiscordTimestamp;
    }

    @Nonnull
    public TimeFormat getFormat() {
        return format;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Nonnull
    public Instant toInstant() {
        return Instant.ofEpochMilli(timestamp);
    }

    @Nonnull
    public DiscordTimestamp plus(long millis) {
        return new DiscordTimestamp(format, timestamp + millis);
    }

    @Nonnull
    public DiscordTimestamp plus(@Nonnull Duration duration) {
        return plus(duration.toMillis());
    }

    @Nonnull
    public DiscordTimestamp minus(long millis) {
        return new DiscordTimestamp(format, timestamp - millis);
    }

    @Nonnull
    public DiscordTimestamp minus(@Nonnull Duration duration) {
        return minus(duration.toMillis());
    }

    @Override
    public String toString() {
        return "<t:" + timestamp / 1000 + ":" + format.getStyle() + ">";
    }


    public enum TimeFormat {
        /**
         * Formats time as {@code 18:49} or {@code 6:49 PM}
         */
        TIME_SHORT("t"),
        /**
         * Formats time as {@code 18:49:26} or {@code 6:49:26 PM}
         */
        TIME_LONG("T"),
        /**
         * Formats date as {@code 16/06/2021} or {@code 06/16/2021}
         */
        DATE_SHORT("d"),
        /**
         * Formats date as {@code 16 June 2021}
         */
        DATE_LONG("D"),
        /**
         * Formats date and time as {@code 16 June 2021 18:49} or {@code June 16, 2021 6:49 PM}
         */
        DATE_TIME_SHORT("f"),
        /**
         * Formats date and time as {@code Wednesday, 16 June 2021 18:49} or {@code Wednesday, June 16, 2021 6:49 PM}
         */
        DATE_TIME_LONG("F"),
        /**
         * Formats date and time as relative {@code 18 minutes ago} or {@code 2 days ago}
         */
        RELATIVE("R");

        /**
         * The default time format used when no style is provided.
         */
        public static final TimeFormat DEFAULT = DATE_TIME_SHORT;

        public static final Pattern MARKDOWN = Pattern.compile("<t:(?<time>-?\\d{1,17})(?::(?<style>[tTdDfFR]))?>");

        private final String style;

        TimeFormat(String style) {
            this.style = style;
        }

        @Nonnull
        public String getStyle() {
            return style;
        }

        @Nonnull
        public static TimeFormat fromStyle(@Nonnull String style) {
            for (TimeFormat format : values()) {
                if (format.style.equals(style))
                    return format;
            }
            return DEFAULT;
        }

        /**
         * Parses the provided markdown into a {@link DiscordTimestamp} instance.
         * <br>This is the reverse operation for the {@link DiscordTimestamp#toString() timestamp.toString()} representation.
         *
         * @param markdown The markdown for the timestamp value
         * @return {@link DiscordTimestamp} instance for the provided markdown
         * @throws IllegalArgumentException If the provided markdown is null or does not match the {@link #MARKDOWN} pattern
         */
        @Nonnull
        public static DiscordTimestamp parse(@Nonnull String markdown) {
            Matcher matcher = MARKDOWN.matcher(markdown.trim());
            if (!matcher.find())
                throw new IllegalArgumentException("Invalid markdown format! Provided: " + markdown);
            String format = matcher.group("style");
            return new DiscordTimestamp(format == null ? DEFAULT : fromStyle(format), Long.parseLong(matcher.group("time")) * 1000);
        }

        @Nonnull
        public String format(@Nonnull TemporalAccessor temporal) {
            long DiscordTimestamp = Instant.from(temporal).toEpochMilli();
            return format(DiscordTimestamp);
        }

        @Nonnull
        public String format(long timestamp) {
            return "<t:" + timestamp / 1000 + ":" + style + ">";
        }

        @Nonnull
        public DiscordTimestamp fromInstant(@Nonnull Instant instant) {
            return new DiscordTimestamp(this, instant.toEpochMilli());
        }

        @Nonnull
        public DiscordTimestamp fromTimestamp(long timestamp) {
            return new DiscordTimestamp(this, timestamp);
        }

        @Nonnull
        public DiscordTimestamp now() {
            return new DiscordTimestamp(this, System.currentTimeMillis());
        }

        @Nonnull
        public DiscordTimestamp after(@Nonnull Duration duration) {
            return now().plus(duration);
        }

        @Nonnull
        public DiscordTimestamp after(long millis) {
            return now().plus(millis);
        }

        @Nonnull
        public DiscordTimestamp before(@Nonnull Duration duration) {
            return now().minus(duration);
        }

        @Nonnull
        public DiscordTimestamp before(long millis) {
            return now().minus(millis);
        }
    }
}
