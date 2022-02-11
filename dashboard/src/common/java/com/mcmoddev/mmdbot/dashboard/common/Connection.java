package com.mcmoddev.mmdbot.dashboard.common;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

public class Connection {

    private final Bootstrap bootstrap;
    private final Channel channel;

    public Connection(final Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
        channel = bootstrap.bind().syncUninterruptibly().channel();
    }

    public void sendPacket(Packet packet) {
        channel.writeAndFlush(packet);
    }
}
