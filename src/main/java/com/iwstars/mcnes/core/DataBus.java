package com.iwstars.mcnes.core;

import com.iwstars.mcnes.core.ppu.PpuMemory;

/**
 * @description: cpu与ppu通信
 * @author: WStars
 * @date: 2020-04-23 10:36
 */
public class DataBus {

    /**
     * PPU Control Register
     */
    public static byte[] p_2000 = new byte[8];

    /**
     *  PPU Mask Register
     */
    public static byte[] p_2001 = new byte[8];

    /**
     *  PPU Status Register
     */
    public static byte[] p_2002 = new byte[8];

    /**
     *  OAM Address Port
     */
    public static byte[] p_2003 = new byte[8];

    /**
     *  OAM Data Port
     */
    public static byte[] p_2004 = new byte[8];

    /**
     *  PPU Scrolling Position Register
     */
    public static byte[] p_2005 = new byte[8];

    public static boolean p_2006_flag = false;
    /**
     *  PPU Address Register
     */
    public static short p_2006_data;
    /**
     *  PPU Data Port
     */
    public static byte[] p_2007 = new byte[8];

    /**
     *  OAM DMA register (high byte)
     */
    public static byte[] p_4014 = new byte[8];


    public static void writePpuNameTable(short addr, byte data) {
        PpuMemory.write(addr,data);
    }
}
