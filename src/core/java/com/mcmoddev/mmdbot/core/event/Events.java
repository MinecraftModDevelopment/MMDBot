package com.mcmoddev.mmdbot.core.event;

import com.mcmoddev.mmdbot.core.event.moderation.ModerationEvent;
import io.github.matyrobbrt.eventdispatcher.EventBus;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Events {
    public static final EventBus MODERATION_BUS = EventBus.builder("Moderation")
        .baseEventType(ModerationEvent.class)
        .build();
}
