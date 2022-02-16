package com.mcmoddev.mmdbot.dashboard.common.packet;

public interface HasResponse<R extends Packet> {

    Class<R> getResponsePacketClass();

}
