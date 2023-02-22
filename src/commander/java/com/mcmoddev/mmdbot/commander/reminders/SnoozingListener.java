/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2023 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.commander.reminders;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mcmoddev.mmdbot.commander.TheCommander;
import lombok.NonNull;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ObjLongConsumer;

public enum SnoozingListener implements EventListener {
    INSTANCE;

    public static final String BUTTON_LABEL = "snooze_reminder_";
    public static final String SNOOZE_EMOJI = String.valueOf('\u23F1');
    private final Cache<Long, Reminder> reminders = CacheBuilder.newBuilder()
        .initialCapacity(10)
        .maximumSize(100)
        .expireAfterWrite(1, TimeUnit.DAYS)
        .build();

    @Override
    public void onEvent(@NonNull final GenericEvent genericEvent) {
        if (genericEvent instanceof ButtonInteractionEvent event) {
            final var buttonId = event.getButton().getId();
            if (buttonId == null || !buttonId.startsWith(BUTTON_LABEL)) return;
            final var reminder = reminders.getIfPresent(event.getMessage().getIdLong());
            if (reminder == null) {
                event.deferReply(true).setContent("Unknown reminder!").queue();
                return;
            }
            if (reminder.ownerId() != event.getUser().getIdLong()) {
                event.deferReply(true).setContent("You do not own this reminder.").queue();
                return;
            }
            if (Reminders.userReachedMax(event.getUser().getIdLong())) {
                event.deferReply(true).setContent("You cannot add any other reminders as you have reached the limit of %s pending ones. Remove some or wait until they fire."
                    .formatted(TheCommander.getInstance().getGeneralConfig().features().reminders().getLimitPerUser())).queue();
                return;
            }
            reminders.invalidate(reminder);
            final var timeStr = buttonId.substring(BUTTON_LABEL.length());
            final var offset = withOffset(timeStr, Instant.now());
            Reminders.addReminder(new Reminder(reminder.content(), reminder.channelId(), reminder.isPrivateChannel(), reminder.ownerId(), offset));
            event.deferReply(true).setContent("Successfully snoozed reminder until %s (%s)!"
                    .formatted(TimeFormat.DATE_TIME_LONG.format(offset), TimeFormat.RELATIVE.format(offset)))
                .queue();
        }
    }

    public void addSnoozeListener(final long messageId, final Reminder reminder) {
        if (!reminders.asMap().containsKey(messageId)) {
            reminders.put(messageId, reminder);
        }
    }

    public Button createSnoozeButton(@NonNull final String timeOffset) {
        final List<String> visualData = new ArrayList<>();
        decodeOffset(timeOffset, (unit, time) -> visualData.add(time + " " +
            removeS(unit.toString().toLowerCase(Locale.ROOT)) + (time == 1 ? "" : "s")));
        return Button.secondary(BUTTON_LABEL + timeOffset, SNOOZE_EMOJI + " " + String.join(", ", visualData));
    }

    public static Instant withOffset(@NonNull final String offsetGroup, @NonNull final Instant toModify) {
        final var value = new AtomicReference<>(Instant.now());
        decodeOffset(offsetGroup, (unit, time) -> value.set(value.get().plus(time, unit)));
        return value.get();
    }

    private static String removeS(@NonNull final String str) {
        return str.substring(0, str.lastIndexOf('s'));
    }

    /**
     * Decodes an offset group.
     *
     * @param offsetGroup the group to decode
     * @param consumer    the consumer to accept on each match
     */
    public static void decodeOffset(@NonNull final String offsetGroup, @NonNull final ObjLongConsumer<ChronoUnit> consumer) {
        final var allOffsets = offsetGroup.split("-");
        for (final var offset : allOffsets) {
            final var time = decodeTime(offset);
            consumer.accept(time.unit(), time.amount());
        }
    }

    /**
     * Decodes time from a string.
     *
     * @param time the time to decode
     * @return the decoded time.
     */
    public static Time decodeTime(@NonNull final String time) {
        final var unit = switch (time.charAt(time.length() - 1)) {
            case 'n' -> ChronoUnit.NANOS;
            case 'l' -> ChronoUnit.MILLIS;
            case 's' -> ChronoUnit.SECONDS;
            case 'h' -> ChronoUnit.HOURS;
            case 'd' -> ChronoUnit.DAYS;
            case 'w' -> ChronoUnit.WEEKS;
            case 'M' -> ChronoUnit.MONTHS;
            case 'y' -> ChronoUnit.YEARS;
            default -> ChronoUnit.MINUTES;
        };
        final var tm = Long.parseLong(time.substring(0, time.length() - 1));
        return new Time(tm, unit);
    }

    public record Time(long amount, ChronoUnit unit) {

    }
}
