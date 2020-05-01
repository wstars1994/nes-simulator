package com.iwstars.mcnes.core.cpu;

import com.iwstars.mcnes.util.MemUtil;

/**
 * @description: CPU寄存器
 * @author: WStars
 * @date: 2020-04-19 10:48
 */
public class CpuRegister {
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
    private static byte REG_SP = (byte) 0xFF;

    /**
     * 状态寄存器 Sign Flag 如果操作结果为负，则设置此项(1)；如果为正，则清除此项(0)
     */
    private static byte REG_S_N;
    /**
     * 状态寄存器 Sign Flag
     */
    private static byte REG_S_V;
    /**
     * 当一个软件中断 (BRK 指令)被执行的时候，这个标记被设置.
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

    /**
     * 将数据放入寄存器A
     * @param data
     */
    public static int LDA(byte data) {
        CpuRegister.REG_A = data;
        CpuRegister.setN(CpuRegister.REG_A);
        CpuRegister.setZ(CpuRegister.REG_A);
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
        int addr = MemUtil.concatByte(low, high);
        cpuMemory.write(addr, CpuRegister.REG_A);
        return 4;
    }

    /**
     * 获取寄存器A值
     * @return
     */
    public static int getReg_A(){
        return REG_A;
    }
    /**
     * data->REG_X
     * @param data
     */
    public static int LDX(byte data) {
        CpuRegister.REG_X = data;
        CpuRegister.setN(data);
        CpuRegister.setZ(data);
        return 2;
    }
    /**
     * 将X索引寄存器的数据存入栈指针SP寄存器
     */
    public static int TXS() {
        CpuRegister.REG_SP = CpuRegister.REG_X;
        return 2;
    }

    /**
     * 存储2字节16位 data -> addr
     * @param low 低8位
     * @param high 高8位
     */
    public static int LDA_ABS(CpuMemory cpuMemory,byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        byte readData = cpuMemory.read(addr);
        CpuRegister.LDA(readData);
        return 4;
    }

    public static int getReg_X(){
        return REG_X;
    }

    public static byte getReg_S() {
        return REG_SP;
    }
    public static void setReg_S(byte sp) {
        REG_SP = sp;
    }

    public static int getReg_Y() {
        return REG_Y;
    }
    public static void setZ(byte data) {
        CpuRegister.REG_S_Z = (byte) ((data == 0)?1:0);
    }

    public static void setN(byte data) {
        //负数=1 正数=0
        CpuRegister.REG_S_N = (byte) ((data >> 7) & 1);
    }
    public static byte getN() {
        return CpuRegister.REG_S_N;
    }

    public static void setI(byte data) {
        CpuRegister.REG_S_I = data;
    }

    public static void setD(byte data) {
        CpuRegister.REG_S_D = data;
    }

    public static void setC(byte data) {
        CpuRegister.REG_S_C = (byte) ((data & 0xff00) == 0 ? 1 : 0);
    }
    public static void setC1(byte data) {
        CpuRegister.REG_S_C = data;
    }

    public static byte getZ() {
        return CpuRegister.REG_S_Z;
    }

    public static byte getC() {
        return CpuRegister.REG_S_C;
    }

    public static void setV(byte data) {
        CpuRegister.REG_S_V = data;
    }

    public static void setB(byte data) {
        CpuRegister.REG_S_B = data;
    }

    private static int getB() {
        return CpuRegister.REG_S_B;
    }

    private static int getV() {
        return CpuRegister.REG_S_V;
    }

    /**
     * data -> REG_Y
     * @param data
     */
    public static int LDY(byte data) {
        CpuRegister.REG_Y = data;
        CpuRegister.setN(data);
        CpuRegister.setZ(data);
        return 2;
    }

