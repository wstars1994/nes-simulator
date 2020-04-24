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
     * 栈指针
     */
    private static int REG_SP;

    public static int getReg_A(){
        return REG_A;
    }

    /**
     * data -> REG_A
     * @param data
     */
    public static int LDA(byte data) {
        CpuReg.REG_A = data;
        CpuRegStatus.setN(CpuReg.REG_A);
        CpuRegStatus.setZ(CpuReg.REG_A);
        return 2;
    }

    /**
     * 存储2字节16位 data -> addr
     * @param cpuMemory
     * @param low 低8位
     * @param high 高8位
     */
    public static int STA_ABS(CpuMemory cpuMemory, byte low, byte high) {
        //16位 short
        cpuMemory.write(MemUtil.getShort(low,high),CpuReg.REG_A);
        switch (MemUtil.getShort(low,high)) {
            case 0x2000:
                CpuPpuReg.pcr_2000 = MemUtil.toBits(CpuReg.REG_A);
                break;
        }
        return 4;
    }

    /**
     * data->REG_X
     * @param data
     */
    public static int LDX(byte data) {
        CpuReg.REG_X = data;
        CpuRegStatus.setN(CpuReg.REG_X);
        CpuRegStatus.setZ(CpuReg.REG_X);
        return 2;
    }

    /**
     * 将X索引寄存器的数据存入栈指针SP寄存器
     */
    public static int TXS() {
        CpuReg.REG_SP = CpuReg.REG_X;
        return 2;
    }

    /**
     * 存储2字节16位 data -> addr
     * @param low 低8位
     * @param high 高8位
     */
    public static int LDA_ABS(byte low, byte high) {
        short addr = MemUtil.getShort(low, high);
        byte readData = 1;
        //PPU寄存器
        if(addr>=0x2000 && addr<0x2008) {
            switch (addr) {
                //读PPUSTATUS状态寄存器
                case 0x2002:
                    //当CPU读取$2002后vblank标志设置为0
                    readData = MemUtil.bytesToByte(CpuPpuReg.psr_2002);
                    CpuPpuReg.psr_2002[7] = 0;
                    break;
            }
        }
        CpuReg.LDA(readData);
        return 4;
    }
    /**
     * data -> REG_Y
     * @param data
     */
    public static int LDY(byte data) {
        CpuReg.REG_Y = data;
        return 2;
    }

    /**
     * LDA绝对X变址
     * @param d1
     * @param d2
     */
    public static int LDA_ABS_X(byte d1, byte d2) {
        return 4;
    }

    /**
     * 禁止中断
     */
    public static int SEI() {
        CpuRegStatus.setI((byte) 1);
        return 2;
    }

    /**
     * Clear decimal mode
     */
    public static int CLD() {
        CpuRegStatus.setD((byte) 0);
        return 2;
    }

    /**
     *
     * @param cpuMemory
     * @param data
     */
    public static int BPL(CpuMemory cpuMemory, byte data) {
        if(CpuRegStatus.getN() == 0) {
            cpuMemory.setPrgPc(cpuMemory.getPrgPc() + data);
            return 3;
        }
        return 3;
    }
}
