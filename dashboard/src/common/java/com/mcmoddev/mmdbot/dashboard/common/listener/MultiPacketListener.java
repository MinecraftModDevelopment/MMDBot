package com.mcmoddev.mmdbot.dashboard.common.listener;

import com.mcmoddev.mmdbot.dashboard.common.Packet;

import java.util.List;

public class MultiPacketListener implements PacketListener {

    private final List<PacketListener> listeners;

    public MultiPacketListener(final List<PacketListener> listeners) {
        this.listeners = listeners;
    }

    public MultiPacketListener(final PacketListener... listeners) {
        this.listeners = List.of(listeners);
    }

    @Override
    public void onPacket(final Packet packet) {
        listeners.forEach(l -> l.onPacket(packet));
    }
}