    /**
     * LDA绝对X变址
     * @param low
     * @param high
     */
    public static int LDA_ABS_X(CpuMemory cpuMemory,byte low, byte high) {
        int aShort = MemUtil.concatByte(low, high) + (CpuRegister.REG_X & 0xff);
        CpuRegister.LDA(cpuMemory.read(aShort));
        return 4;
    }

    /**
     * 禁止中断
     */
    public static int SEI() {
        CpuRegister.setI((byte) 1);
        return 2;
    }

    /**
     * Clear decimal mode
     */
    public static int CLD() {
        CpuRegister.setD((byte) 0);
        return 2;
    }

    /**
     * REG_S_N == 0 切换
     * @param cpuMemory
     * @param data
     */
    public static int BPL(CpuMemory cpuMemory, byte data) {
        if(CpuRegister.getN() == 0) {
            int cycle =  2 + (cpuMemory.getPrgPc() & 0xff00) == ((cpuMemory.getPrgPc() + data) & 0xff00) ? 1 : 2;
            cpuMemory.setPrgPc(cpuMemory.getPrgPc() + data);
            return cycle;
        }
        return 2;
    }

    /**
     * CMP Compare memory and accumulator
     * @param data
     * @return
     */
    public static int CMP(byte data) {
        byte regA = CpuRegister.REG_A;
        byte cmpData = (byte) (regA - data);
        CpuRegister.setN(cmpData);
        CpuRegister.setZ(cmpData);
        CpuRegister.setC1((byte) ((cmpData & 0xff00) == 0 ? 1 : 0));
        return 2;
    }

    /**
     * Z=0切换
     * @param data
     * @return
     */
    public static int BNE(CpuMemory cpuMemory,byte data) {
        if(CpuRegister.getZ() == 0) {
            int clk = 2 + (cpuMemory.getPrgPc() & 0xff00) == ((cpuMemory.getPrgPc() + data) & 0xff00) ? 1 : 2;
            cpuMemory.setPrgPc(cpuMemory.getPrgPc() + data);
            return clk;
        }
        return 2;
    }

    /**
     * C!=0 切换
     * @param cpuMemory
     * @param data
     * @return
     */
    public static int BCS(CpuMemory cpuMemory,byte data) {
        if(CpuRegister.getC() != 0) {
            int clk = 2 + (cpuMemory.getPrgPc() & 0xff00) == ((cpuMemory.getPrgPc() + data) & 0xff00) ? 1 : 2;
            cpuMemory.setPrgPc(cpuMemory.getPrgPc() + data);
            return clk;
        }
        return 2;
    }

    /**
     * X=X-1
     * @return
     */
    public static int DEX() {
        CpuRegister.REG_X = (byte) (CpuRegister.REG_X - 1);
        CpuRegister.setN(CpuRegister.REG_X);
        CpuRegister.setZ(CpuRegister.REG_X);
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
        short pc = (short) (cpuMemory.getPrgPc()-1);
        cpuMemory.push16Stack(pc);
        cpuMemory.setPrgPc(aShort);
        return 6;
    }

    public static int STA_ZERO(CpuMemory cpuMemory, byte addr) {
        cpuMemory.write(addr, CpuRegister.REG_A);
        return 3;
    }

    public static int STX_ZERO(CpuMemory cpuMemory, byte addr) {
        cpuMemory.write(addr, CpuRegister.REG_X);
        return 3;
    }

    public static int CPX(byte data) {
        byte regX = CpuRegister.REG_X;
        byte cmpData = (byte) (regX - data);
        CpuRegister.setN(cmpData);
        CpuRegister.setZ(cmpData);
        CpuRegister.setC(cmpData);
        return 2;
    }

    /**
     * A -> M
     * @param cpuMemory
     * @param data
     * @return
     */
    public static int STA_INDIRECT_Y(CpuMemory cpuMemory, byte data) {
        int addr = MemUtil.concatByte(cpuMemory.read(data), cpuMemory.read((data + 1)))+ (CpuRegister.REG_Y&0xFF);
        cpuMemory.write(addr, CpuRegister.REG_A);
        return 6;
    }

