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
 * Each DiscordTimestamp can be displayed with different {@link TimeFormat TimeFormats}.
 */
public class DiscordTimestamp {
    private final TimeFormat format;
    private final long DiscordTimestamp;

    protected DiscordTimestamp(TimeFormat format, long DiscordTimestamp) {
        this.format = format;
        this.DiscordTimestamp = DiscordTimestamp;
    }

    /**
     * The {@link TimeFormat} used to display this DiscordTimestamp.
     *
     * @return The {@link TimeFormat}
     */
    @Nonnull
    public TimeFormat getFormat() {
        return format;
    }

    /**
     * The unix epoch DiscordTimestamp for this markdown DiscordTimestamp.
     * <br>This is similar to {@link System#currentTimeMillis()} and provided in millisecond precision for easier compatibility.
     * Discord uses seconds precision instead.
     *
     * @return The millisecond unix epoch DiscordTimestamp
     */
    public long getDiscordTimestamp() {
        return DiscordTimestamp;
    }

    /**
     * Shortcut for {@code Instant.ofEpochMilli(getDiscordTimestamp())}.
     *
     * @return The {@link Instant} of this DiscordTimestamp
     */
    @Nonnull
    public Instant toInstant() {
        return Instant.ofEpochMilli(DiscordTimestamp);
    }

    /**
     * Creates a new DiscordTimestamp instance with the provided offset into the future relative to the current DiscordTimestamp.
     *
     * @param millis The millisecond offset for the new DiscordTimestamp
     * @return Copy of this DiscordTimestamp with the relative offset
     * @see #plus(Duration)
     */
    @Nonnull
    public DiscordTimestamp plus(long millis) {
        return new DiscordTimestamp(format, DiscordTimestamp + millis);
    }

    /**
     * Creates a new DiscordTimestamp instance with the provided offset into the future relative to the current DiscordTimestamp.
     *
     * @param duration The offset for the new DiscordTimestamp
     * @return Copy of this DiscordTimestamp with the relative offset
     * @throws IllegalArgumentException If the provided duration is null
     * @see #plus(long)
     */
    @Nonnull
    public DiscordTimestamp plus(@Nonnull Duration duration) {
        return plus(duration.toMillis());
    }

    /**
     * Creates a new DiscordTimestamp instance with the provided offset into the past relative to the current DiscordTimestamp.
     *
     * @param millis The millisecond offset for the new DiscordTimestamp
     * @return Copy of this DiscordTimestamp with the relative offset
     * @see #minus(Duration)
     */
    @Nonnull
    public DiscordTimestamp minus(long millis) {
        return new DiscordTimestamp(format, DiscordTimestamp - millis);
    }

    /**
     * Creates a new DiscordTimestamp instance with the provided offset into the past relative to the current DiscordTimestamp.
     *
     * @param duration The offset for the new DiscordTimestamp
     * @return Copy of this DiscordTimestamp with the relative offset
     * @throws IllegalArgumentException If the provided duration is null
     * @see #minus(long)
     */
    @Nonnull
    public DiscordTimestamp minus(@Nonnull Duration duration) {
        return minus(duration.toMillis());
    }

    @Override
    public String toString() {
        return "<t:" + DiscordTimestamp / 1000 + ":" + format.getStyle() + ">";
    }


    /**
     * Utility enum used to provide different markdown styles for DiscordTimestamps.
     * <br>These can be used to represent a unix epoch DiscordTimestamp in different formats.
     *
     * <p>These DiscordTimestamps are rendered by the individual receiving discord client in a local timezone and language format.
     * Each DiscordTimestamp can be displayed with different {@link net.dv8tion.jda.api.utils.TimeFormat TimeFormats}.
     *
     * <h2>Example</h2>
     * <pre>{@code
     * channel.sendMessage("Current Time: " + TimeFormat.RELATIVE.now()).queue();
     * channel.sendMessage("Uptime: " + TimeFormat.RELATIVE.format(getStartTime())).queue();
     * }</pre>
     */
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
        RELATIVE("R"),
        ;

        /**
         * The default time format used when no style is provided.
         */
        public static final TimeFormat DEFAULT = DATE_TIME_SHORT;

        /**
         * {@link Pattern} used for {@link #parse(String)}.
         *
         * <h2>Groups</h2>
         * <table>
         *   <caption style="display: none">Javadoc is stupid, this is not a required tag</caption>
         *   <tr>
         *     <th>Index</th>
         *     <th>Name</th>
         *     <th>Description</th>
         *   </tr>
         *   <tr>
         *     <td>0</td>
         *     <td>N/A</td>
         *     <td>The entire DiscordTimestamp markdown</td>
         *   </tr>
         *   <tr>
         *     <td>1</td>
         *     <td>time</td>
         *     <td>The DiscordTimestamp value as a unix epoch in second precision</td>
         *   </tr>
         *   <tr>
         *     <td>2</td>
         *     <td>style</td>
         *     <td>The style used for displaying the DiscordTimestamp (single letter flag)</td>
         *   </tr>
         * </table>
         *
         * @see #parse(String)
         */
        public static final Pattern MARKDOWN = Pattern.compile("<t:(?<time>-?\\d{1,17})(?::(?<style>[tTdDfFR]))?>");

