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
package com.mcmoddev.mmdbot.dashboard.packets;

import com.mcmoddev.mmdbot.dashboard.ServerBridge;
import com.mcmoddev.mmdbot.dashboard.common.ByteBuffer;
import com.mcmoddev.mmdbot.dashboard.common.packet.HasResponse;
import com.mcmoddev.mmdbot.dashboard.common.packet.Packet;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketContext;
import com.mcmoddev.mmdbot.dashboard.util.Credentials;

public final class CheckAuthorizedPacket implements Packet, HasResponse<CheckAuthorizedPacket.Response> {

    private final String username;
    private final String password;

    public CheckAuthorizedPacket(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public CheckAuthorizedPacket(final ByteBuffer buffer) {
        this.username = buffer.readString();
        this.password = buffer.readString();
    }

    @Override
    public void handle(final PacketContext context) {
        ServerBridge.executeOnInstance(bridge -> context
            .reply(new Response(bridge.checkAuthorized(new Credentials(getUsername(), getPassword())))));
    }

    @Override
    public void encode(final ByteBuffer buffer) {
        buffer.writeString(username);
        buffer.writeString(password);
    }

    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public Credentials getCredentials() {
        return new Credentials(username, password);
    }

    public enum ResponseType {
        AUTHORIZED, DENIED;

        public boolean isAuthorized() {
            return this == AUTHORIZED;
        }
    }

    @Override
    public Class<Response> getResponsePacketClass() {
        return Response.class;
    }

    public static final class Response implements Packet {

        private final ResponseType responseType;

        public Response(final ByteBuffer buffer) {
            this.responseType = buffer.readEnum(ResponseType.class);
        }

        public Response(final ResponseType responseType) {
            this.responseType = responseType;
        }

        @Override
        public void handle(final PacketContext context) {
            // Nothing to handle
        }

        @Override
        public void encode(final ByteBuffer buffer) {
            buffer.writeEnum(responseType);
        }

        public ResponseType getResponseType() {
            return responseType;
        }
    }
}