    public static int DEY() {
        CpuRegister.REG_Y--;
        CpuRegister.setN(CpuRegister.REG_Y);
        CpuRegister.setZ(CpuRegister.REG_Y);
        return 2;
    }

    public static int CPY(byte data) {
        byte regY = CpuRegister.REG_Y;
        short cmpData = (short) ((regY&0xFF) - (data&0xFF));
        CpuRegister.setN1((byte) ((cmpData >> 7) & 1));
        CpuRegister.setZ1((byte) ((cmpData & 0xff) == 0 ? 1 : 0));
        CpuRegister.setC1((byte) ((cmpData & 0xff00) == 0 ? 1 : 0));
        return 2;
    }

    private static void setZ1(byte data) {
        CpuRegister.REG_S_Z = data;
    }

    private static void setN1(byte data) {
        CpuRegister.REG_S_N = data;
    }

    /**
     * 从栈中读取PC,然后PC=PC+1
     * PC fromS, PC + 1 -> PC
     * @param cpuMemory
     * @return
     */
    public static int RTS(CpuMemory cpuMemory) {
        short pcLow16 = (short) (cpuMemory.popStack()&0xFF);
        short pcLow8 = (short) (cpuMemory.popStack()&0xFF);
        int pc = (pcLow16 << 8) | pcLow8;
        cpuMemory.setPrgPc(pc + 1);
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
        CpuRegister.setN(data);
        CpuRegister.setV((byte) ((data>>6)&1));
        CpuRegister.setZ((byte) (CpuRegister.REG_A & data));
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
        int addr = MemUtil.concatByte(low, high) + (CpuRegister.REG_Y&0xFF);
        cpuMemory.write(addr, CpuRegister.REG_A);
        return 5;
    }

    /**
     * 强制中断
     * @param cpuMemory
     * @return
     */
    public static int BRK(CpuMemory cpuMemory) {
        short pc = (short) (cpuMemory.getPrgPc() + 2);
        cpuMemory.push16Stack(pc);
        cpuMemory.pushStack(CpuRegister.REG_S_MERGE());
        CpuRegister.setB((byte) 1);
        short high = cpuMemory.read(0xFFFE);
        cpuMemory.setPrgPc(high);
        return 7;
    }

    public static byte REG_S_MERGE() {
        return (byte) ((CpuRegister.getN() << 7) | (CpuRegister.getV() << 6) | 0x20 | (CpuRegister.getB() << 4)
                | (CpuRegister.getD() << 3) | (CpuRegister.getI() << 2) | (CpuRegister.getZ() << 1) | CpuRegister.getC());
    }

    public static void REG_S_SET(byte data) {
        CpuRegister.REG_S_N = (byte) ((data >> 7) & 1);
        CpuRegister.REG_S_V = (byte) ((data >> 6) & 1);
        CpuRegister.REG_S_B = (byte) ((data >> 4) & 1);
        CpuRegister.REG_S_D = (byte) ((data >> 3) & 1);
        CpuRegister.REG_S_I = (byte) ((data >> 2) & 1);
        CpuRegister.REG_S_Z = (byte) ((data >> 1) & 1);
        CpuRegister.REG_S_C = (byte) (data & 1);
    }

    private static int getI() {
        return CpuRegister.REG_S_I;
    }

    private static int getD() {
        return CpuRegister.REG_S_D;
    }

    public static int INY() {
        CpuRegister.REG_Y = (byte) (CpuRegister.REG_Y + 1);
        CpuRegister.setN(CpuRegister.REG_Y);
        CpuRegister.setZ(CpuRegister.REG_Y);
        return 2;
    }

    public static int ORA(byte data) {
        CpuRegister.REG_A |= data;
        CpuRegister.setN(CpuRegister.REG_A);
        CpuRegister.setZ(CpuRegister.REG_A);
        return 2;
    }

