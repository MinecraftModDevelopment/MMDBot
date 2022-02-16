package com.mcmoddev.mmdbot.client;

import com.mcmoddev.mmdbot.dashboard.common.packet.HasResponse;
import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import javafx.application.Platform;
import lombok.NonNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class PacketProcessorBuilder<R extends Packet> {

    private final Packet packet;
    private final Class<R> responseClass;
    private long timeout = -1;
    private TimeUnit unit = TimeUnit.MINUTES;
    private Predicate<R> predicate = r -> true;
    private Consumer<R> consumer = r -> {};
    private Runnable timeoutAction = () -> {};

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
}
