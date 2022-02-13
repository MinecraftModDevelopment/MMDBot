package com.mcmoddev.mmdbot.dashboard.server;

import com.mcmoddev.mmdbot.dashboard.common.listener.PacketListener;
import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketReceiver;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerPacketHandler extends SimpleChannelInboundHandler<Packet> {

    private final PacketReceiver receiver;
    private final PacketListener listener;
    private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public ServerPacketHandler(final PacketReceiver receiver, final PacketListener listener) {
        this.receiver = receiver;
        this.listener = listener;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channels.add(ctx.channel());
        log.debug("Channel {} has been connected to the server!", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        channels.remove(ctx.channel());
        log.debug("Channel {} has disconnected from the server!", ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Packet msg) throws Exception {
        listener.onPacketAndThen(msg, receiver, () -> msg.handle(receiver));
    }

    public void sendPacket(Packet packet) {
        channels.forEach(c -> c.writeAndFlush(packet));
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        log.error("Exception caught while handling a packet!", cause);
        ctx.close();
    }
}
