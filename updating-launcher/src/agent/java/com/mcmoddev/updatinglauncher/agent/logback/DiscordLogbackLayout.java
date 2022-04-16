/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
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
package com.mcmoddev.updatinglauncher.agent.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.helpers.MessageFormatter;

import javax.annotation.Nullable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiscordLogbackLayout extends LayoutBase<ILoggingEvent> {
    /**
     * The emote used when the given {@link Level} does not a corresponding emote in {@link #LEVEL_TO_EMOTE}.
     */
    private static final String UNKNOWN_EMOTE = ":radio_button:";
    /**
     * An {@linkplain Map immutable map} of {@link Level}s to emotes.
     * <p>
     * Used for visual distinction of log messages within the Discord console channel.
     */
    public static final Map<Level, String> LEVEL_TO_EMOTE = Map.of(
        Level.ERROR, ":red_square:",
        Level.WARN, ":yellow_circle:",
        Level.INFO, ":white_medium_small_square:",
        Level.DEBUG, ":large_blue_diamond:",
        Level.TRACE, ":small_orange_diamond:"
    );
    private static final boolean JDA_EXISTS;

    static {
        var jdaExists = true;
        try {
            Class.forName("net.dv8tion.jda.api.Jda");
        } catch (ClassNotFoundException e) {
            jdaExists = false;
        }
        JDA_EXISTS = jdaExists;
    }

    private static Object tryFormat(final Object obj) {
        if (JDA_EXISTS) {
            final Object jda = JDAFormatter.format(obj);
            if (jda != null) {
                return jda;
            }
        }
        if (obj instanceof Collection<?> col) {
            final Stream<Object> stream = col.stream()
                .map(DiscordLogbackLayout::tryFormat);
            if (obj instanceof Set) {
                return stream.collect(Collectors.toSet());
            }
            return stream.collect(Collectors.toList());

        } else if (obj instanceof Map) {
            return ((Map<?, ?>) obj).entrySet().stream()
                .map(entry -> new AbstractMap.SimpleImmutableEntry<>(
                    tryFormat(entry.getKey()), tryFormat(entry.getValue())
                ))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        } else if (obj instanceof final Map.Entry<?, ?> entry) {
            return new AbstractMap.SimpleImmutableEntry<>(tryFormat(entry.getKey()),
                tryFormat(entry.getValue()));

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
        final StringBuilder builder = new StringBuilder(2000);
        builder
            .append(LEVEL_TO_EMOTE.getOrDefault(event.getLevel(), UNKNOWN_EMOTE));
        builder
            .append(" ")
            .append(event.getLevel().toString());
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

        if (event.getThrowableProxy() != null) {
            final String stacktrace = buildStacktrace(event.getThrowableProxy());
            builder.append("Stacktrace: ");
            if (stacktrace.length() > 1800) {
                builder.append("*Too long to be displayed.*");
            } else {
                builder.append(CoreConstants.LINE_SEPARATOR)
                    .append("```ansi")
                    .append(CoreConstants.LINE_SEPARATOR)
                    .append(stacktrace)
                    .append("```");
            }
        }
        return builder.toString();
    }

    private String buildStacktrace(IThrowableProxy exception) {
        final var builder = new StringBuilder();
        for (int i = 0; i < exception.getStackTraceElementProxyArray().length; i++) {
            builder.append("\t at ").append(exception.getStackTraceElementProxyArray()[i].toString())
                .append(CoreConstants.LINE_SEPARATOR);
        }
        return builder.toString();
    }

    /**
     * Converts the given {@link ILoggingEvent} into a formatted message string, converting {@link IMentionable}s
     * as needed.
     *
     * @param event The logging event
     * @return The formatted message, with replaced mentions
     * @see #tryFormat(Object) #tryConvertMentionables(Object)
     */
    private String getFormattedMessage(final ILoggingEvent event) {
        final Object[] arguments = event.getArgumentArray();
        if (event.getArgumentArray() != null) {
            var newArgs = new Object[arguments.length];
            for (var i = 0; i < arguments.length; i++) {
                newArgs[i] = tryFormat(arguments[i]);
            }

            return MessageFormatter.arrayFormat(event.getMessage(), newArgs).getMessage();
        }
        return event.getFormattedMessage();
    }

    public static final class JDAFormatter {

        /**
         * Tries to convert the given object (or any contained objects within) to
         * {@linkplain IMentionable#getAsMention() string mentions}.
         * <p>
         * Mentions will consist of the return value of {@link IMentionable#getAsMention()}, along with the {@linkplain
         * net.dv8tion.jda.api.entities.ISnowflake#getIdLong() snowflake ID}* and the name of the object if available.
         * <p>
         * If the object is {@link IMentionable}, cast and convert into mention, then return the new mention.
         *
         * @param obj The object
         * @return The converted object, according to the conversion rules
         */
        @Nullable
        public static Object format(final Object obj) {
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
            } else {
                return null;
            }
        }
    }
}
