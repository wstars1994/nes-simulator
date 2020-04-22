package com.iwstars.mcnes.core.cpu;

import com.iwstars.mcnes.util.MemUtil;

/**
 * @description: CPU寄存器
 * @author: WStars
 * @date: 2020-04-19 10:48
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
        CpuRegStatus.setN(CpuReg.REG_A);
        CpuRegStatus.setZ(CpuReg.REG_A);
    }

    /**
     * 存储2字节16位 data -> addr
     * @param cpuMemory
     * @param low 低8位
     * @param high 高8位
     */
    public static void STA_ABS(CpuMemory cpuMemory, byte low, byte high) {
        //16位 short
        cpuMemory.write(MemUtil.getShort(low,high),CpuReg.REG_A);
    }

    /**
     * data->REG_X
     * @param data
     */
    public static void LDX(byte data) {
        CpuReg.REG_X = data;
        CpuRegStatus.setN(CpuReg.REG_X);
        CpuRegStatus.setZ(CpuReg.REG_X);
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
    public static void LDA_ABS(byte low, byte high) {
        short addr = MemUtil.getShort(low, high);
        byte readData = 0;
        //PPU寄存器
        if(addr>=0x2000 && addr<0x2008) {
            switch (addr) {
                //读PPUSTATUS状态寄存器
                case 0x2002:
                    break;
                default:
                    readData = 0;
            }
        }
        CpuReg.LDA(readData);
    }
    /**
     * data -> REG_Y
     * @param data
     */
    public static void LDY(byte data) {
        CpuReg.REG_Y = data;
    }

    /**
     * LDA绝对X变址
     * @param d1
     * @param d2
     */
    public static void LDA_ABS_X(byte d1, byte d2) {

        System.out.println(d1&0xFF);
    }

    /**
     * 禁止中断
     */
    public static void SEI() {
        CpuRegStatus.setI((byte) 1);
    }

    /**
     * Clear decimal mode
     */
    public static void CLD() {
        CpuRegStatus.setD((byte) 0);
    }

    /**
     *
     * @param data
     */
    public static void BPL(byte data) {
        if(CpuRegStatus.getN()== 0 ) {

        }
    }
}
