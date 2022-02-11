package com.mcmoddev.mmdbot.dashboard.common.encode;

import static com.mcmoddev.mmdbot.dashboard.common.PacketRegistry.PacketSet;
import com.mcmoddev.mmdbot.dashboard.common.ByteBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(PacketDecoder.class);
    private static final Marker MARKER = MarkerFactory.getMarker("PACKET_RECEIVED");

    private final PacketSet packetSet;

    public PacketDecoder(final PacketSet packetSet) {
        this.packetSet = packetSet;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int i = in.readableBytes();
        if (i != 0) {
            ByteBuffer buffer = new ByteBuffer(in);
            int j = buffer.readVarInt();
            final var packet = packetSet.createPacket(j, buffer);
            if (packet == null) {
                throw new IOException("Bad packet id " + j);
            } else {
                if (buffer.readableBytes() > 0) {
                    throw new IOException("Packet " + j + " (" + packet.getClass().getSimpleName() + ") was larger than I expected, found " + buffer.readableBytes() + " bytes extra whilst reading packet " + j);
                } else {
                    out.add(packet);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(MARKER, " IN: [{}] {}", j, packet.getClass().getName());
                    }
                }
            }
        }
    }
}
