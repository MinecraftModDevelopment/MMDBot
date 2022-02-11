package com.mcmoddev.mmdbot.dashboard.common.listener;

import com.mcmoddev.mmdbot.dashboard.common.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PacketWaiter implements PacketListener {

    private static final Logger LOG = LoggerFactory.getLogger(PacketWaiter.class);
    private final HashMap<Class<?>, Set<WaitingPacket>> waitingPackets;
    private final ScheduledExecutorService threadpool;

    public PacketWaiter() {
        this(Executors.newSingleThreadScheduledExecutor());
    }

    public PacketWaiter(ScheduledExecutorService threadpool) {
        this.waitingPackets = new HashMap<>();
        this.threadpool = threadpool;
    }

    public <P extends Packet> void waitForPacket(Class<P> classType, Predicate<P> condition, Consumer<P> action) {
        waitForPacket(classType, condition, action, -1, null, null);
    }

    public <P extends Packet> void waitForPacket(Class<P> classType, Predicate<P> condition, Consumer<P> action,
                                                 long timeout, TimeUnit unit, Runnable timeoutAction) {
        if (!isShutdown()) {
            throw new IllegalArgumentException("Attempted to register a WaitingEvent while the EventWaiter's threadpool was already shut down!");
        }

        WaitingPacket<P> we = new WaitingPacket<>(condition, action);
        Set<WaitingPacket> set = waitingPackets.computeIfAbsent(classType, c -> ConcurrentHashMap.newKeySet());
        set.add(we);

        if (timeout > 0 && unit != null) {
            threadpool.schedule(() -> {
                try {
                    if (set.remove(we) && timeoutAction != null) timeoutAction.run();
                } catch (Exception ex) {
                    LOG.error("Failed to run timeoutAction", ex);
                }
            }, timeout, unit);
        }
    }

    @Override
    public void onPacket(final Packet packet) {
        Class<?> c = packet.getClass();

        while (c != null) {
            final Set<WaitingPacket> set = waitingPackets.get(c);
            if (set != null) {
                set.removeIf(wEvent -> wEvent.attempt(packet));
                c = c.getSuperclass();
            }
        }
    }

    public void shutdown() {
        threadpool.shutdown();
    }

    public boolean isShutdown() {
        return threadpool.isShutdown();
    }

    private static final class WaitingPacket<P extends Packet> {
        final Predicate<P> condition;
        final Consumer<P> action;

        WaitingPacket(Predicate<P> condition, Consumer<P> action) {
            this.condition = condition;
            this.action = action;
        }

        boolean attempt(P event) {
            if (condition.test(event)) {
                action.accept(event);
                return true;
            }
            return false;
        }
    }

}
