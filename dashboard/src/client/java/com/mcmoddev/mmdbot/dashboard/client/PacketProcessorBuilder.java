/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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
package com.mcmoddev.mmdbot.dashboard.client;

import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.util.RunnableQueue;
import javafx.application.Platform;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class PacketProcessorBuilder<R extends Packet> {

    private final Packet packet;
    private final Class<R> responseClass;
    private long timeout = -1;
    private TimeUnit unit = TimeUnit.MINUTES;
    private Predicate<R> predicate = r -> true;
    private Consumer<R> consumer = r -> {
    };
    private Runnable timeoutAction = () -> {
    };

    PacketProcessorBuilder(final Packet packet, final Class<R> responseClass) {
        this.packet = packet;
        this.responseClass = responseClass;
    }

    public PacketProcessorBuilder<R> withTimeout(long timeout, @NonNull TimeUnit unit) {
        this.timeout = timeout;
        this.unit = unit;
        return this;
    }

    public PacketProcessorBuilder<R> withPredicate(@NonNull Predicate<R> predicate) {
        this.predicate = predicate;
        return this;
    }

    public PacketProcessorBuilder<R> withAction(@NonNull Consumer<R> consumer) {
        this.consumer = consumer;
        return this;
    }

    public PacketProcessorBuilder<R> withPlatformAction(@NonNull Consumer<R> consumer) {
        return withAction(p -> Platform.runLater(() -> consumer.accept(p)));
    }

    public PacketProcessorBuilder<R> withTimeoutAction(@NonNull Runnable timeoutAction) {
        this.timeoutAction = timeoutAction;
        return this;
    }

    public PacketProcessorBuilder<R> withPlatformTimeoutAction(@NonNull Runnable timeoutAction) {
        return withTimeoutAction(() -> Platform.runLater(timeoutAction));
    }

    public void queue() {
        DashboardClient.sendPacket(packet);
        DashboardClient.PACKET_WAITER.waitForPacket(responseClass, predicate, consumer, timeout, unit, timeoutAction);
    }

    public void queueAndBlock(boolean unBlockOnTimeout) throws ExecutionException, InterruptedException {
        DashboardClient.sendPacket(packet);
        final var tmAction = new AtomicReference<Runnable>(() -> {});
        final var future = DashboardClient.PACKET_WAITER.awaitPacket(responseClass, predicate, timeout, unit, () -> {
            tmAction.get().run();
            timeoutAction.run();
        });
        tmAction.set(() -> future.cancel(true));
        while (!future.isDone()) {
            // Block the caller thread, until the packet is received.
        }
        try {
            consumer.accept(future.get());
        } catch (CancellationException e) {
            if (!unBlockOnTimeout) {
                // Throw the exception back if cancellation was not supposed to happen.
                throw e;
            }
        }
    }

    /**
     * Similar to {@link #queueAndBlock(boolean)}, but throws any Exception back as a runtime one.
     *
     * @see #queueAndBlock(boolean)
     */
    public void queueAndBlockNoException(boolean unBlockOnTimeout) {
        try {
            queueAndBlock(unBlockOnTimeout);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
