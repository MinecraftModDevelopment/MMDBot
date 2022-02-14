/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
package com.mcmoddev.mmdbot.dashboard.common.packet.impl;

import com.mcmoddev.mmdbot.dashboard.common.ByteBuffer;
import com.mcmoddev.mmdbot.dashboard.common.ServerBridge;
import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketReceiver;

public class CheckAuthorizedPacket implements Packet {

    private final String username;
    private final String password;

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
        System.out.println("Handle");
		ServerBridge.executeOnInstance(bridge -> receiver.send(new Response(bridge.checkAuthorized(this))));
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
            System.out.println("Received packet");
        }

        public ResponseType getResponseType() {
            return type;
        }
    }

    public enum ResponseType {
        AUTHORIZED, DENIED;

        public boolean isAuthorized() {
            return this == AUTHORIZED;
        }
    }
}
