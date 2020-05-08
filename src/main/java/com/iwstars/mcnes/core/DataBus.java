package com.iwstars.mcnes.core;

import com.iwstars.mcnes.core.cpu.CpuMemory;
import com.iwstars.mcnes.core.ppu.PpuMemory;

/**
 * @description: cpu与ppu通信
 * @author: WStars
 * @date: 2020-04-23 10:36
 */
public class DataBus {

    public static PpuMemory ppuMemory;

    public static CpuMemory cpuMemory;

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

    /**
     *  PPU Address Register
     */
    public static int p_2006_data;
    public static boolean p_2006_flag = false;

    /**
     *  PPU Data Port
     */
    public static byte p_2007_read = 0;

    /**
     * 向ppu写数据
     * @param addr
     * @param data
     */
    public static void writePpuMemory(int addr, byte data) {
        ppuMemory.write(addr,data);
    }

    /**
     * 写ppu精灵数据
     * @param addr
     * @param data
     */
    public static void writePpuSprRam(byte addr, byte data) {
        ppuMemory.writeSprRam(addr,data);
    }
}
