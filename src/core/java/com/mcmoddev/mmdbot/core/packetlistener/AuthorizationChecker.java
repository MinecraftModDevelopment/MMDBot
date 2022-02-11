package com.mcmoddev.mmdbot.core.packetlistener;

import com.mcmoddev.mmdbot.core.RunBots;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketListener;
import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketReceiver;
import com.mcmoddev.mmdbot.dashboard.common.packet.impl.CheckAuthorizedPacket;

import java.util.Objects;
import java.util.stream.Stream;

public record AuthorizationChecker(RunBots.DashboardConfig.Account[] accounts) implements PacketListener {

    @Override
    public void onPacket(final Packet packet, final PacketReceiver receiver) {
        if (!(packet instanceof CheckAuthorizedPacket authPacket)) {
            return;
        }

        if (Stream.of(accounts).anyMatch(acc -> Objects.equals(acc.username, authPacket.getUsername())
            && Objects.equals(acc.password, authPacket.getPassword()))) {
            receiver.reply(new CheckAuthorizedPacket.Response(CheckAuthorizedPacket.ResponseType.AUTHORIZED));
        } else {
            receiver.reply(new CheckAuthorizedPacket.Response(CheckAuthorizedPacket.ResponseType.DENIED));
        }
    }
}
