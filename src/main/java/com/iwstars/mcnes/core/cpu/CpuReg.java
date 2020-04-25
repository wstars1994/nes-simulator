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
     * 寄存器A存储2字节16位 data -> addr
     * @param cpuMemory
     * @param low 低8位
     * @param high 高8位
     */
    public static int STA_ABS(CpuMemory cpuMemory, byte low, byte high) {
        //16位 short
        cpuMemory.write(MemUtil.concatByte(low,high),CpuReg.REG_A);
        switch (MemUtil.concatByte(low,high)) {
            case 0x2000:
                CpuPpuReg.p_2000 = MemUtil.toBits(CpuReg.REG_A);
                break;
            case 0x2001:
                CpuPpuReg.p_2001 = MemUtil.toBits(CpuReg.REG_A);
                break;
            case 0x4011:
                break;
             //OAM DMA register (high byte)
            case 0x4014:
//                CpuPpuReg.pcr_2000 = MemUtil.toBits(CpuReg.REG_A);
                System.out.println("OAM DMA register (high byte)");
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
        int addr = MemUtil.concatByte(low, high);
        byte readData = 1;
        //PPU寄存器
        if(addr>=0x2000 && addr<0x2008) {
            switch (addr) {
                //读PPUSTATUS状态寄存器
                case 0x2002:
                    //当CPU读取$2002后vblank标志设置为0
                    readData = MemUtil.bitsToByte(CpuPpuReg.p_2002);
                    CpuPpuReg.p_2002[7] = 0;
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
        int aShort = MemUtil.concatByte(low, high);
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
     * Z=0切换
     * @param data
     * @return
     */
    public static int BNE(CpuMemory cpuMemory,byte data) {
        if(CpuRegStatus.getZ() == 0) {
            cpuMemory.setPrgPc(cpuMemory.getPrgPc() + data);
        }
        return 3;
    }

    /**
     * C!=0 切换
     * @param cpuMemory
     * @param data
     * @return
     */
    public static int BCS(CpuMemory cpuMemory,byte data) {
        if(CpuRegStatus.getC() != 0) {
            cpuMemory.setPrgPc(cpuMemory.getPrgPc() + data);
        }
        return 3;
    }

    /**
     * X=X-1
     * @return
     */
    public static int DEX() {
        CpuReg.REG_X = (byte) (CpuReg.REG_X - 1);
        CpuRegStatus.setN(CpuReg.REG_X);
        CpuRegStatus.setZ(CpuReg.REG_X);
        return 2;
    }

    /**
     *
     * @param cpuMemory
     * @param low
     * @param high
     * @return
     */
    public static int JSR(CpuMemory cpuMemory,byte low, byte high) {
        int aShort = MemUtil.concatByte(low, high);
        int pc = cpuMemory.getPrgPc();
        cpuMemory.pushStack((byte) (pc&0xFF));
        cpuMemory.pushStack((byte) ((pc>>8)&0xFF));
        cpuMemory.pushStack((byte) ((pc>>16)&0xFF));
        cpuMemory.pushStack((byte) ((pc>>24)&0xFF));
        cpuMemory.setPrgPc(aShort);
        return 6;
    }

    public static int STA_ZERO(CpuMemory cpuMemory, byte addr) {
        cpuMemory.write(addr,CpuReg.REG_A);
        return 3;
    }

    public static int STX_ZERO(CpuMemory cpuMemory, byte addr) {
        cpuMemory.write(addr,CpuReg.REG_X);
        return 3;
    }

    public static int CPX( byte data) {
        byte regX = CpuReg.REG_X;
        byte cmpData = (byte) (regX - data);
        CpuRegStatus.setN(cmpData);
        CpuRegStatus.setZ(cmpData);
        CpuRegStatus.setC(cmpData);
        return 2;
    }

    /**
     * A -> M
     * @param cpuMemory
     * @param addr
     * @return
     */
    public static int STA_INDIRECT_Y(CpuMemory cpuMemory, byte addr) {
        int memAddress = cpuMemory.read(addr) + (CpuReg.REG_Y&0xFF);
        cpuMemory.write(memAddress,CpuReg.REG_A);
        return 6;
    }

    public static int DEY() {
        CpuReg.REG_Y = (byte) (CpuReg.REG_Y-1);
        CpuRegStatus.setN(CpuReg.REG_Y);
        CpuRegStatus.setZ(CpuReg.REG_Y);
        return 2;
    }

    public static int CPY(byte data) {
        byte regY = CpuReg.REG_Y;
        byte cmpData = (byte) (regY - data);
        CpuRegStatus.setN(cmpData);
        CpuRegStatus.setZ(cmpData);
        CpuRegStatus.setC(cmpData);
        return 2;
    }

    public static int ADC_ABS(CpuMemory cpuMemory, byte low, byte high) {

//        CpuRegStatus.setN(cmpData);
//        CpuRegStatus.setZ(cmpData);
//        CpuRegStatus.setC(cmpData);
//        CpuRegStatus.setV(cmpData);
        return 4;
    }

    /**
     * 从栈中读取PC,然后PC=PC+1
     * PC fromS, PC + 1 -> PC
     * @param cpuMemory
     * @return
     */
    public static int RTS(CpuMemory cpuMemory) {
        byte pcHigh32 = cpuMemory.popStack();
        int pcHigh24 = cpuMemory.popStack();
        int pcLow16 = cpuMemory.popStack();
        int pcLow8 = cpuMemory.popStack();
        int pc = (pcHigh32 << 24) | (pcHigh24 << 16) | (pcLow16 << 8) | pcLow8;
        cpuMemory.setPrgPc(pc);
        return 6;
    }

    /**
     *
     * @param cpuMemory
     * @param low
     * @param high
     * @return
     */
    public static int BIT_ABS(CpuMemory cpuMemory ,byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        byte data = cpuMemory.read(addr);
        CpuRegStatus.setN(data);
        CpuRegStatus.setV((byte) ((data>>6)&1));
        CpuRegStatus.setZ((byte) (CpuReg.REG_A & data));
        return 4;
    }

    /**
     * A -> M
     * @param cpuMemory
     * @param low
     * @param high
     * @return
     */
    public static int STA_ABS_Y(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        int memAddress = cpuMemory.read(addr) + (CpuReg.REG_Y&0xFF);
        cpuMemory.write(memAddress,CpuReg.REG_A);
        return 5;
    }

    /**
     * 强制中断
     * @param cpuMemory
     * @return
     */
    public static int BRK(CpuMemory cpuMemory) {
        int pc = cpuMemory.getPrgPc() + 2;
        CpuReg.pushIntStack(cpuMemory,pc);
        CpuReg.pushIntStack(cpuMemory,cpuMemory.getSp());
        CpuRegStatus.setB((byte) 1);
        cpuMemory.setPrgPc(cpuMemory.read(0xFFFE) | (cpuMemory.read(0xFFFF) << 8));
        return 7;
    }
//    SET_SIGN(src);
//    SET_OVERFLOW(0x40 & src);    /* Copy bit 6 to OVERFLOW flag. */
//    SET_ZERO(src & AC);

//    public static int CMP_ABS(byte low, byte high) {
//        short aShort = MemUtil.getShort(low, high);
//        return 4;
//    }

    private static void pushIntStack(CpuMemory cpuMemory,int data){
        cpuMemory.pushStack((byte) (data&0xFF));
        cpuMemory.pushStack((byte) ((data>>8)&0xFF));
        cpuMemory.pushStack((byte) ((data>>16)&0xFF));
        cpuMemory.pushStack((byte) ((data>>24)&0xFF));
    }
}
