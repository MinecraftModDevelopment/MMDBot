package com.mcmoddev.mmdbot.dashboard.common.encode;

import com.mcmoddev.mmdbot.dashboard.common.Packet;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketListener;
import com.mcmoddev.mmdbot.dashboard.common.PacketReceiver;
import com.mcmoddev.mmdbot.dashboard.common.PacketRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public final class PacketHandler extends SimpleChannelInboundHandler<Packet> {

    private final PacketReceiver receiver;
    private final PacketListener listener;

    public PacketHandler(final PacketReceiver receiver, final PacketListener listener) {
        this.receiver = receiver;
        this.listener = listener;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Packet msg) throws Exception {
        listener.onPacket(msg);
        msg.handle(receiver);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(new PacketRegistry.TestPacket(12));
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        System.out.println(ctx.channel().remoteAddress() + " joined the party!");
    }
}
