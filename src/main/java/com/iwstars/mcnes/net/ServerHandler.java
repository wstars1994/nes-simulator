package com.iwstars.mcnes.net;

import com.iwstars.mcnes.core.DataBus;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 数据处理
 * @author WStars
 * @date 2021/10/25 15:00
 */
public class ServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        byte keyIndex = getKeyIndex(Integer.parseInt(msg));
        DataBus.c_4017_datas[keyIndex] = 1;
    }

    private byte getKeyIndex(int keyCode){
        byte key = -1;
        switch (keyCode){
            //W UP
            case 87:
                key = 4;
                break;
            //S DOWN
            case 83:
                key = 5;
                break;
            //A LEFT
            case 65:
                key = 6;
                break;
            //D RIGHT
            case 68:
                key = 7;
                break;
            //1 SELECT
            case 49:
                key = 2;
                break;
            //2 START
            case 50:
                key = 3;
                break;
            //J A
            case 74:
                key = 0;
                break;
            //K B
            case 75:
                key = 1;
                break;
            default:
                key = -1;
        }
        return key;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        System.out.println("new conn");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        cause.printStackTrace();
    }
}