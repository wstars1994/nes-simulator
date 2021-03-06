package com.wxclog.core;

import com.wxclog.core.cpu.CpuMemory;
import com.wxclog.core.ppu.PpuMemory;

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

    public static byte p_scroll_x = 0;

    /**
     *  PPU Address Register
     */
    public static short p_vram_addr;
    public static boolean p_write_toggle = false;
    public static short p_vram_temp_addr;

    /**
     *  PPU Data Port
     */
    public static byte p_2007_read = 0;

    /**
     *  Joypad 1
     */
    public static byte c_4016 = -1;
    public static byte c_4016_datas[] = new byte[8];

    /**
     *  Joypad 2
     */
    public static byte c_4017 = -1;
    public static byte c_4017_datas[] = new byte[8];

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

    /**
     * 渲染背景
     */
    public static boolean showBg() {return p_2001[3] == 1;}
    /**
     * 渲染精灵
     */
    public static boolean showSpr() {return p_2001[4] == 1;}

}
