package com.mcmoddev.mmdbot.dashboard.common.encode;

import com.mcmoddev.mmdbot.dashboard.common.ByteBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketPrepender extends MessageToByteEncoder<ByteBuf> {

    private static final int MAX_BYTES = 3;

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
        int i = msg.readableBytes();
        int j = ByteBuffer.getVarIntSize(i);
        if (j > 3) {
            throw new IllegalArgumentException("Unable to fit " + i + " into 3");
        } else {
            ByteBuffer byteBuffer = new ByteBuffer(out);
            byteBuffer.ensureWritable(j + i);
            byteBuffer.writeVarInt(i);
            byteBuffer.writeBytes(msg, msg.readerIndex(), i);
        }
    }

}
