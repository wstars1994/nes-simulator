package com.wxclog.ui;

import com.wxclog.core.Const;
import com.wxclog.core.DataBus;
import com.wxclog.net.NesNetMain;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;


/**
 * 控制器
 * @author WStars
 * @date 2021/12/7 15:26
 */
public class NesKeyAdapter{

    public static class KeyPress implements EventHandler<KeyEvent>{
        @Override
        public void handle(KeyEvent event) {
            byte keyIndex = getKeyIndex(event.getText());
            if(keyIndex !=-1){
                if(!Const.gamepadMain){
                    DataBus.c_4017_datas[keyIndex] = 1;
                }else{
                    DataBus.c_4016_datas[keyIndex] = 1;
                }
                if(NesNetMain.channel!=null){
                    NesNetMain.channel.writeAndFlush(keyIndex);
                }
            }
        }
    }

    public static class KeyReleased implements EventHandler<KeyEvent>{
        @Override
        public void handle(KeyEvent event) {
            byte keyIndex = getKeyIndex(event.getText());
            if(keyIndex !=-1){
                if(!Const.gamepadMain){
                    DataBus.c_4017_datas[keyIndex] = 0;
                }else{
                    DataBus.c_4016_datas[keyIndex] = 0;
                }
                if(NesNetMain.channel != null){
                    NesNetMain.channel.writeAndFlush(keyIndex*10);
                }
            }
        }
    }

    private static byte getKeyIndex(String keyCode){
        byte key = -1;
        Integer data = Const.gamepadMapping.get(keyCode);
        if(data != null){
            return data.byteValue();
        }
        return key;
    }
}
