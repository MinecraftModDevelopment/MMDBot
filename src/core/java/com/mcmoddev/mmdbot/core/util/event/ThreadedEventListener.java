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
package com.mcmoddev.mmdbot.core.util.event;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.NonNull;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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
    private final List<EventListener> listeners = Collections.synchronizedList(new CopyOnWriteArrayList<>());

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
    @CanIgnoreReturnValue
    public ThreadedEventListener addListener(@NonNull final EventListener listener) {
        this.listeners.add(listener);
        return this;
    }

    /**
     * Adds multiple listeners whose actions will be run in the {@link #threadPool}
     *
     * @param listeners the listeners to add
     * @return the current {@link ThreadedEventListener} for chaining conveniences.
     */
    @CanIgnoreReturnValue
    public ThreadedEventListener addListeners(@NonNull final EventListener... listeners) {
        this.listeners.addAll(Arrays.asList(listeners));
        return this;
    }

    /**
     * Adds multiple listeners whose actions will be run in the {@link #threadPool}
     *
     * @param listeners the listeners to add
     * @return the current {@link ThreadedEventListener} for chaining conveniences.
     */
    @CanIgnoreReturnValue
    public ThreadedEventListener addListeners(@NonNull List<? extends EventListener> listeners) {
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
                if (listener != null) {
                    listener.onEvent(event);
                }
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
