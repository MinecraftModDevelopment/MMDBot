package com.mcmoddev.mmdbot.dashboard.common.encode;

import com.mcmoddev.mmdbot.dashboard.common.PacketReceiver;
import com.mcmoddev.mmdbot.dashboard.common.PacketRegistry;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;

public class DashboardChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final PacketRegistry.PacketSet packetSet;
    private final PacketReceiver receiver;
    private final PacketListener listener;

    public DashboardChannelInitializer(final PacketRegistry.PacketSet packetSet, final PacketReceiver receiver, final PacketListener listener) {
        this.packetSet = packetSet;
        this.receiver = receiver;
        this.listener = listener;
    }

    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        ch.config().setOption(ChannelOption.TCP_NODELAY, true);
        final var pipeline = ch.pipeline();
        pipeline //.addLast("timeout", new ReadTimeoutHandler(30))
            .addLast("splitter", new FrameDecoder())
            .addLast("decoder", new PacketDecoder(packetSet))
            .addLast("prepender", new PacketPrepender())
            .addLast("encoder", new PacketEncoder(packetSet))
            .addLast("packet_handler", new PacketHandler(receiver, listener));
    }
}
