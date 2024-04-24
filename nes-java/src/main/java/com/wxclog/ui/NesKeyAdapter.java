package com.wxclog.ui;

import com.wxclog.core.Const;
import com.wxclog.core.DataBus;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * 控制器
 * @author WStars
 * @date 2021/12/7 15:26
 */
public class NesKeyAdapter extends KeyAdapter {

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        byte keyIndex = getKeyIndex(keyCode);
        if(keyIndex !=-1){
            if(!Const.gamepadMain){
                DataBus.c_4017_datas[keyIndex] = 1;
            }else{
                DataBus.c_4016_datas[keyIndex] = 1;
            }
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        byte keyIndex = getKeyIndex(keyCode);
        if(keyIndex !=-1){
            if(!Const.gamepadMain){
                DataBus.c_4017_datas[keyIndex] = 0;
            }else{
                DataBus.c_4016_datas[keyIndex] = 0;
            }
        }
    }

    private byte getKeyIndex(int keyCode){
        byte key = -1;
        Integer data = Const.gamepadMapping.get(keyCode);
        if(data != null){
            return data.byteValue();
        }
        return key;
    }
}
