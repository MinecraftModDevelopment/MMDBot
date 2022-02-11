package com.mcmoddev.mmdbot.dashboard.common.packet.impl;

import com.mcmoddev.mmdbot.dashboard.common.ByteBuffer;
import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketReceiver;

import java.util.Random;

public class CheckAuthorizedPacket implements Packet {

    private final String username;
    private final String password;

    private static final Random RAND = new Random();

    public CheckAuthorizedPacket(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public CheckAuthorizedPacket(final ByteBuffer buf) {
        this.username = buf.readUtf();
        this.password = buf.readUtf();
    }

    @Override
    public void encode(final ByteBuffer buffer) {
        buffer.writeUtf(username);
        buffer.writeUtf(password);
    }

    @Override
    public void handle(final PacketReceiver receiver) {
        // Should be implemented by the core
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public static final class Response implements Packet {

        private final ResponseType type;

        public Response(final ResponseType type) {
            this.type = type;
        }

        public Response(final ByteBuffer buf) {
            this.type = buf.readEnum(ResponseType.class);
        }

        @Override
        public void encode(final ByteBuffer buffer) {
            buffer.writeEnum(type);
        }

        @Override
        public void handle(final PacketReceiver receiver) {
            // Nothing to do
        }

        public ResponseType getResponseType() {
            return type;
        }
    }

    public enum ResponseType {
        AUTHORIZED, DENIED;
    }
}
