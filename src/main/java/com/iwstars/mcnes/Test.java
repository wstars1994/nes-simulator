package com.iwstars.mcnes;

import com.iwstars.mcnes.core.DataBus;
import com.iwstars.mcnes.util.MemUtil;

/**
 * @description: 测试
 * @author: WStars
 * @date: 2020-04-26 09:59
 */
public class Test {

    public static void cal(byte data){
        //写两次 第一次x 第二次y
        if(!DataBus.p_write_toggle) {
            DataBus.p_vram_addr = (byte) (data>>3);
        } else {
            //将后5位放到第5位前
            DataBus.p_vram_addr |= ((data&31)<<5);
            //将低三位放到15位最前边
            DataBus.p_vram_addr |= (data&7)<<12;
        }
    }

    public static void main(String[] args) {


        cal((byte) 0x7D);
        cal((byte) 0x5e);


        System.out.println(Integer.toBinaryString(DataBus.p_vram_addr));
    }
}
