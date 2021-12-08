package com.wxclog.net;

import com.wxclog.ui.ServerEventListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 数据处理
 * @author WStars
 * @date 2021/10/25 15:00
 */
public class ServerHandler extends SimpleChannelInboundHandler<String> {

    private ServerEventListener serverEventListener;

    public ServerHandler(ServerEventListener serverEventListener) {
        this.serverEventListener = serverEventListener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        serverEventListener.event(0,msg);

//        int keyIndex = Integer.parseInt(msg);
//        boolean press = keyIndex < 10 && keyIndex>=0;
//        if(Const.gamepadMain) {
//            if(press){
//                DataBus.c_4017_datas[keyIndex] = 1;
//            }else{
//                DataBus.c_4017_datas[keyIndex/10] = 0;
//            }
//        }else{
//            if(press){
//                DataBus.c_4016_datas[keyIndex] = 1;
//            }else{
//                DataBus.c_4016_datas[keyIndex/10] = 0;
//            }
//        }
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
        NesNetMain.channel = ctx.channel();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        cause.printStackTrace();
    }
}
