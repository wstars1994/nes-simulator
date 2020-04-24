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


    /**
     * 获取寄存器A值
     * @return
     */
    public static int getReg_A(){
        return REG_A;
    }

    /**
     * 将数据放入寄存器A
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
        System.out.print(" -->Write");
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
     * @param low
     * @param high
     */
    public static int LDA_ABS_X(CpuMemory cpuMemory,byte low, byte high) {
        short aShort = MemUtil.getShort(low, high);
        int addr = aShort + (CpuReg.REG_X & 0xff);
        CpuReg.LDA(cpuMemory.read(addr));
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
     * REG_S_N == 0 切换
     * @param cpuMemory
     * @param data
     */
    public static int BPL(CpuMemory cpuMemory, byte data) {
        if(CpuRegStatus.getN() == 0) {
            cpuMemory.setPrgPc(cpuMemory.getPrgPc() + data);
        }
        return 3;
    }

    /**
     * CMP Compare memory and accumulator
     * @param data
     * @return
     */
    public static int CMP(byte data) {
        byte regA = CpuReg.REG_A;
        byte cmpData = (byte) (regA - data);
        CpuRegStatus.setN(cmpData);
        CpuRegStatus.setZ(cmpData);
        CpuRegStatus.setC(cmpData);
        return 2;
    }

    /**
     *
     * @param data
     * @return
     */
    public static int BNE(CpuMemory cpuMemory,byte data) {
        if(CpuRegStatus.getZ() == 0) {
            cpuMemory.setPrgPc(cpuMemory.getPrgPc() + data);
        }
        return 3;
    }

    public static int BCS(CpuMemory cpuMemory,byte data) {
        if(CpuRegStatus.getC() != 0) {
            cpuMemory.setPrgPc(cpuMemory.getPrgPc() + data);
        }
        return 3;
    }

    public static int DEX() {
        CpuReg.REG_X = (byte) (CpuReg.REG_X - 1);
        CpuRegStatus.setN(CpuReg.REG_X);
        CpuRegStatus.setZ(CpuReg.REG_X);
        return 2;
    }

    public static int JSR(CpuMemory cpuMemory,Byte low, Byte high) {
//        short aShort = (INT) (MemUtil.getShort(low, high)&0xFFFFFFFF);
//        cpuMemory.setPrgPc(aShort);
//        push((cpuMemory.getPrgPc() >> 8) & 0xff);
//        push((cpuMemory.getPrgPc() >> 8) & 0xff);
        return 6;
    }

//    public static int CMP_ABS(byte low, byte high) {
//        short aShort = MemUtil.getShort(low, high);
//        return 4;
//    }

    public static void main(String[] args) {
        System.out.println(((short)-28468)&0xFFFFFFFF);
    }
}
