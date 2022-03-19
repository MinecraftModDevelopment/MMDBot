package com.mcmoddev.mmdbot.core.util.event;

import io.github.matyrobbrt.eventdispatcher.Event;
import io.github.matyrobbrt.eventdispatcher.EventBus;

import java.util.function.Consumer;

/**
 * An event listener which can only be registered once.
 */
public final class OneTimeEventListener<E extends Event> {
    private boolean registered;
    private final int priority;
    private final Consumer<E> eventListener;

    public OneTimeEventListener(final int priority, final Consumer<E> eventListener) {
        this.priority = priority;
        this.eventListener = eventListener;
    }

    public OneTimeEventListener(final Consumer<E> eventListener) {
        this(0, eventListener);
    }

    public void register(EventBus bus) {
        if (registered) return;
        registered = true;
        bus.addListener(priority, eventListener);
    }
}
