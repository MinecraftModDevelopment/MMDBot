package com.mcmoddev.mmdbot.client;

import com.mcmoddev.mmdbot.dashboard.common.Packet;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketListener;
import com.mcmoddev.mmdbot.dashboard.common.PacketReceiver;
import com.mcmoddev.mmdbot.dashboard.common.PacketRegistry;
import com.mcmoddev.mmdbot.dashboard.common.encode.DashboardChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.Scanner;

public class DashboardClient {

    private static Channel channel;

    public static void setup(InetSocketAddress address, PacketListener listener) {
        final var group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new DashboardChannelInitializer(PacketRegistry.SET, new PacketReceiver() {
                    @Override
                    public void reply(final Packet packet) {

                    }
                }, listener));
            channel = bootstrap.connect(address.getAddress(), address.getPort()).sync().channel();
        } catch (Exception ignored) {

        }
    }

    public static void main(String[] args) {
        setup(new InetSocketAddress("localhost", 8912), packet -> {

        });
        channel.writeAndFlush(new PacketRegistry.TestPacket(120912));
        final var in = new Scanner(System.in);
        while (true) {
            channel.writeAndFlush(new PacketRegistry.TestPacket(in.nextInt()));
        }
    }

}
