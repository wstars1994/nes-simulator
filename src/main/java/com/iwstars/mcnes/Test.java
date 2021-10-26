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
        byte nameTable[][] = {{1,2,3,4},{5,6,7,8}};

        byte[][] namePage = new byte[4][];
        byte[][] namePage2 = new byte[4][];

        namePage[0] = nameTable[0];
        namePage2[0] = nameTable[0];

        namePage2[0][1]=9;

        System.out.println(namePage[0][1]);

    }
}
