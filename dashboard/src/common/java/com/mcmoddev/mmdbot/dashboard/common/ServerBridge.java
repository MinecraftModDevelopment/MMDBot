package com.mcmoddev.mmdbot.dashboard.common;

import com.mcmoddev.mmdbot.dashboard.common.packet.impl.CheckAuthorizedPacket;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public interface ServerBridge {
    AtomicReference<ServerBridge> INSTANCE = new AtomicReference<>();

    static void executeOnInstance(Consumer<ServerBridge> consumer) {
        final var inst = INSTANCE.get();
        if (inst != null) {
            consumer.accept(inst);
        }
    }

    static void setInstance(ServerBridge instance) {
        if (INSTANCE.get() == null) {
            INSTANCE.set(instance);
        }
    }

    CheckAuthorizedPacket.ResponseType checkAuthorized(CheckAuthorizedPacket packet);
}
