package com.wxclog.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int messageLength = in.readInt();
        byte messageData[] = new byte[messageLength];
        in.readBytes(messageData);
        out.add(new String(messageData));
    }
}
