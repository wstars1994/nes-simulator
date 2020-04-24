package com.iwstars.mcnes.core.cpu;

/**
 * @description: 状态为寄存器
 * @author: WStars
 * @date: 2020-04-22 13:30
 */
public class CpuRegStatus {

    /**
     * 状态寄存器 Sign Flag 如果操作结果为负，则设置此项(1)；如果为正，则清除此项(0)
     */
    private static byte REG_S_N;
    /**
     * 状态寄存器 Sign Flag
     */
    private static byte REG_S_V;
    /**
     * 状态寄存器 Sign Flag
     */
    private static byte REG_S_B;
    /**
     * 状态寄存器 Sign Flag
     */
    private static byte REG_S_D;
    /**
     * 状态寄存器 Sign Flag
     */
    private static byte REG_S_I;
    /**
     * 状态寄存器 Sign Flag
     */
    private static byte REG_S_Z;
    /**
     * 状态寄存器 Sign Flag
     */
    private static byte REG_S_C;


    public static void setZ(byte data) {
        CpuRegStatus.REG_S_Z = (byte) ((data== 0)?1:0);
    }

    public static void setN(byte data) {
        //负数=1 正数=0
        CpuRegStatus.REG_S_N = (byte) ((data >> 7) & 1);
    }
    public static byte getN() {
        return CpuRegStatus.REG_S_N;
    }

    public static void setI(byte data) {
        CpuRegStatus.REG_S_I = data;
    }

    public static void setD(byte data) {
        CpuRegStatus.REG_S_D = data;
    }

    public static void setC(byte data) {
        CpuRegStatus.REG_S_C = (byte) ((data&0xfff) < 0x100 ? 1: 0);
    }

    public static byte getZ() {
        return CpuRegStatus.REG_S_Z;
    }

    public static byte getC() {
        return CpuRegStatus.REG_S_C;
    }
}
