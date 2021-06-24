package com.mcmoddev.mmdbot.utilities.console;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import com.google.common.collect.ImmutableMap;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.helpers.MessageFormatter;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A custom {@link ch.qos.logback.core.Layout} for logging to Discord channels.
 * <p>
 * For each logging level (except {@link Level#OFF} and {@link Level#ALL}, there is an associated emote, used for
 * visual distinction of the log messages.
 * <p>
 * Any {@link IMentionable} in the {@linkplain ILoggingEvent#getArgumentArray() message formatting arguments}, even
 * inside {@link Collection}s and {@link Map}s, are converted into string mentions using
 * {@link IMentionable#getAsMention()}.
 *
 * @author sciwhiz12
 */
public class ConsoleChannelLayout extends LayoutBase<ILoggingEvent> {
    /**
     * The emote used when the given {@link Level} does not a corresponding emote in {@link #LEVEL_TO_EMOTE}.
     */
    private static final String UNKNOWN_EMOTE = ":radio_button:";
    /**
     * An {@linkplain ImmutableMap immutable map} of {@link Level}s to emotes.
     * <p>
     * Used for visual distinction of log messages within the Discord console channel.
     */
    private static final ImmutableMap<Level, String> LEVEL_TO_EMOTE = ImmutableMap.<Level, String>builder()
        .put(Level.ERROR, ":red_square:")
        .put(Level.WARN, ":yellow_circle:")
        .put(Level.INFO, ":white_medium_small_square:")
        .put(Level.DEBUG, ":large_blue_diamond:")
        .put(Level.TRACE, ":small_orange_diamond:")
        .build();

    /**
     * The Prepend level name.
     */
    private boolean prependLevelName = true;

    /**
     * Sets whether to prepend the logging {@link Level} name to the output.
     *
     * @param prependLevelNameIn Whether to prepend level names
     */
    public void setPrependLevelName(final boolean prependLevelNameIn) {
        this.prependLevelName = prependLevelNameIn;
    }

    /**
     * Tries to convert the given object (or any contained objects within) to
     * {@linkplain IMentionable#getAsMention() string mentions}.
     * <p>
     * Mentions will consist of the return value of {@link IMentionable#getAsMention()}, along with the {@linkplain
     * net.dv8tion.jda.api.entities.ISnowflake#getIdLong() snowflake ID}* and the name of the object if available.
     * <p>
     * The rules for conversion are as follows:
     * <ul>
     *     <li>If the object is {@link IMentionable}, cast and convert into mention, then return the new mention.</li>
     *     <li>If the object is a {@link Collection}, call this method on all entries within the collection, then
     *     return a new collection with those modified entries.
     *     <p>If the object is a {@link Set}, then the returned collection is a {@code Set}. Otherwise, the returned
     *     collection is a {@link java.util.List}.</p></li>
     *     <li>If the object is a {@link Map}, call this method on all keys and values within the map, then return an
     *     new {@link ImmutableMap} with those modified entries.</li>
     *     <li>If the object is a {@link Map.Entry}, call this method on the key and value, then return a new
     *     {@link AbstractMap.SimpleImmutableEntry} containing the modified key and value pair.</li>
     *     <li>Otherwise, return the object unmodified.</li>
     * </ul>
     *
     * @param obj The object
     * @return The converted object, according to the conversion rules
     */
    private static Object tryConvertMentionables(final Object obj) {
        if (obj instanceof IMentionable) {
            String name = null;
            if (obj instanceof User) {
                name = ((User) obj).getAsTag();
            } else if (obj instanceof Role) {
                name = ((Role) obj).getName();
            } else if (obj instanceof GuildChannel) {
                name = ((GuildChannel) obj).getName();
            } else if (obj instanceof Emote) {
                name = ((Emote) obj).getName();
            }
            if (name != null) {
                return String.format("%s (%s;`%s`)", ((IMentionable) obj).getAsMention(), name, ((IMentionable)
                    obj).getIdLong());
            } else {
                return String.format("%s (`%s`)", ((IMentionable) obj).getAsMention(),
                    ((IMentionable) obj).getIdLong());
            }
        } else if (obj instanceof Collection) {
            final Stream<Object> stream = ((Collection<?>) obj).stream()
                .map(ConsoleChannelLayout::tryConvertMentionables);
            if (obj instanceof Set) {
                return stream.collect(Collectors.toSet());
            }
            return stream.collect(Collectors.toList());

        } else if (obj instanceof Map) {
            return ((Map<?, ?>) obj).entrySet().stream()
                .map(entry -> new AbstractMap.SimpleImmutableEntry<>(
                    tryConvertMentionables(entry.getKey()), tryConvertMentionables(entry.getValue())
                ))
                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));

        } else if (obj instanceof Map.Entry) {
            final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) obj;
            return new AbstractMap.SimpleImmutableEntry<>(tryConvertMentionables(entry.getKey()),
                tryConvertMentionables(entry.getValue()));

        }
        return obj;
    }

    /**
     * {@inheritDoc}
     *
     * @param event the event
     * @return the string
     */
    @Override
    public String doLayout(final ILoggingEvent event) {
        final var builder = new StringBuilder();
        builder
            .append(LEVEL_TO_EMOTE.getOrDefault(event.getLevel(), UNKNOWN_EMOTE));
        if (prependLevelName) {
            builder
                .append(" ")
                .append(event.getLevel().toString());
        }
        builder
            .append(" [**")
            .append(event.getLoggerName());
        if (event.getMarker() != null) {
            builder
                .append("**/**")
                .append(event.getMarker().getName());
        }
        builder
            .append("**] - ")
            .append(getFormattedMessage(event))
            .append(CoreConstants.LINE_SEPARATOR);
        return builder.toString();
    }

    /**
     * Converts the given {@link ILoggingEvent} into a formatted message string, converting {@link IMentionable}s
     * as needed.
     *
     * @param event The logging event
     * @return The formatted message, with replaced mentions
     * @see #tryConvertMentionables(Object) #tryConvertMentionables(Object)
     */
    private String getFormattedMessage(final ILoggingEvent event) {
        final Object[] arguments = event.getArgumentArray();
        if (event.getArgumentArray() != null) {
            var newArgs = new Object[arguments.length];
            for (var i = 0; i < arguments.length; i++) {
                newArgs[i] = tryConvertMentionables(arguments[i]);
            }

            return MessageFormatter.arrayFormat(event.getMessage(), newArgs).getMessage();
        }
        return event.getFormattedMessage();
    }
}
