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
}
