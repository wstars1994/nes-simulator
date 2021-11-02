package com.wxclog.net;

import com.wxclog.core.Const;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;

/**
 * 对战网络
 * @author WStars
 * @date 2021/10/25 14:17
 */
public class NesNetMain {

    public static Channel channel;

    public static void init(){
        new Thread(()->{
            if(Const.gamepadMain){
                initServer();
            }else{
                initClient();
            }
        }).start();
    }

    private static void initServer(){
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
    }

    private static void initClient(){
        try{
            final EventLoopGroup group = new NioEventLoopGroup();
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            System.out.println("正在连接中...");
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new MessageToByteEncoder() {
                                @Override
                                protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
                                    out.writeBytes(msg.toString().getBytes());
                                }
                            });
                            pipeline.addLast(new ByteToMessageDecoder() {
                                @Override
                                protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
                                    byte datas[] = new byte[in.readableBytes()];
                                    in.readBytes(datas);
                                    out.add(new String(datas));
                                }
                            });
                            pipeline.addLast(new ServerHandler());
                        }
                    });
            //发起异步连接请求，绑定连接端口和host信息
            final ChannelFuture future = b.connect("192.168.1.142", 7777).sync();

            future.addListener((ChannelFutureListener) arg0 -> {
                if (future.isSuccess()) {
                    System.out.println("连接服务器成功");
                    channel = future.channel();
                }else{
                    System.out.println("连接服务器失败");
                    future.cause().printStackTrace();
                    group.shutdownGracefully(); //关闭线程组
                }
            });
        }catch (Exception e){

        }
    }
}
