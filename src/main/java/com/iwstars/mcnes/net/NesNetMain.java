package com.iwstars.mcnes.net;

import com.iwstars.mcnes.Main;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;

/**
 * 对战网络
 * @author WStars
 * @date 2021/10/25 14:17
 */
public class NesNetMain {

    public static void init(){
        new Thread(()->{
            if(Main.controlMain){
                NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
                NioEventLoopGroup workGroup = new NioEventLoopGroup(4);
                try {
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap
                            .group(bossGroup, workGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel socketChannel) throws Exception {
                                    ChannelPipeline p = socketChannel.pipeline();
                                    p.addLast(new MessageToByteEncoder() {
                                        @Override
                                        protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
                                            out.writeBytes(msg.toString().getBytes());
                                        }
                                    });
                                    p.addLast(new ByteToMessageDecoder() {
                                        @Override
                                        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
                                            byte datas[] = new byte[in.readableBytes()];
                                            in.readBytes(datas);
                                            out.add(new String(datas));
                                        }
                                    });
                                    p.addLast(new ServerHandler());
                                }
                            });
                    Channel ch = bootstrap.bind(7777).sync().channel();
                    System.out.println("Netty服务启动成功,port:"+7777);
                    ch.closeFuture().sync();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    bossGroup.shutdownGracefully();
                    workGroup.shutdownGracefully();
                }
            }else{
            }
        }).start();
    }

}
