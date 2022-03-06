package com.mcmoddev.mmdbot.core.util;

import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * An {@link EventListener} implementation which allows running the action of
 * multiple {@link EventListener}s in a thread poll.
 *
 * @author matyrobbrt
 */
@Slf4j
public class ThreadedEventListener implements EventListener, ExecutorService {
    @Delegate
    private final ExecutorService threadPool;
    private final List<EventListener> listeners = new ArrayList<>();

    /**
     * Creates a new {@link ThreadedEventListener}
     *
     * @param threadPool the thread poll used for running the actions of the events
     * @param listeners  the listeners to run
     */
    public ThreadedEventListener(final ExecutorService threadPool, EventListener... listeners) {
        this.threadPool = threadPool;
        this.listeners.addAll(Arrays.asList(listeners));
    }

    /**
     * Adds another listener whose action will be run in the {@link #threadPool}
     *
     * @param listener the listener to add
     * @return the current {@link ThreadedEventListener} for chaining conveniences.
     */
    public ThreadedEventListener addListener(EventListener listener) {
        this.listeners.add(listener);
        return this;
    }

    /**
     * Adds multiple listeners whose actions will be run in the {@link #threadPool}
     *
     * @param listeners the listeners to add
     * @return the current {@link ThreadedEventListener} for chaining conveniences.
     */
    public ThreadedEventListener addListeners(EventListener... listeners) {
        this.listeners.addAll(Arrays.asList(listeners));
        return this;
    }

    /**
     * Adds multiple listeners whose actions will be run in the {@link #threadPool}
     *
     * @param listeners the listeners to add
     * @return the current {@link ThreadedEventListener} for chaining conveniences.
     */
    public ThreadedEventListener addListeners(List<EventListener> listeners) {
        this.listeners.addAll(listeners);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SubscribeEvent
    public void onEvent(@Nonnull GenericEvent event) {
        listeners.forEach(listener -> threadPool.execute(() -> {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                log.error("Error while executing threaded event!", e);
                // Reply to the user in order to inform them
                if (event instanceof IReplyCallback replyCallback) {
                    replyCallback.deferReply(true).addEmbeds(new EmbedBuilder()
                        .setTitle("This interaction failed due to an exception.")
                        .setColor(Color.RED)
                        .setDescription(e.toString())
                        .build()).queue();
                }
            }
        }));
    }

    /**
     * @return the thread pool {@link EventListener}s are run in
     */
    public Executor getThreadPool() {
        return threadPool;
    }

    /**
     * Clears all of the {@link EventListener}s registered.
     */
    public void clear() {
        this.listeners.clear();
    }
}
