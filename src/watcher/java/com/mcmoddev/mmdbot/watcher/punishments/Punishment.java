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
package com.mcmoddev.mmdbot.watcher.punishments;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Punishment(ActionType type, Duration duration) {

    public Punishment {
        Objects.requireNonNull(type);
    }

    public enum ActionType {
        BAN, MUTE, KICK
    }

    public void punish(Member member, String reason, Runnable whenDone) {
        if (whenDone == null) whenDone = () -> {};
        final Runnable finalWhenDone = whenDone;
        final var memberId = member.getId();
        switch (type) {
            case BAN -> {
                if (duration == null || duration.toMillis() <= 0) {
                    member.ban(0, reason).queue();
                } else {
                    member.ban(0, reason)
                        .flatMap(o -> {
                            finalWhenDone.run();
                            return member.getGuild().unban(User.fromId(memberId));
                        })
                        .queueAfter(duration.toSeconds(), TimeUnit.MILLISECONDS);
                }
            }
            case KICK -> member.kick(reason).queue($ -> finalWhenDone.run());
            case MUTE -> member.timeoutFor(duration == null ? Duration.of(28, ChronoUnit.DAYS) : duration)
                .reason(reason)
                .queue($ -> finalWhenDone.run());
        }
    }

    public static class Serializer implements TypeSerializer<Punishment> {

        @Override
        public Punishment deserialize(final Type type, final ConfigurationNode node) {
            final String str = node.getString();
            if (str == null) {
                return null;
            }
            final var split = str.split(" ");
            final var actionType = ActionType.valueOf(split[0].toUpperCase(Locale.ROOT));
            if (split.length > 1 && !split[1].isBlank()) {
                return new Punishment(actionType, deserialize(split[1]));
            } else {
                return new Punishment(actionType, null);
            }
        }

        @Override
        public void serialize(final Type type, @Nullable final Punishment obj, final ConfigurationNode node) throws SerializationException {
            if (obj == null) {
                node.raw(null);
                return;
            }
            node.set(obj.type().toString() + (obj.duration() == null ? "" : " " + serialize(obj.duration())));
        }

        private static final Pattern DURATION =
            Pattern.compile("(-?)" + "(?:(?<days>\\d+)[dD])?" + "(?:(?<hours>\\d+)[hH])?" + "(?:(?<minutes>\\d+)[mM])?"
                + "(?:(?<seconds>\\d+)[sS])?");

        @Nullable
        private static Duration deserialize(@Nullable String string) {
            if (string == null || string.isBlank()) {
                return null;
            }

            if (string.equals("0")) {
                return Duration.ZERO;
            }

            Duration duration = Duration.ZERO;
            final Matcher matcher = DURATION.matcher(string);
            if (!matcher.matches()) {
                return null;
            }

            final @Nullable String days = matcher.group("days");
            if (days != null) {
                duration = duration.plusDays(Long.parseLong(days));
            }

            final @Nullable String hours = matcher.group("hours");
            if (hours != null) {
                duration = duration.plusHours(Long.parseLong(hours));
            }

            final @Nullable String minutes = matcher.group("minutes");
            if (minutes != null) {
                duration = duration.plusMinutes(Long.parseLong(minutes));
            }

            final @Nullable String seconds = matcher.group("seconds");
            if (seconds != null) {
                duration = duration.plusSeconds(Long.parseLong(seconds));
            }

            return duration;
        }

        private static String serialize(Duration duration) {
            duration = duration.withNanos(0).minusMillis(duration.toMillisPart());
            if (duration.isZero()) {
                return "0";
            }
            final StringBuilder builder = new StringBuilder();
            if (duration.isNegative()) {
                builder.append('-');
            }
            final long days = duration.toDaysPart();
            if (days > 0) {
                builder.append(days).append('d');
            }
            final int hours = duration.toHoursPart();
            if (hours > 0) {
                builder.append(hours).append('h');
            }
            final int minutes = duration.toMinutesPart();
            if (minutes > 0) {
                builder.append(minutes).append('h');
            }
            final int seconds = duration.toSecondsPart();
            if (seconds > 0) {
                builder.append(seconds).append('h');
            }
            return builder.toString();
        }
    }
}
