package com.wxclog.net;

import com.wxclog.ui.ServerEventListener;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.*;

import java.util.List;

/**
 * 对战网络
 * @author WStars
 * @date 2021/10/25 14:17
 */
public class NesNetMain {

    public static Channel channel;

    public static void connectServer(ServerEventListener serverEventListener){
        try{
            final EventLoopGroup group = new NioEventLoopGroup();
            Bootstrap b = new Bootstrap();
            b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        System.out.println("正在连接中...");
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Short.MAX_VALUE,0,4));
                        pipeline.addLast(new MessageDecoder());
                        pipeline.addLast(new MessageEncoder());
                        pipeline.addLast(new ServerHandler(serverEventListener));
                    }
                });
            //发起异步连接请求，绑定连接端口和host信息
            final ChannelFuture future = b.connect("127.0.0.1", 7777).sync();

            future.addListener((ChannelFutureListener) arg0 -> {
                if (future.isSuccess()) {
                    System.out.println("连接服务器成功");
                    channel = future.channel();
                    serverEventListener.event(-1,"{\"type\":-1}");
                    channel.writeAndFlush("{\"type\":0}");
                }else{
                    System.out.println("连接服务器失败");
                    future.cause().printStackTrace();
                    group.shutdownGracefully(); //关闭线程组
                }
            });
        }catch (Exception e){
 
        }
    }

    public static void send(int type,String data){
        System.out.println(type);
        switch (type){
            case 0:
            case 1:
            case 3:
                channel.writeAndFlush("{\"type\":"+type+"}");
                break;
            case 2:
                channel.writeAndFlush("{\"type\":"+type+",\"roomId\": \""+data+"\"}");
                break;
        }

    }

    public static void close() {
        if(channel!=null && channel.isOpen()){
            channel.close();
        }
    }
}