    public static int AND(byte data) {
        CpuRegister.REG_A &= data;
        CpuRegister.setN(CpuRegister.REG_A);
        CpuRegister.setZ(CpuRegister.REG_A);
        return 2;
    }

    public static int TXA() {
        CpuRegister.REG_A = CpuRegister.REG_X;
        CpuRegister.setN(CpuRegister.REG_A);
        CpuRegister.setZ(CpuRegister.REG_A);
        return 2;
    }

    public static int JMP_ABS(CpuMemory cpuMemory, byte low, byte high) {
        int aShort = MemUtil.concatByte(low, high);
        cpuMemory.setPrgPc(aShort);
        return 3;
    }

    public static int INC_ABS(CpuMemory cpuMemory, byte low, byte high) {
        int aShort = MemUtil.concatByte(low, high);
        byte data = (byte) (cpuMemory.read(aShort) + 1);
        cpuMemory.write(aShort, data);
        CpuRegister.setN(data);
        CpuRegister.setZ(data);
        return 6;
    }

    /**
     * nmi中断
     */
    public static void NMI(CpuMemory cpuMemory) {
        cpuMemory.push16Stack((short) cpuMemory.getPrgPc());
        cpuMemory.pushStack(CpuRegister.REG_S_MERGE());
        CpuRegister.setB((byte) 1);
        int high = (cpuMemory.read(0xFFFA) & 0xff) | ((cpuMemory.read(0xFFFA + 1) & 0xff) << 8);
        cpuMemory.setPrgPc(high);
    }

    public static int LDY_ABS(CpuMemory cpuMemory, byte low, byte high) {
        int readData = MemUtil.concatByte(low, high);
        byte data = cpuMemory.read(readData);
        CpuRegister.LDY(data);
        return 4;
    }

    public static int LDX_ABS(CpuMemory cpuMemory, byte low, byte high) {
        int readData = MemUtil.concatByte(low, high);
        byte data = cpuMemory.read(readData);
        CpuRegister.LDX(data);
        return 4;
    }

    public static int LDA_INDIRECT_Y(CpuMemory cpuMemory, byte data) {
        int addr = MemUtil.concatByte(cpuMemory.read(data), cpuMemory.read(data + 1)) + (CpuRegister.REG_Y&0xFF);
        byte read = cpuMemory.read(addr);
        CpuRegister.LDA(read);
        return 5;
    }

    public static int SED() {
        CpuRegister.setD((byte) 1);
        return 2;
    }

    public static int LDX_ABS_Y(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (CpuRegister.REG_Y&0xFF);
        CpuRegister.REG_X = cpuMemory.read(addr);
        CpuRegister.setN(REG_X);
        CpuRegister.setZ(REG_X);
        return 4;
    }

