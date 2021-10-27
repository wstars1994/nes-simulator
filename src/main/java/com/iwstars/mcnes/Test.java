package com.iwstars.mcnes;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;

/**
 * @description: 测试
 * @author: WStars
 * @date: 2020-04-26 09:59
 */
public class Test {

    public static void main(String[] args) {
        int addr = 65534;
        addr-=0xC000;
        addr = 0x8000+7*16*1024+addr;
        System.out.println(addr);
    }
}
