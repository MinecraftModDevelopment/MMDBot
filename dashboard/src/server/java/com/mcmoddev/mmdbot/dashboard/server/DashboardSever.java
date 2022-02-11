package com.mcmoddev.mmdbot.dashboard.server;

import com.mcmoddev.mmdbot.dashboard.common.Packet;
import com.mcmoddev.mmdbot.dashboard.common.listener.PacketListener;
import com.mcmoddev.mmdbot.dashboard.common.PacketReceiver;
import com.mcmoddev.mmdbot.dashboard.common.PacketRegistry;
import com.mcmoddev.mmdbot.dashboard.common.encode.DashboardChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

public class DashboardSever {

    private static ChannelFuture channel;

    public static void setup(InetSocketAddress address, PacketListener listener) {
        final var boosGroup = new NioEventLoopGroup(1);
        try {
            channel = new ServerBootstrap()
                .group(boosGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new DashboardChannelInitializer(PacketRegistry.SET, new PacketReceiver() {
                    @Override
                    public void reply(final Packet packet) {

                    }
                }, listener)).localAddress(address.getAddress(), address.getPort()).bind().syncUninterruptibly();
        } catch (Exception ignored) {

        }
        Runtime.getRuntime().addShutdownHook(new Thread(boosGroup::shutdownGracefully, "DashboardServerCloser"));
    }

    public static void main(String[] args) {
        setup(new InetSocketAddress("localhost", 8912), packet -> {

        });
        System.out.println("Stuff finished");
    }

}