    public static int STA_ABS_X(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high)+ (CpuRegister.REG_X&0xFF);
        cpuMemory.write(addr, CpuRegister.REG_A);
        return 5;
    }

    public static int LSR() {
        CpuRegister.setC((byte) (CpuRegister.REG_A&1));
        CpuRegister.REG_A = (byte) ((CpuRegister.REG_A & 0xff) >> 1);
        CpuRegister.setN(CpuRegister.REG_A);
        CpuRegister.setZ(CpuRegister.REG_A);
        return 2;
    }

    public static int TAX() {
        CpuRegister.REG_X = CpuRegister.REG_A;
        CpuRegister.setN(CpuRegister.REG_X);
        CpuRegister.setZ(CpuRegister.REG_X);
        return 2;
    }

    public static int PHA(CpuMemory cpuMemory) {
        cpuMemory.pushStack(CpuRegister.REG_A);
        return 3;
    }

    public static int ORA_ZERO(CpuMemory cpuMemory, byte data) {
        CpuRegister.REG_A |= cpuMemory.read(data);
        CpuRegister.setN(CpuRegister.REG_A);
        CpuRegister.setZ(CpuRegister.REG_A);
        return 3;
    }

    public static int PLA(CpuMemory cpuMemory) {
        CpuRegister.REG_A = cpuMemory.popStack();
        return 4;
    }

    public static int ROL() {
        byte regA = CpuRegister.REG_A;
        CpuRegister.REG_A = (byte) ((regA << 1) | CpuRegister.getC());
        CpuRegister.setC1((byte)((regA >> 7) & 1));
        CpuRegister.setN(CpuRegister.REG_A);
        CpuRegister.setZ(CpuRegister.REG_A);
        return 2;
    }

    public static int AND_ABS(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (CpuRegister.REG_X & 0xff);
        CpuRegister.REG_A &= cpuMemory.read(addr);
        CpuRegister.setN(CpuRegister.REG_A);
        CpuRegister.setZ(CpuRegister.REG_A);
        return 4;
    }

    public static int BEQ(CpuMemory cpuMemory, byte data) {
        if (CpuRegister.getZ() == 1) {
            int clk = 2 + (cpuMemory.getPrgPc() & 0xff00) == ((cpuMemory.getPrgPc() + data) & 0xff00) ? 1 : 2;
            cpuMemory.setPrgPc(cpuMemory.getPrgPc() + data);
            return clk;
        }
        return 2;
    }

    public static int INX() {
        CpuRegister.REG_X = (byte) (CpuRegister.REG_X + 1);
        CpuRegister.setN(CpuRegister.REG_X);
        CpuRegister.setZ(CpuRegister.REG_X);
        return 2;
    }

    public static int SEC() {
        CpuRegister.setC1((byte) 1);
        return 2;
    }

    public static int SBC_ABS_Y(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (CpuRegister.REG_Y & 0xFF);
        byte read = cpuMemory.read(addr);
        int sbc = (CpuRegister.REG_A & 0xff) - (read& 0xff) - (CpuRegister.getC() != 0 ? 0 : 1);
        CpuRegister.setC1((byte) ((sbc & 0xff00) == 0 ? 1 : 0));
        CpuRegister.setN((byte) (sbc&0xff));
        CpuRegister.setZ((byte) (sbc&0xff));
        CpuRegister.setV((byte) ((((CpuRegister.REG_A ^ read) & 0x80) != 0)&& (((CpuRegister.REG_A ^ sbc) & 0x80) != 0) ? 1 : 0));
        CpuRegister.REG_A = (byte) sbc;
        return 4;
    }

    public static int BCC(CpuMemory cpuMemory, byte data) {
        if(CpuRegister.getC() == 0) {
            int clk = 2 + (cpuMemory.getPrgPc() & 0xff00) == ((cpuMemory.getPrgPc() + data) & 0xff00) ? 1 : 2;
            cpuMemory.setPrgPc(cpuMemory.getPrgPc() + data);
            return clk;
        }
        return 2;
    }

    public static int EOR_ZERO(CpuMemory cpuMemory, byte addr) {
        CpuRegister.REG_A ^= cpuMemory.read(addr);
        CpuRegister.setN(CpuRegister.REG_A);
        CpuRegister.setZ(CpuRegister.REG_A);
        return 3;
    }

    public static int CLC() {
        CpuRegister.setC1((byte) 0);
        return 2;
    }

    public static int ROR_ABS_X(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (CpuRegister.REG_X & 0xFF);
        byte read = cpuMemory.read(addr);
        byte read2 = (byte) (((read & 0xff) >> 1) | (CpuRegister.getC() << 7));
        CpuRegister.setC1((byte) (read&1));
        CpuRegister.setN(read2);
        CpuRegister.setZ(read2);
        cpuMemory.write(addr,read2);
        return 7;
    }

    public static int RTI(CpuMemory cpuMemory) {
        CpuRegister.REG_S_SET(cpuMemory.popStack());
        cpuMemory.setPrgPc(cpuMemory.pop16Stack());
        return 6;
    }
}
