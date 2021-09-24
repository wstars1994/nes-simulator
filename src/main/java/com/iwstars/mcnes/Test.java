package com.iwstars.mcnes;

import com.iwstars.mcnes.core.DataBus;

/**
 * @description: 测试
 * @author: WStars
 * @date: 2020-04-26 09:59
 */
public class Test {

    public static void cal(byte data){
        //写两次 第一次x 第二次y
        if(!DataBus.p_write_toggle) {
            //将data的高5位为addr低5位赋值
            DataBus.p_vram_addr &= ~0x1F;
            DataBus.p_vram_addr |= (data>>3)&0x1F;
        } else {
            //将data前5位放到addr低5位前
            DataBus.p_vram_addr |= ((data>>3)<<5);
            //将低三位放到15位最前边
            DataBus.p_vram_addr |= (data&0x7)<<12;
        }
        DataBus.p_write_toggle = !DataBus.p_write_toggle;
    }

    public static void main(String[] args) {

//
//        cal((byte) 0x7D);
//        cal((byte) 0x5e);
//
//
//        System.out.println(Integer.toBinaryString(DataBus.p_vram_addr));
        System.out.println(3<<10);
    }
}
