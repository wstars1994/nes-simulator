package com.iwstars.mcnes.core.cpu;

/**
 * @description: cpu与ppu通信
 * @author: WStars
 * @date: 2020-04-23 10:36
 */
public class CpuPpuReg {

    /**
     * PPU Control Register
     */
    public static byte[] pcr_2000 = new byte[8];

    /**
     *  PPU Mask Register
     */
    public static byte[] pmr_2001 = new byte[8];

    /**
     *  PPU Status Register
     */
    public static byte[] psr_2002 = new byte[8];

    /**
     *  OAM Address Port
     */
    public static byte[] oap_2003 = new byte[8];

    /**
     *  OAM Data Port
     */
    public static byte[] odp_2004 = new byte[8];

    /**
     *  PPU Scrolling Position Register
     */
    public static byte[] spr_2005 = new byte[8];

    /**
     *  PPU Address Register
     */
    public static byte[] par_2006 = new byte[8];

    /**
     *  PPU Data Port
     */
    public static byte[] pdp_2007 = new byte[8];

    /**
     *  OAM DMA register (high byte)
     */
    public static byte[] oamDmar_4014 = new byte[8];

}
