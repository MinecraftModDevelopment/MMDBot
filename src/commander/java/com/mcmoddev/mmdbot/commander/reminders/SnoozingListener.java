package com.mcmoddev.mmdbot.commander.reminders;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
        .expireAfterAccess(10, TimeUnit.SECONDS)
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
     * @param offsetGroup the group to decode
     * @param consumer the consumer to accept on each match
     */
    public static void decodeOffset(@NonNull final String offsetGroup, @NonNull final ObjLongConsumer<ChronoUnit> consumer) {
        final var allOffsets = offsetGroup.split("-");
        for (final var offset : allOffsets) {
            final var unit = switch (offset.charAt(offset.length() - 1)) {
                case 'n' -> ChronoUnit.NANOS;
                case 's' -> ChronoUnit.SECONDS;
                case 'h' -> ChronoUnit.HOURS;
                case 'd' -> ChronoUnit.DAYS;
                case 'w' -> ChronoUnit.WEEKS;
                case 'M' -> ChronoUnit.MONTHS;
                case 'y' -> ChronoUnit.YEARS;
                default -> ChronoUnit.MINUTES;
            };
            final var time = Long.parseLong(offset.substring(0, offset.length() - 1));
            consumer.accept(unit, time);
        }
    }
}
