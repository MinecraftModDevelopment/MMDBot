package com.mcmoddev.mmdbot.core;

import com.mcmoddev.mmdbot.dashboard.common.ServerBridge;
import com.mcmoddev.mmdbot.dashboard.common.packet.impl.CheckAuthorizedPacket;

import java.util.Objects;
import java.util.stream.Stream;

public final class ServerBridgeImpl implements ServerBridge {

    @Override
    public CheckAuthorizedPacket.ResponseType checkAuthorized(final CheckAuthorizedPacket authPacket) {
        if (Stream.of(RunBots.getDashboardConfig().accounts).anyMatch(acc -> Objects.equals(acc.username, authPacket.getUsername())
            && Objects.equals(acc.password, authPacket.getPassword()))) {
            return CheckAuthorizedPacket.ResponseType.AUTHORIZED;
        } else {
            return CheckAuthorizedPacket.ResponseType.DENIED;
        }
    }
}
