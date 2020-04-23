package com.iwstars.mcnes.util;

/**
 * @description: 内存操作util
 * @author: WStars
 * @date: 2020-04-19 12:56
 */
public class MemUtil {

    /**
     * 两个byte组成的short
     * @param low
     * @param high
     * @return
     */
    public static short getShort(byte low, byte high){
        return (short) ((low & 0xFF) | ((high & 0xFF) << 8));
    }

    /**
     * from right to left
     * @param data
     * @return
     */
    public static byte[] toBits(byte data){
        byte[] bytes = new byte[8];
        for(int i=0;i<8;i++) {
            bytes[i] = (byte) ((data>>i)&1);
        }
        return bytes;
    }
}