        private final String style;

        TimeFormat(String style) {
            this.style = style;
        }

        /**
         * The display style flag used for the markdown representation.
         * <br>This is encoded into the markdown to provide the client with rendering context.
         *
         * @return The style flag
         */
        @Nonnull
        public String getStyle() {
            return style;
        }

        /**
         * Returns the time format for the provided style flag.
         *
         * @param style The style flag
         * @return The representative TimeFormat or {@link #DEFAULT} if none could be identified
         * @throws IllegalArgumentException If the provided style string is not exactly one character long
         */
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
         * <br>This is the reverse operation for the {@link DiscordTimestamp#toString() DiscordTimestamp.toString()} representation.
         *
         * @param markdown The markdown for the DiscordTimestamp value
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

        /**
         * Formats the provided {@link TemporalAccessor} instance into a DiscordTimestamp markdown.
         *
         * @param temporal The {@link TemporalAccessor}
         * @return The markdown string with this encoded style
         * @throws IllegalArgumentException    If the provided temporal instance is null
         * @throws java.time.DateTimeException If the temporal accessor cannot be converted to an instant
         * @see Instant#from(TemporalAccessor)
         */
        @Nonnull
        public String format(@Nonnull TemporalAccessor temporal) {
            long DiscordTimestamp = Instant.from(temporal).toEpochMilli();
            return format(DiscordTimestamp);
        }

        /**
         * Formats the provided unix epoch DiscordTimestamp into a DiscordTimestamp markdown.
         * <br>Compatible with millisecond precision DiscordTimestamps such as the ones provided by {@link System#currentTimeMillis()}.
         *
         * @param DiscordTimestamp The millisecond epoch
         * @return The markdown string with this encoded style
         */
        @Nonnull
        public String format(long DiscordTimestamp) {
            return "<t:" + DiscordTimestamp / 1000 + ":" + style + ">";
        }

        /**
         * Converts the provided {@link Instant} into a {@link DiscordTimestamp} with this style.
         *
         * @param instant The {@link Instant} for the DiscordTimestamp
         * @return The {@link DiscordTimestamp} instance
         * @throws IllegalArgumentException If null is provided
         * @see #now()
         * @see #atDiscordTimestamp(long)
         * @see Instant#from(TemporalAccessor)
         * @see Instant#toEpochMilli()
         */
        @Nonnull
        public DiscordTimestamp atInstant(@Nonnull Instant instant) {
            return new DiscordTimestamp(this, instant.toEpochMilli());
        }

        /**
         * Converts the provided unix epoch DiscordTimestamp into a {@link DiscordTimestamp} with this style.
         * <br>Compatible with millisecond precision DiscordTimestamps such as the ones provided by {@link System#currentTimeMillis()}.
         *
         * @param DiscordTimestamp The millisecond epoch
         * @return The {@link DiscordTimestamp} instance
         * @see #now()
         */
        @Nonnull
        public DiscordTimestamp atDiscordTimestamp(long DiscordTimestamp) {
            return new DiscordTimestamp(this, DiscordTimestamp);
        }

        /**
         * Shortcut for {@code style.atDiscordTimestamp(System.currentTimeMillis())}.
         *
         * @return {@link DiscordTimestamp} instance for the current time
         * @see DiscordTimestamp#plus(long)
         * @see DiscordTimestamp#minus(long)
         */
        @Nonnull
        public DiscordTimestamp now() {
            return new DiscordTimestamp(this, System.currentTimeMillis());
        }

        /**
         * Shortcut for {@code style.now().plus(duration)}.
         *
         * @param duration The {@link Duration} offset into the future
         * @return {@link DiscordTimestamp} instance for the offset relative to the current time
         * @throws IllegalArgumentException If null is provided
         * @see #now()
         * @see DiscordTimestamp#plus(Duration)
         */
        @Nonnull
        public DiscordTimestamp after(@Nonnull Duration duration) {
            return now().plus(duration);
        }

        /**
         * Shortcut for {@code style.now().plus(millis)}.
         *
         * @param millis The millisecond offset into the future
         * @return {@link DiscordTimestamp} instance for the offset relative to the current time
         * @see #now()
         * @see DiscordTimestamp#plus(long)
         */
        @Nonnull
        public DiscordTimestamp after(long millis) {
            return now().plus(millis);
        }

        /**
         * Shortcut for {@code style.now().minus(duration)}.
         *
         * @param duration The {@link Duration} offset into the past
         * @return {@link DiscordTimestamp} instance for the offset relative to the current time
         * @throws IllegalArgumentException If null is provided
         * @see #now()
         * @see DiscordTimestamp#minus(Duration)
         */
        @Nonnull
        public DiscordTimestamp before(@Nonnull Duration duration) {
            return now().minus(duration);
        }

        /**
         * Shortcut for {@code style.now().minus(millis)}.
         *
         * @param millis The millisecond offset into the past
         * @return {@link DiscordTimestamp} instance for the offset relative to the current time
         * @see #now()
         * @see DiscordTimestamp#minus(long)
         */
        @Nonnull
        public DiscordTimestamp before(long millis) {
            return now().minus(millis);
        }
    }
}
