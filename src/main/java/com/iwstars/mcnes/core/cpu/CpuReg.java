package com.iwstars.mcnes.core.cpu;

/**
 * @description: CPU寄存器
 * @author: WStars
 * @date: 2020-04-21 10:48
 */
public class CpuReg {
    /**
     * 累加器
     */
    private static byte REG_A;
    /**
     * X索引寄存器
     */
    private static byte REG_X;
    /**
     * Y索引寄存器
     */
    private static byte REG_Y;
    /**
     * 状态寄存器
     */
    private static byte REG_S;
    /**
     * 指令计数器 16位
     */
    private static short REG_PC;
    /**
     * 栈指针
     */
    private static int REG_SP;

    /**
     * data -> REG_A
     * @param data
     */
    public static void LDA(byte data) {
        CpuReg.REG_A = data;
    }

    /**
     * 存储2字节16位 data -> addr
     * @param cpuMemory
     * @param low 低8位
     * @param high 高8位
     */
    public static void STA_2Byte(CpuMemory cpuMemory, byte low, byte high) {
        //16位 short
        cpuMemory.write(CpuReg.getShort(low,high),CpuReg.REG_A);
    }

    /**
     * data->REG_X
     * @param data
     */
    public static void LDX(byte data) {
        CpuReg.REG_X = data;
    }

    /**
     * 将X索引寄存器的数据存入栈指针SP寄存器
     */
    public static void TXS() {
        CpuReg.REG_SP = CpuReg.REG_X;
    }

    /**
     * 存储2字节16位 data -> addr
     * @param low 低8位
     * @param high 高8位
     */
    public static void LDA_2Byte(byte low, byte high) {
        System.out.println(getShort(low,high));
//        CpuReg.LDA(getShort(low,high));
    }

    /**
     * 两个byte组成的short
     * @param low
     * @param high
     * @return
     */
    private static short getShort(byte low, byte high){
        return (short) ((low & 0xFF) | ((high & 0xFF) << 8));
    }
}
