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
    private static byte REG_S_B = 1;
    /**
     * 状态寄存器 Sign Flag
     */
    private static byte REG_S_D;
    /**
     * 状态寄存器 Sign Flag
     */
    private static byte REG_S_I = 1;
    /**
     * 状态寄存器 Sign Flag
     */
    private static byte REG_S_Z;
    /**
     * 状态寄存器 Sign Flag
     */
    private static byte REG_S_C;

    /**
     * 寄存器A存储2字节16位 data -> addr
     * @param cpuMemory
     * @param low 低8位
     * @param high 高8位
     */
    public static int STA_ABS(CpuMemory cpuMemory, byte low, byte high) {
        //16位 short
        int addr = MemUtil.concatByte(low, high);
        cpuMemory.write(addr, REG_A);
        return 4;
    }

    /**
     * 将数据放入寄存器A
     * @param data
     */
    public static int LDA(byte data) {
        REG_A = data;
        setN(REG_A);
        setZ(REG_A);
        return 2;
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
        REG_X = data;
        setN(data);
        setZ(data);
        return 2;
    }
    /**
     * 将X索引寄存器的数据存入栈指针SP寄存器
     */
    public static int TXS() {
        REG_SP = REG_X;
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
        LDA(readData);
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
        REG_S_Z = (byte) ((data == 0)?1:0);
    }

    public static void setN(byte data) {
        //负数=1 正数=0
        REG_S_N = (byte) ((data >> 7) & 1);
    }
    public static byte getN() {
        return REG_S_N;
    }

    public static void setI(byte data) {
        REG_S_I = data;
    }

    public static void setD(byte data) {
        REG_S_D = data;
    }

    public static void setC(byte data) {
        REG_S_C = (byte) ((data & 0xff00) == 0 ? 1 : 0);
    }
    public static void setC1(byte data) {
        REG_S_C = data;
    }

    public static byte getZ() {
        return REG_S_Z;
    }

    public static byte getC() {
        return REG_S_C;
    }

    public static void setV(byte data) {
        REG_S_V = data;
    }

    public static void setB(byte data) {
        REG_S_B = data;
    }

    public static int getB() {
        return REG_S_B;
    }

    public static int getV() {
        return REG_S_V;
    }

    /**
     * data -> REG_Y
     * @param data
     */
    public static int LDY(byte data) {
        REG_Y = data;
        setN(data);
        setZ(data);
        return 2;
    }

    /**
     * LDA绝对X变址
     * @param low
     * @param high
     */
    public static int LDA_ABS_X(CpuMemory cpuMemory,byte low, byte high) {
        int aShort = MemUtil.concatByte(low, high) + (REG_X & 0xff);
        LDA(cpuMemory.read(aShort));
        return 4;
    }

    /**
     * 禁止中断
     */
    public static int SEI() {
        setI((byte) 1);
        return 2;
    }

    /**
     * Clear decimal mode
     */
    public static int CLD() {
        setD((byte) 0);
        return 2;
    }

    /**
     * REG_S_N == 0 切换
     * @param cpuMemory
     * @param data
     */
    public static int BPL(CpuMemory cpuMemory, byte data) {
        if(getN() == 0) {
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
        byte cmpData = (byte) (REG_A - data);
        setN(cmpData);
        setZ((byte) (cmpData&0xFF));
        setC(cmpData);
        return 2;
    }

    /**
     * Z=0切换
     * @param data
     * @return
     */
    public static int BNE(CpuMemory cpuMemory,byte data) {
        if(REG_S_Z == 0) {
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
        if(REG_S_C != 0) {
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
        REG_X = (byte) (REG_X - 1);
        setN(REG_X);
        setZ(REG_X);
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
        cpuMemory.write(addr&0xFF, REG_A);
        return 3;
    }

    public static int STX_ZERO(CpuMemory cpuMemory, byte addr) {
        cpuMemory.write(addr&0xFF, REG_X);
        return 3;
    }

    public static int CPX(byte data) {
        short regX = REG_X;
        byte cmpData = (byte) (regX - data);
        setN(cmpData);
        setZ(cmpData);
        setC(cmpData);
        return 2;
    }

    /**
     * A -> M
     * @param cpuMemory
     * @param data
     * @return
     */
    public static int STA_INDIRECT_Y(CpuMemory cpuMemory, byte data) {
        int addr = MemUtil.concatByte(cpuMemory.read(data&0xFF), cpuMemory.read(((data&0xFF) + 1)))+ (REG_Y&0xFF);
        cpuMemory.write(addr, REG_A);
        return 6;
    }

    public static int DEY() {
        REG_Y--;
        setN(REG_Y);
        setZ(REG_Y);
        return 2;
    }

    public static int CPY(byte data) {
        byte regY = REG_Y;
        short cmpData = (short) ((regY&0xFF) - (data&0xFF));
        setN1((byte) ((cmpData >> 7) & 1));
        setZ1((byte) ((cmpData & 0xff) == 0 ? 1 : 0));
        setC1((byte) ((cmpData & 0xff00) == 0 ? 1 : 0));
        return 2;
    }

    private static void setZ1(byte data) {
        REG_S_Z = data;
    }

    private static void setN1(byte data) {
        REG_S_N = data;
    }

    /**
     * 从栈中读取PC,然后PC=PC+1
     * PC fromS, PC + 1 -> PC
     * @param cpuMemory
     * @return
     */
    public static int RTS(CpuMemory cpuMemory) {
        int pc = cpuMemory.pop16Stack();
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
        setN(data);
        setV((byte) ((data>>6)&1));
        setZ((byte) (REG_A & data));
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
        int addr = MemUtil.concatByte(low, high) + (REG_Y&0xFF);
        cpuMemory.write(addr, REG_A);
        return 5;
    }

    /**
     * 强制中断
     * @param cpuMemory
     * @return
     */
    public static int BRK(CpuMemory cpuMemory) {
        setB((byte) 1);
        short pc = (short) (cpuMemory.getPrgPc() + 2);
        cpuMemory.push16Stack(pc);
        cpuMemory.pushStack(REG_S_MERGE());
        short high = cpuMemory.read(0xFFFE);
        cpuMemory.setPrgPc(high);
        setI((byte) 1);
        return 7;
    }

    public static byte REG_S_MERGE() {
        return (byte) ((getN() << 7) | (getV() << 6) | 0x20 | (getB() << 4)
                | (getD() << 3) | (getI() << 2) | (getZ() << 1) | REG_S_C);
    }

    public static void REG_S_SET(byte data) {
        REG_S_N = (byte) ((data >> 7) & 1);
        REG_S_V = (byte) ((data >> 6) & 1);
        REG_S_B = (byte) ((data >> 4) & 1);
        REG_S_D = (byte) ((data >> 3) & 1);
        REG_S_I = (byte) ((data >> 2) & 1);
        REG_S_Z = (byte) ((data >> 1) & 1);
        REG_S_C = (byte) (data & 1);
    }

    public static int getI() {
        return REG_S_I;
    }

    public static int getD() {
        return REG_S_D;
    }

    public static int INY() {
        REG_Y = (byte) (REG_Y + 1);
        setN(REG_Y);
        setZ(REG_Y);
        return 2;
    }

    public static int ORA(byte data) {
        REG_A |= data;
        setN(REG_A);
        setZ(REG_A);
        return 2;
    }

    public static int AND(byte data) {
        REG_A &= data;
        setN(REG_A);
        setZ(REG_A);
        return 2;
    }

    public static int TXA() {
        REG_A = REG_X;
        setN(REG_A);
        setZ(REG_A);
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
        setN(data);
        setZ(data);
        return 6;
    }

    /**
     * nmi中断
     */
    public static void NMI(CpuMemory cpuMemory) {
        cpuMemory.push16Stack((short) cpuMemory.getPrgPc());
        cpuMemory.pushStack(REG_S_MERGE());
        setI((byte) 1);
        int high = (cpuMemory.read(0xFFFA) & 0xff) | ((cpuMemory.read(0xFFFB) & 0xff) << 8);
        cpuMemory.setPrgPc(high);
    }

    public static int LDY_ABS(CpuMemory cpuMemory, byte low, byte high) {
        int readData = MemUtil.concatByte(low, high);
        byte data = cpuMemory.read(readData);
        LDY(data);
        return 4;
    }

    public static int LDX_ABS(CpuMemory cpuMemory, byte low, byte high) {
        int readData = MemUtil.concatByte(low, high);
        byte data = cpuMemory.read(readData);
        LDX(data);
        return 4;
    }

    public static int LDA_INDIRECT_Y(CpuMemory cpuMemory, byte data) {
        int addr = MemUtil.concatByte(cpuMemory.read(data&0xFF), cpuMemory.read((data&0xFF)+1) )+ (REG_Y&0xFF);
        byte read = cpuMemory.read(addr);
        LDA(read);
        return 5;
    }

    public static int SED() {
        setD((byte) 1);
        return 2;
    }

    public static int LDX_ABS_Y(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_Y&0xFF);
        REG_X = cpuMemory.read(addr);
        setN(REG_X);
        setZ(REG_X);
        return 4;
    }

    public static int STA_ABS_X(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high)+ (REG_X&0xFF);
        cpuMemory.write(addr, REG_A);
        return 5;
    }

    public static int LSR() {
        setC1((byte) (REG_A&1));
        REG_A = (byte) ((REG_A & 0xff) >> 1);
        setN(REG_A);
        setZ(REG_A);
        return 2;
    }

    public static int TAX() {
        REG_X = REG_A;
        setN(REG_X);
        setZ(REG_X);
        return 2;
    }

    public static int PHA(CpuMemory cpuMemory) {
        cpuMemory.pushStack(REG_A);
        return 3;
    }

    public static int ORA_ZERO(CpuMemory cpuMemory, byte data) {
        REG_A |= cpuMemory.read(data&0xFF);
        setN(REG_A);
        setZ(REG_A);
        return 3;
    }

    public static int PLA(CpuMemory cpuMemory) {
        REG_A = cpuMemory.popStack();
        return 4;
    }

    public static int ROL() {
        byte regA = REG_A;
        REG_A = (byte) ((regA << 1) | REG_S_C);
        setC1((byte)((regA >> 7) & 1));
        setN(REG_A);
        setZ(REG_A);
        return 2;
    }

    public static int AND_ABS(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_X & 0xff);
        REG_A &= cpuMemory.read(addr);
        setN(REG_A);
        setZ(REG_A);
        return 4;
    }

    public static int BEQ(CpuMemory cpuMemory, byte data) {
        if (getZ() == 1) {
            int clk = 2 + (cpuMemory.getPrgPc() & 0xff00) == ((cpuMemory.getPrgPc() + data) & 0xff00) ? 1 : 2;
            cpuMemory.setPrgPc(cpuMemory.getPrgPc() + data);
            return clk;
        }
        return 2;
    }

    public static int INX() {
        REG_X = (byte) (REG_X + 1);
        setN(REG_X);
        setZ(REG_X);
        return 2;
    }

    public static int SEC() {
        setC1((byte) 1);
        return 2;
    }

    public static int SBC_ABS_Y(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_Y & 0xFF);
        byte read = cpuMemory.read(addr);
        int sbc = (REG_A & 0xff) - (read& 0xff) - (REG_S_C != 0 ? 0 : 1);
        setC1((byte) ((sbc & 0xff00) == 0 ? 1 : 0));
        setN((byte) (sbc&0xff));
        setZ((byte) (sbc&0xff));
        setV((byte) ((((REG_A ^ read) & 0x80) != 0)&& (((REG_A ^ sbc) & 0x80) != 0) ? 1 : 0));
        REG_A = (byte) sbc;
        return 4;
    }

    public static int BCC(CpuMemory cpuMemory, byte data) {
        if(REG_S_C == 0) {
            int clk = 2 + (cpuMemory.getPrgPc() & 0xff00) == ((cpuMemory.getPrgPc() + data) & 0xff00) ? 1 : 2;
            cpuMemory.setPrgPc(cpuMemory.getPrgPc() + data);
            return clk;
        }
        return 2;
    }

    public static int EOR_ZERO(CpuMemory cpuMemory, byte addr) {
        EOR_A(cpuMemory.read(addr&0xFF));
        return 3;
    }

    public static int CLC() {
        setC1((byte) 0);
        return 2;
    }

    public static int ROR_ABS_X(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_X & 0xFF);
        byte read = cpuMemory.read(addr);
        byte read2 = (byte) (((read & 0xff) >> 1) | (REG_S_C << 7));
        setC1((byte) (read&1));
        setN(read2);
        setZ(read2);
        cpuMemory.write(addr,read2);
        return 7;
    }

    public static int RTI(CpuMemory cpuMemory) {
        REG_S_SET(cpuMemory.popStack());
        cpuMemory.setPrgPc(cpuMemory.pop16Stack());
        return 6;
    }

    public static int DEC_ABS(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        byte read = (byte) (cpuMemory.read(addr)-1);
        cpuMemory.write(addr, read);
        setN(read);
        setZ(read);
        return 6;
    }

    public static int INC_ZERO(CpuMemory cpuMemory, byte addr) {
        byte data = (byte) (cpuMemory.read(addr&0xFF)+1);
        cpuMemory.write(addr&0xFF,data);
        setN(data);
        setZ(data);
        return 5;
    }

    public static int ASL_A() {
        setC1((byte) ((REG_A >> 7) & 1));
        REG_A <<= 1;
        setN(REG_A);
        setZ(REG_A);
        return 2;
    }

    public static int TAY() {
        REG_Y = REG_A;
        setN(REG_Y);
        setZ(REG_Y);
        return 2;
    }

    public static int JMP_INDIRECT(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        int pc = MemUtil.concatByte(cpuMemory.read(addr), cpuMemory.read(addr + 1));
        cpuMemory.setPrgPc(pc);
        return 5;
    }

    public static int LDA_ABS_Y(CpuMemory cpuMemory, byte low, byte high) {
        int aShort = MemUtil.concatByte(low, high) + (REG_Y & 0xff);
        LDA(cpuMemory.read(aShort));
        return 4;
    }

    public static int ADC_ABS(CpuMemory cpuMemory, byte low, byte high) {
        byte data = cpuMemory.read(MemUtil.concatByte(low, high));
        ADC(data);
        return 4;
    }

    public static int ADC(byte data) {
        int adcData = (REG_A & 0xff) + (data & 0xff) + (REG_S_C & 0xff);
        setN((byte) (adcData&0xFF));
        setZ((byte) (adcData&0xFF));
        setC1((byte) (adcData>>8));
        setV((byte) ((((REG_A ^ data) & 0x80) == 0 && ((REG_A ^adcData) & 0x80) != 0) ? 1 : 0));
        REG_A = (byte) (adcData & 0xff);
        return 2;
    }

    public static int STY_ABS(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        cpuMemory.write(addr,REG_Y);
        return 4;
    }

    public static int LDA_ZERO(CpuMemory cpuMemory, byte addr) {
        LDA(cpuMemory.read(addr&0xFF));
        return 4;
    }

    public static int DEC_ZERO(CpuMemory cpuMemory, byte addr) {
        byte read = (byte) (cpuMemory.read(addr & 0xFF) - 1);
        cpuMemory.write(addr & 0xFF,read);
        setN(read);
        setZ(read);
        return 5;
    }

    public static int TYA() {
        REG_A = REG_Y;
        setN(REG_A);
        setZ(REG_A);
        return 2;
    }

    public static int ADC_ZERO(CpuMemory cpuMemory, byte addr) {
        byte data = cpuMemory.read(addr&0xFF);
        ADC(data);
        return 3;
    }

    public static int LDX_ZERO(CpuMemory cpuMemory, byte addr) {
        LDX(cpuMemory.read(addr&0xFF));
        return 3;
    }

    public static int STX_ABS(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        cpuMemory.write(addr,REG_X);
        return 4;
    }

    public static int BMI(CpuMemory cpuMemory, byte data) {
        if(REG_S_N == 1) {
            int clk = 2 + (cpuMemory.getPrgPc() & 0xff00) == ((cpuMemory.getPrgPc() + data) & 0xff00) ? 1 : 2;
            cpuMemory.setPrgPc(cpuMemory.getPrgPc() + data);
            return clk;
        }
        return 2;
    }

    public static int ADC_ABS_Y(CpuMemory cpuMemory, byte low, byte high) {
        int aShort = MemUtil.concatByte(low, high) + (REG_Y & 0xff);
        ADC(cpuMemory.read(aShort));
        return 4;
    }

    public static int SBC(byte data) {
        int sbcData = (REG_A & 0xff) - (data & 0xff) - (REG_S_C != 0 ? 0 : 1);
        REG_S_C = (byte) ((sbcData & 0xff00) == 0 ? 1 : 0);
        setN((byte) (sbcData&0xFF));
        setZ((byte) (sbcData&0xFF));
        REG_S_V = (byte) ((((REG_A ^ data) & 0x80) != 0) && (((REG_A ^ sbcData) & 0x80) != 0) ? 1 : 0);
        REG_A = (byte) sbcData;
        return 2;
    }

    public static int STY_ZERO(CpuMemory cpuMemory, byte addr) {
        cpuMemory.write(addr&0xFF,REG_Y);
        return 3;
    }

    public static int BIT_ZERO(CpuMemory cpuMemory, byte addr) {
        byte data = cpuMemory.read(addr&0xFF);
        setN(data);
        setZ((byte) (REG_A & data));
        REG_S_V = (byte) ((data >> 6) & 1);
        return 3;
    }

    public static int LDY_ZERO(CpuMemory cpuMemory, byte addr) {
        LDY(cpuMemory.read(addr&0xFF));
        return 3;
    }

    public static int CMP_ABS(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        CMP(cpuMemory.read(addr));
        return 4;
    }

    public static int CMP_ABS_Y(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_Y & 0xff);
        CMP(cpuMemory.read(addr));
        return 4;
    }

    public static int EOR_A(byte data) {
        REG_A ^= data;
        setN(REG_A);
        setZ(REG_A);
        return 2;
    }

    public static int ROL_ZERO(CpuMemory cpuMemory,byte addr) {
        byte data = cpuMemory.read(addr&0xFF);
        byte rol = (byte) ((data << 1) | REG_S_C);
        setC1((byte)((data >> 7) & 1));
        setN(rol);
        setZ(rol);
        cpuMemory.write(addr,rol);
        return 5;
    }

    public static int LSR_ZERO(CpuMemory cpuMemory,byte addr) {
        byte data = cpuMemory.read(addr);
        setC1((byte) (data&1));
        byte lsr = (byte) ((data & 0xff) >> 1);
        setN(lsr);
        setZ(lsr);
        return 5;
    }

    public static int DEC_ABS_X(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_X & 0xff);
        byte data = (byte) (cpuMemory.read(addr) - 1);
        cpuMemory.write(addr,data);
        setN(data);
        setZ(data);
        return 7;
    }

    public static int LSR_ABS(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        byte data = cpuMemory.read(addr);
        setC1((byte) (data&1));
        byte lsr = (byte) ((data & 0xff) >> 1);
        setN(lsr);
        setZ(lsr);
        cpuMemory.write(addr,lsr);
        return 6;
    }

    public static int ROR_A() {
        byte read2 = (byte) (((REG_A & 0xff) >> 1) | (REG_S_C << 7));
        setC1((byte) (REG_A&1));
        setN(read2);
        setZ(read2);
        REG_A = read2;
        return 2;
    }

    public static int ROL_ABS(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        byte data = cpuMemory.read(addr);
        byte rol = (byte) ((data << 1) | REG_S_C);
        setC1((byte)((data >> 7) & 1));
        setN(rol);
        setZ(rol);
        cpuMemory.write(addr,rol);
        return 6;
    }

    public static int CMP_ZERO(CpuMemory cpuMemory, byte addr) {
        byte data = cpuMemory.read(addr&0xFF);
        CMP(data);
        return 3;
    }

    public static int LDY_ABS_X(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_X & 0xff);
        byte data = cpuMemory.read(addr);
        LDY(data);
        return 4;
    }

    public static int STA_ZERO_X(CpuMemory cpuMemory, byte addr) {
        byte addr2 = (byte) (cpuMemory.read(addr&0xFF) + (REG_X & 0xff));
        STA_ZERO(cpuMemory,addr2);
        return 4;
    }

    public static int CMP_ABS_X(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_X & 0xff);
        CMP(cpuMemory.read(addr));
        return 4;
    }

    public static int PHP(CpuMemory cpuMemory) {
        cpuMemory.pushStack(CpuRegister.REG_S_MERGE());
        return 3;
    }

    public static int CPX_ZERO(CpuMemory cpuMemory, byte addr) {
        CPX(cpuMemory.read(addr&0xFF));
        return 3;
    }

    public static int ROR_ZERO(CpuMemory cpuMemory, byte addr) {
        byte read = cpuMemory.read(addr&0xFF);
        byte read2 = (byte) (((read & 0xff) >> 1) | (REG_S_C << 7));
        setC1((byte) (read&1));
        setN(read2);
        setZ(read2);
        cpuMemory.write(addr,read2);
        return 5;
    }

    public static int LDA_ZERO_X(CpuMemory cpuMemory, byte addr) {
        byte data = (byte) ((addr&0xFF) + (REG_X & 0xff));
        LDA(cpuMemory.read(data&0xFF));
        return 4;
    }

    public static int AND_ZERO(CpuMemory cpuMemory, byte addr) {
        byte data = cpuMemory.read(addr&0xFF);
        AND(data);
        return 3;
    }

    public static int PLP(CpuMemory cpuMemory) {
        CpuRegister.REG_S_SET(cpuMemory.popStack());
        return 4;
    }

    public static int ASL_ZERO(CpuMemory cpuMemory, byte addr) {
        byte data = cpuMemory.read(addr&0xFF);
        CpuRegister.setC1((byte) ((data >> 7) & 1));
        byte data1= (byte) (data << 1);
        CpuRegister.setN(data1);
        CpuRegister.setZ(data1);
        cpuMemory.write(addr, data1);
        return 5;
    }

    public static int AND_INDIRECT_Y(CpuMemory cpuMemory, byte data) {
        int addr = MemUtil.concatByte(cpuMemory.read(data&0xFF), cpuMemory.read((data&0xFF)+1) )+ (REG_Y&0xFF);
        byte read = cpuMemory.read(addr);
        AND(read);
        return 5;
    }

    public static int SBC_ZERO(CpuMemory cpuMemory, byte addr) {
        byte data = cpuMemory.read(addr&0xFF);
        SBC(data);
        return 3;
    }

    public static int LDY_ZERO_X(CpuMemory cpuMemory, byte addr) {
        byte data = (byte) (addr+ (REG_X & 0xff));
        LDY(cpuMemory.read(data));
        return 4;
    }

    public static int NOP() {
        return 2;
    }

    public static int EOR_ABS(CpuMemory cpuMemory, byte low, byte high) {
        int data = MemUtil.concatByte(low, high);
        EOR_A(cpuMemory.read(data));
        return 4;
    }

    public static int TSX() {
        REG_X = REG_SP;
        CpuRegister.setN(REG_X);
        CpuRegister.setZ(REG_X);
        return 2;
    }

    public static int ASL_ABS(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        byte data = cpuMemory.read(addr);
        CpuRegister.setC1((byte) ((data >> 7) & 1));
        byte data1= (byte) (data << 1);
        CpuRegister.setN(data1);
        CpuRegister.setZ(data1);
        cpuMemory.write(addr, data1);
        return 6;
    }

    public static int SBC_ABS(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        byte data = cpuMemory.read(addr);
        SBC(data);
        return 4;
    }

    public static int EOR_INDIRECT_Y(CpuMemory cpuMemory, byte data) {
        int addr = MemUtil.concatByte(cpuMemory.read(data&0xFF), cpuMemory.read((data&0xFF)+1) )+ (REG_Y&0xFF);
        EOR_A(cpuMemory.read(addr));
        return 5;
    }

    public static int INC_ABS_X(CpuMemory cpuMemory, byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_X & 0xff);
        byte data = (byte) (cpuMemory.read(addr) + 1);
        cpuMemory.write(addr, data);
        setN(data);
        setZ(data);
        return 7;
    }
}
