package com.wxclog.core.cpu;

import com.wxclog.util.MemUtil;

/**
 * @description: CPU寄存器
 * @author: WStars
 * @date: 2020-04-19 10:48
 */
public class CpuRegister {
    
    private CpuMemory cpuMemory;

    public CpuRegister(CpuMemory cpuMemory) {
        this.cpuMemory = cpuMemory;
    }

    /**
     * A累加器,X,Y
     */
    private byte REG_A, REG_X, REG_Y;
    /**
     * 栈指针
     */
    private byte REG_SP = (byte) 0xFF;

    /**
     * 状态寄存器
     */
    private byte REG_S_N,REG_S_V,REG_S_B = 1,REG_S_D,REG_S_I = 1,REG_S_Z,REG_S_C;

    /**
     * 寄存器A存储2字节16位 data -> addr
     * @param low 低8位
     * @param high 高8位
     */
    public int STA_ABS( byte low, byte high) {
        //16位 short
        int addr = MemUtil.concatByte(low, high);
        cpuMemory.write(addr, REG_A);
        return 4;
    }

    /**
     * 将数据放入寄存器A
     * @param data
     */
    public int LDA(byte data) {
        REG_A = data;
        setN(REG_A);
        setZ(REG_A);
        return 2;
    }

    /**
     * 获取寄存器A值
     * @return
     */
    public int getReg_A(){
        return REG_A;
    }
    /**
     * data->REG_X
     * @param data
     */
    public int LDX(byte data) {
        REG_X = (byte) (data&0xFF);
        setN(data);
        setZ(data);
        return 2;
    }
    /**
     * 将X索引寄存器的数据存入栈指针SP寄存器
     */
    public int TXS() {
        REG_SP = REG_X;
        return 2;
    }

    /**
     * 存储2字节16位 data -> addr
     * @param low 低8位
     * @param high 高8位
     */
    public int LDA_ABS(byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        byte readData = cpuMemory.read(addr);
        LDA(readData);
        return 4;
    }

    public int getReg_X(){
        return REG_X;
    }

    public byte getReg_S() {
        return REG_SP;
    }
    public void setReg_S(byte sp) {
        REG_SP = sp;
    }

    public int getReg_Y() {
        return REG_Y;
    }
    public void setZ(byte data) {
        REG_S_Z = (byte) ((data == 0)?1:0);
    }

    public void setN(byte data) {
        //负数=1 正数=0
        REG_S_N = (byte) ((data >> 7) & 1);
    }
    public byte getN() {
        return REG_S_N;
    }

    public void setI(byte data) {
        REG_S_I = data;
    }

    public void setD(byte data) {
        REG_S_D = data;
    }

    public void setC(byte data) {
        REG_S_C = (byte) ((data & 0xff00) == 0 ? 1 : 0);
    }
    public void setC1(byte data) {
        REG_S_C = data;
    }

    public byte getZ() {
        return REG_S_Z;
    }

    public byte getC() {
        return REG_S_C;
    }

    public void setV(byte data) {
        REG_S_V = data;
    }

    public void setB(byte data) {
        REG_S_B = data;
    }

    public int getB() {
        return REG_S_B;
    }

    public int getV() {
        return REG_S_V;
    }


    //---------------------------寻址方式 开始-----------------------------------

    int zero(byte data,byte reg){
        return ((data&0XFF) + (reg & 0xff))&0xff;
    }

    byte indirectY(byte data){
        int addr = MemUtil.concatByte(cpuMemory.read(data&0xFF), cpuMemory.read((data&0xFF)+1) )+ (REG_Y & 0xFF);
        return cpuMemory.read(addr);
    }
    int indirectX(byte data){
        byte addr = (byte) ((data & 0xFF) + (REG_X & 0xFF));
        return MemUtil.concatByte(cpuMemory.read(data&0xFF), cpuMemory.read((addr & 0xFF) + 1));
    }

    private byte branch(byte data,boolean flag) {
        byte cpuCycle = 2;
        if(flag) {
            cpuCycle += (cpuMemory.getPrgPc() & 0xff00) == ((cpuMemory.getPrgPc() + data) & 0xff00) ? 1 : 2;
            cpuMemory.setPrgPc(cpuMemory.getPrgPc() + data);
        }
        return cpuCycle;
    }

    //---------------------------寻址方式 结束-----------------------------------

    /**
     * data -> REG_Y
     * @param data
     */
    public int LDY(byte data) {
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
    public int LDA_ABS_X(byte low, byte high) {
        int aShort = MemUtil.concatByte(low, high) + (REG_X & 0xff);
        byte read = cpuMemory.read(aShort);
        LDA(read);
        return 4;
    }

    /**
     * 禁止中断
     */
    public int F_I(byte data) {
        setI(data);
        return 2;
    }

    /**
     * 清除十进制标志位
     */
    public int CLD() {
        setD((byte) 0);
        return 2;
    }

    /**
     * REG_S_N == 0 切换
     * @param data
     */
    public int BPL(byte data) {
        if(getN() == 0) {
            int cycle =  2 + ((cpuMemory.getPrgPc() & 0xff00) == ((cpuMemory.getPrgPc() + data) & 0xff00) ? 1 : 2);
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
    public int CMP(byte data) {
        short cmpData = (short) ((REG_A & 0xff) - (data & 0xff));
        REG_S_N = (byte) ((cmpData >> 7) & 1);
        setZ((byte) (cmpData&0xFF));
        REG_S_C = (byte) ((cmpData & 0xff00) == 0 ? 1 : 0);
        return 2;
    }

    /**
     * Z=0切换
     * @param data
     * @return
     */
    public int BNE(byte data) {
        return branch(data,REG_S_Z == 0);
    }

    /**
     * C!=0 切换
     * @param data
     * @return
     */
    public int BCS(byte data) {
        return branch(data,REG_S_C != 0);
    }

    /**
     * X=X-1
     * @return
     */
    public int DEX() {
        REG_X = (byte) (REG_X - 1);
        setN(REG_X);
        setZ(REG_X);
        return 2;
    }

    /**
     *
     * @param low
     * @param high
     * @return
     */
    public int JSR(byte low, byte high) {
        int aShort = MemUtil.concatByte(low, high);
        short pc = (short) (cpuMemory.getPrgPc()-1);
        this.push16Stack(pc);
        cpuMemory.setPrgPc(aShort);
        return 6;
    }

    public int STA(byte addr) {
        cpuMemory.write(addr&0xFF, REG_A);
        return 3;
    }

    public int STX_ZERO(int addr) {
        cpuMemory.write(addr&0xFF, REG_X);
        return 3;
    }

    public int CPX(byte data) {
        int cmpData = (REG_X&0xff) - (data&0xff);
        setN((byte) (cmpData&0xff));
        setZ((byte) (cmpData&0xff));
        REG_S_C = (byte) ((cmpData & 0xff00) == 0 ? 1 : 0);
        return 2;
    }

    /**
     * A -> M
     * @param data
     * @return
     */
    public int STA_INDIRECT_Y( byte data) {
        int addr = MemUtil.concatByte(cpuMemory.read(data&0xFF), cpuMemory.read(((data&0xFF) + 1)))+ (REG_Y&0xFF);
        cpuMemory.write(addr, REG_A);
        return 6;
    }

    public int DEY() {
        REG_Y--;
        setN(REG_Y);
        setZ(REG_Y);
        return 2;
    }

    public int CPY(byte data) {
        byte regY = REG_Y;
        short cmpData = (short) ((regY&0xFF) - (data&0xFF));
        setN1((byte) ((cmpData >> 7) & 1));
        setZ1((byte) ((cmpData & 0xff) == 0 ? 1 : 0));
        setC1((byte) ((cmpData & 0xff00) == 0 ? 1 : 0));
        return 2;
    }

    private void setZ1(byte data) {
        REG_S_Z = data;
    }

    private void setN1(byte data) {
        REG_S_N = data;
    }

    /**
     * 从栈中读取PC,然后PC=PC+1
     * PC fromS, PC + 1 -> PC
     * @return
     */
    public int RTS() {
        int pc = this.pop16Stack();
        cpuMemory.setPrgPc(pc + 1);
        return 6;
    }

    /**
     *
     * @param low
     * @param high
     * @return
     */
    public int BIT_ABS(byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        byte data = cpuMemory.read(addr);
        setN(data);
        setV((byte) ((data>>6)&1));
        setZ((byte) (REG_A & data));
        return 4;
    }

    /**
     * A -> M
     * @param low
     * @param high
     * @return
     */
    public int STA_ABS_Y( byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_Y&0xFF);
        cpuMemory.write(addr, REG_A);
        return 5;
    }

    /**
     * 强制中断
     * @return
     */
    public int BRK() {
        setB((byte) 1);
        setI((byte) 1);
        short pc = (short) (cpuMemory.getPrgPc() + 2);
        this.push16Stack(pc);
        pushStack(REG_S_MERGE());
        int addr = MemUtil.concatByte(cpuMemory.read(0xFFFE), cpuMemory.read(0xFFFF));
        cpuMemory.setPrgPc(addr);
        return 7;
    }

    /**
     * 入栈
     * @param data
     */
    private void pushStack(byte data){
        byte sp = this.getReg_S();
        cpuMemory.write(0x0100 | (sp&0xFF),data);
        this.setReg_S((byte) (sp-1));
    }

    /**
     * 入栈16位
     */
    public void push16Stack(short data){
        pushStack((byte) ((data>>8)&0xFF));
        pushStack((byte) (data&0xFF));
    }

    /**
     * 出栈
     */
    public byte popStack(){
        int sp = this.getReg_S();
        this.setReg_S((byte) (sp+1));
        byte data = cpuMemory.read(0x0100 | ((sp + 1) & 0xFF));
        return data;
    }

    /**
     * 出栈
     */
    public int pop16Stack(){
        short pcLow8 = (short) (popStack()&0xFF);
        short pcLow16 = (short) (popStack()&0xFF);
        return  (pcLow16 << 8) | pcLow8;
    }


    public byte REG_S_MERGE() {
        return (byte) ((getN() << 7) | (getV() << 6) | 0x20 | (getB() << 4)
                | (getD() << 3) | (getI() << 2) | (getZ() << 1) | REG_S_C);
    }

    public void REG_S_SET(byte data) {
        REG_S_N = (byte) ((data >> 7) & 1);
        REG_S_V = (byte) ((data >> 6) & 1);
        REG_S_B = (byte) ((data >> 4) & 1);
        REG_S_D = (byte) ((data >> 3) & 1);
        REG_S_I = (byte) ((data >> 2) & 1);
        REG_S_Z = (byte) ((data >> 1) & 1);
        REG_S_C = (byte) (data & 1);
    }

    public int getI() {
        return REG_S_I;
    }

    public int getD() {
        return REG_S_D;
    }

    public int INY() {
        REG_Y = (byte) (REG_Y + 1);
        setN(REG_Y);
        setZ(REG_Y);
        return 2;
    }

    public int ORA(byte data) {
        REG_A |= data;
        setN(REG_A);
        setZ(REG_A);
        return 2;
    }

    public int AND(byte data) {
        REG_A &= data;
        setN(REG_A);
        setZ(REG_A);
        return 2;
    }

    public int TXA() {
        REG_A = REG_X;
        setN(REG_A);
        setZ(REG_A);
        return 2;
    }

    public int JMP_ABS( byte low, byte high) {
        int aShort = MemUtil.concatByte(low, high);
        cpuMemory.setPrgPc(aShort);
        return 3;
    }

    public int INC_ABS( byte low, byte high) {
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
    public void NMI() {
        this.push16Stack((short) cpuMemory.getPrgPc());
        this.pushStack(REG_S_MERGE());
        setI((byte) 1);
        int high = (cpuMemory.read(0xFFFA) & 0xff) | ((cpuMemory.read(0xFFFB) & 0xff) << 8);
        cpuMemory.setPrgPc(high);
    }

    public int LDY_ABS( byte low, byte high) {
        int readData = MemUtil.concatByte(low, high);
        byte data = cpuMemory.read(readData);
        LDY(data);
        return 4;
    }

    public int LDX_ABS( byte low, byte high) {
        int readData = MemUtil.concatByte(low, high);
        byte data = cpuMemory.read(readData);
        LDX(data);
        return 4;
    }

    public int LDA_INDIRECT_Y(byte data) {
        LDA(indirectY(data));
        return 5;
    }
    public int SED() {
        setD((byte) 1);
        return 2;
    }

    public int LDX_ABS_Y( byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_Y&0xFF);
        REG_X = cpuMemory.read(addr);
        setN(REG_X);
        setZ(REG_X);
        return 4;
    }

    public int STA_ABS_X( byte low, byte high) {
        int addr = MemUtil.concatByte(low, high)+ (REG_X&0xFF);
        cpuMemory.write(addr, REG_A);
        return 5;
    }

    public int LSR() {
        setC1((byte) (REG_A & 1));
        REG_A = (byte) ((REG_A & 0xff) >> 1);
        setN(REG_A);
        setZ(REG_A);
        return 2;
    }

    public int TAX() {
        REG_X = REG_A;
        setN(REG_X);
        setZ(REG_X);
        return 2;
    }

    public int PHA() {
        this.pushStack(REG_A);
        return 3;
    }

    public int ORA_ZERO( byte data) {
        REG_A |= cpuMemory.read(data&0xFF);
        setN(REG_A);
        setZ(REG_A);
        return 3;
    }

    public int PLA() {
        REG_A = this.popStack();
        setN(REG_A);
        setZ(REG_A);
        return 4;
    }

    public int ROL_A() {
        byte regA = REG_A;
        REG_A = (byte) ((regA << 1) | REG_S_C);
        setC1((byte)((regA >> 7) & 1));
        setN(REG_A);
        setZ(REG_A);
        return 2;
    }

    public int AND_ABS( byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        REG_A &= cpuMemory.read(addr);
        setN(REG_A);
        setZ(REG_A);
        return 4;
    }

    public int BEQ( byte data) {
        if (getZ() == 1) {
            int clk = 2 + ((cpuMemory.getPrgPc() & 0xff00) == ((cpuMemory.getPrgPc() + data) & 0xff00) ? 1 : 2);
            cpuMemory.setPrgPc(cpuMemory.getPrgPc() + data);
            return clk;
        }
        return 2;
    }

    public int INX() {
        REG_X = (byte) (REG_X + 1);
        setN(REG_X);
        setZ(REG_X);
        return 2;
    }

    public int SEC() {
        setC1((byte) 1);
        return 2;
    }

    public int SBC_ABS_Y( byte low, byte high) {
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

    public int BCC( byte data) {
        return branch(data,REG_S_C == 0);
    }

    public int EOR_ZERO( byte addr) {
        EOR_A(cpuMemory.read(addr&0xFF));
        return 3;
    }

    public int CLC() {
        setC1((byte) 0);
        return 2;
    }

    public int ROR_ABS_X( byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_X & 0xFF);
        byte read = cpuMemory.read(addr);
        byte read2 = (byte) (((read & 0xff) >> 1) | (REG_S_C << 7));
        setC1((byte) (read&1));
        setN(read2);
        setZ(read2);
        cpuMemory.write(addr,read2);
        return 7;
    }

    public int RTI() {
        REG_S_SET(this.popStack());
        cpuMemory.setPrgPc(this.pop16Stack());
        return 6;
    }

    public int DEC_ABS( byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        byte read = (byte) (cpuMemory.read(addr)-1);
        cpuMemory.write(addr, read);
        setN(read);
        setZ(read);
        return 6;
    }

    public int INC_ZERO( byte addr) {
        byte data = (byte) (cpuMemory.read(addr&0xFF)+1);
        cpuMemory.write(addr&0xFF,data);
        setN(data);
        setZ(data);
        return 5;
    }

    public int ASL_A() {
        setC1((byte) ((REG_A >> 7) & 1));
        REG_A <<= 1;
        setN(REG_A);
        setZ(REG_A);
        return 2;
    }

    public int TAY() {
        REG_Y = REG_A;
        setN(REG_Y);
        setZ(REG_Y);
        return 2;
    }

    public int JMP_INDIRECT( byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        int pc = MemUtil.concatByte(cpuMemory.read(addr), cpuMemory.read(addr + 1));
        cpuMemory.setPrgPc(pc);
        return 5;
    }

    public int LDA_ABS_Y( byte low, byte high) {
        int aShort = MemUtil.concatByte(low, high) + (REG_Y & 0xff);
        LDA(cpuMemory.read(aShort));
        return 4;
    }

    public int ADC_ABS( byte low, byte high) {
        byte data = cpuMemory.read(MemUtil.concatByte(low, high));
        ADC(data);
        return 4;
    }

    public int ADC(byte data) {
        int adcData = (REG_A & 0xff) + (data & 0xff) + (REG_S_C & 0xff);

        REG_S_C = (byte) (adcData >> 8);
        REG_S_Z = (byte) ((adcData & 0xff) == 0 ? 1 : 0);
        REG_S_N = (byte) ((adcData >> 7) & 1);
        REG_S_V = (byte) ((((REG_A ^ data) & 0x80) == 0 && ((REG_A ^ adcData) & 0x80) != 0) ? 1
                : 0);
        REG_A = (byte) (adcData & 0xff);
        return 2;
    }

    public int STY_ABS( byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        cpuMemory.write(addr,REG_Y);
        return 4;
    }

    public int LDA_ZERO( byte addr) {
        LDA(cpuMemory.read(addr&0xFF));
        return 3;
    }

    public int DEC_ZERO(int addr) {
        byte read = (byte) (cpuMemory.read(addr & 0xFF) - 1);
        cpuMemory.write(addr & 0xFF,read);
        setN(read);
        setZ(read);
        return 5;
    }

    public int TYA() {
        REG_A = REG_Y;
        setN(REG_A);
        setZ(REG_A);
        return 2;
    }

    public int ADC_ZERO( byte addr) {
        byte data = cpuMemory.read(addr&0xFF);
        ADC(data);
        return 3;
    }

    public int LDX_ZERO(int addr) {
        LDX(cpuMemory.read(addr&0xFF));
        return 3;
    }

    public int STX_ABS( byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        cpuMemory.write(addr,REG_X);
        return 4;
    }

    public int BMI( byte data) {
        if(REG_S_N == 1) {
            int clk = 2 + ((cpuMemory.getPrgPc() & 0xff00) == ((cpuMemory.getPrgPc() + data) & 0xff00) ? 1 : 2);
            cpuMemory.setPrgPc(cpuMemory.getPrgPc() + data);
            return clk;
        }
        return 2;
    }

    public int ADC_ABS_Y( byte low, byte high) {
        int aShort = MemUtil.concatByte(low, high) + (REG_Y & 0xff);
        ADC(cpuMemory.read(aShort));
        return 4;
    }

    public int SBC(byte data) {
        int sbcData = (REG_A & 0xff) - (data & 0xff) - (REG_S_C != 0 ? 0 : 1);
        REG_S_C = (byte) ((sbcData & 0xff00) == 0 ? 1 : 0);
        setN((byte) (sbcData&0xFF));
        setZ((byte) (sbcData&0xFF));
        REG_S_V = (byte) ((((REG_A ^ data) & 0x80) != 0) && (((REG_A ^ sbcData) & 0x80) != 0) ? 1 : 0);
        REG_A = (byte) sbcData;
        return 2;
    }

    public int STY_ZERO( byte addr) {
        cpuMemory.write(addr&0xFF,REG_Y);
        return 3;
    }

    public int BIT_ZERO( byte addr) {
        byte data = cpuMemory.read(addr&0xFF);
        setN(data);
        setZ((byte) (REG_A & data));
        REG_S_V = (byte) ((data >> 6) & 1);
        return 3;
    }

    public int LDY_ZERO( byte addr) {
        LDY(cpuMemory.read(addr&0xFF));
        return 3;
    }

    public int CMP_ABS( byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        CMP(cpuMemory.read(addr));
        return 4;
    }

    public int CMP_ABS_Y( byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_Y & 0xff);
        CMP(cpuMemory.read(addr));
        return 4;
    }

    public int EOR_A(byte data) {
        REG_A ^= data;
        setN(REG_A);
        setZ(REG_A);
        return 2;
    }

    public int ROL_ZERO(int addr) {
        byte data = cpuMemory.read(addr);
        byte rol = (byte) ((data << 1) | REG_S_C);
        setC1((byte)((data >> 7) & 1));
        setN(rol);
        setZ(rol);
        cpuMemory.write(addr,rol);
        return 5;
    }

    public int LSR_ZERO(int addr) {
        byte data = cpuMemory.read(addr);
        setC1((byte) (data&1));
        byte lsr = (byte) ((data & 0xff) >> 1);
        setN(lsr);
        setZ(lsr);
        cpuMemory.write(addr,lsr);
        return 5;
    }

    public int DEC_ABS_X( byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_X & 0xff);
        byte data = (byte) (cpuMemory.read(addr) - 1);
        cpuMemory.write(addr,data);
        setN(data);
        setZ(data);
        return 7;
    }

    public int LSR_ABS( byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        byte data = cpuMemory.read(addr);
        setC1((byte) (data&1));
        byte lsr = (byte) ((data & 0xff) >> 1);
        setN(lsr);
        setZ(lsr);
        cpuMemory.write(addr,lsr);
        return 6;
    }

    public int ROR_A() {
        byte read2 = (byte) (((REG_A & 0xff) >> 1) | (REG_S_C << 7));
        setC1((byte) (REG_A&1));
        setN(read2);
        setZ(read2);
        REG_A = read2;
        return 2;
    }

    public int ROL_ABS( byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        byte data = cpuMemory.read(addr);
        byte rol = (byte) ((data << 1) | REG_S_C);
        setC1((byte)((data >> 7) & 1));
        setN(rol);
        setZ(rol);
        cpuMemory.write(addr,rol);
        return 6;
    }

    public int CMP_ZERO( byte addr) {
        byte data = cpuMemory.read(addr&0xFF);
        CMP(data);
        return 3;
    }

    public int LDY_ABS_X( byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_X & 0xff);
        byte data = cpuMemory.read(addr);
        LDY(data);
        return 4;
    }

    public int STA_ZERO_X(byte addr) {
        int addr2 = zero(addr,REG_X);
        STA((byte) addr2);
        return 4;
    }

    public int CMP_ABS_X( byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_X & 0xff);
        CMP(cpuMemory.read(addr));
        return 4;
    }

    public int PHP() {
        this.pushStack(this.REG_S_MERGE());
        return 3;
    }

    public int CPX_ZERO( byte addr) {
        CPX(cpuMemory.read(addr&0xFF));
        return 3;
    }

    public int ROR_ZERO(int addr) {
        byte read = cpuMemory.read(addr);
        byte read2 = (byte) (((read & 0xff) >> 1) | (REG_S_C << 7));
        setC1((byte) (read&1));
        setN(read2);
        setZ(read2);
        cpuMemory.write(addr,read2);
        return 5;
    }

    public int LDA_ZERO_X(byte addr) {
        int data = zero(addr,REG_X);
        LDA(cpuMemory.read(data&0xFF));
        return 4;
    }

    public int AND_ZERO(byte addr) {
        byte data = cpuMemory.read(addr&0xFF);
        AND(data);
        return 3;
    }

    public int PLP() {
        this.REG_S_SET(this.popStack());
        return 4;
    }

    public int ASL_ZERO(int addr) {
        byte data = cpuMemory.read(addr);
        this.setC1((byte) ((data >> 7) & 1));
        byte data1= (byte) (data << 1);
        this.setN(data1);
        this.setZ(data1);
        cpuMemory.write(addr, data1);
        return 5;
    }

    public int AND_INDIRECT_Y( byte data) {
        AND(indirectY(data));
        return 5;
    }

    public int SBC_ZERO( byte addr) {
        byte data = cpuMemory.read(addr&0xFF);
        SBC(data);
        return 3;
    }

    public int LDY_ZERO_X( byte addr) {
        int data = zero(addr,REG_X)&0xff;
        LDY(cpuMemory.read(data));
        return 4;
    }

    public int NOP() {
        return 2;
    }

    public int EOR_ABS( byte low, byte high) {
        int data = MemUtil.concatByte(low, high);
        EOR_A(cpuMemory.read(data));
        return 4;
    }

    public int TSX() {
        REG_X = REG_SP;
        this.setN(REG_X);
        this.setZ(REG_X);
        return 2;
    }

    public int ASL_ABS(byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        byte data = cpuMemory.read(addr);
        this.setC1((byte) ((data >> 7) & 1));
        byte data1= (byte) (data << 1);
        this.setN(data1);
        this.setZ(data1);
        cpuMemory.write(addr, data1);
        return 6;
    }

    public int SBC_ABS( byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        byte data = cpuMemory.read(addr);
        SBC(data);
        return 4;
    }

    public int EOR_INDIRECT_Y( byte data) {
        EOR_A(indirectY(data));
        return 5;
    }

    public int INC_ABS_X( byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_X & 0xff);
        byte data = (byte) (cpuMemory.read(addr) + 1);
        cpuMemory.write(addr, data);
        setN(data);
        setZ(data);
        return 7;
    }

    public int EOR_ZERO_X(byte addr) {
        int data = zero(addr,REG_X);
        EOR_A(cpuMemory.read(data));
        return 4;
    }

    public int AND_ZERO_X(byte addr) {
        int data = zero(addr,REG_X);
        AND(cpuMemory.read(data));
        return 4;
    }

    public int STY_ZERO_X(byte addr) {
        int data = zero(addr,REG_X);
        cpuMemory.write(data,REG_Y);
        return 4;
    }

    public int ADC_ZERO_X(byte data) {
        int addr = zero(data,REG_X);
        ADC(cpuMemory.read(addr));
        return 4;
    }

    public int INC_ZERO_X(byte data) {
        int addr = zero(data,REG_X);
        int incData = cpuMemory.read(addr) + 1;
        cpuMemory.write(addr, (byte) incData);
        setN((byte) incData);
        setZ((byte) incData);
        return 6;
    }

    public int ORA_ABS(byte low, byte high) {
        int addr = MemUtil.concatByte(low, high);
        ORA(cpuMemory.read(addr));
        return 4;
    }

    public int CMP_ZERO_X(byte data) {
        int addr = zero(data,REG_X);
        CMP(cpuMemory.read(addr));
        return 4;
    }

    public int SBC_ZERO_X(byte data) {
        int addr = zero(data,REG_X);
        byte read = cpuMemory.read(addr);
        SBC(read);
        return 4;
    }

    public int AND_ABS_Y(byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_Y & 0xff);
        AND(cpuMemory.read(addr));
        return 4;
    }

    public int AND_ABS_X(byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_X & 0xff);
        AND(cpuMemory.read(addr));
        return 4;
    }

    public int ADC_ABS_X(byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_X&0xFF);
        ADC(cpuMemory.read(addr));
        return 4;
    }

    public int DEC_ZERO_X(byte data) {
        int addr = zero(data,REG_X);
        DEC_ZERO(addr);
        return 6;
    }

    public int ORA_ZERO_X(byte data) {
        int addr = zero(data,REG_X);
        ORA(cpuMemory.read(addr));
        return 4;
    }

    public int BVC(byte data) {
        return branch(data,REG_S_V == 0);
    }

    public int SBC_ABS_X(byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + REG_X;
        SBC(cpuMemory.read(addr));
        return 4;
    }

    public int CPY_ZERO(byte addr) {
        byte data = cpuMemory.read(addr&0xFF);
        CPY(data);
        return 3;
    }

    public int ORA_INDIRECT_Y(byte data) {
        ORA(indirectY(data));
        return 5;
    }

    public int ORA_ABS_Y(byte low, byte high) {
        int addr = MemUtil.concatByte(low,high) + (REG_Y&0xFF);
        byte read = cpuMemory.read(addr);
        ORA(read);
        return 4;
    }

    public int ADC_INDIRECT_Y(byte data) {
        ADC(indirectY(data));
        return 5;
    }

    public int ORA_ABS_X(byte low, byte high) {
        int addr = MemUtil.concatByte(low,high) + (REG_X&0xFF);
        byte read = cpuMemory.read(addr);
        ORA(read);
        return 4;
    }

    public int CMP_INDIRECT_Y(byte data) {
        CMP(indirectY(data));
        return 5;
    }

    public int SBC_INDIRECT_Y(byte data) {
        SBC(indirectY(data));
        return 5;
    }

    public int LDA_INDIRECT_X(byte data) {
        int addr = indirectX(data);
        LDA(cpuMemory.read(addr));
        return 6;
    }

    public int STA_INDIRECT_X(byte data) {
        int addr = indirectX(data);
        STA(cpuMemory.read(addr));
        return 6;
    }

    public int CPY_ABS(byte low, byte high) {
        CPY(cpuMemory.read(MemUtil.concatByte(low,high)));
        return 4;
    }

    public int ROR_ABS(byte low, byte high) {
        ROR_ZERO(MemUtil.concatByte(low,high));
        return 6;
    }

    public int BVS(byte data) {
        return branch(data,REG_S_V==1);
    }

    public int ROL_ABS_X(byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_X & 0xff);
        ROL_ZERO(addr);
        return 7;
    }

    public int ASL_ABS_X(byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_X & 0xff);
        ASL_ZERO(addr);
        return 7;
    }

    public int EOR_ABS_X(byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_X & 0xff);
        EOR_A(cpuMemory.read(addr));
        return 4;
    }

    public int CPX_ABS(byte low, byte high) {
        CPX(cpuMemory.read(MemUtil.concatByte(low,high)));
        return 4;
    }

    public int ROR_ZERO_X(byte data) {
        ROR_ZERO(zero(data,REG_X));
        return 6;
    }

    public int LSR_ZERO_X(byte data) {
        LSR_ZERO(zero(data,REG_X));
        return 6;
    }

    public int ROL_ZERO_X(byte data) {
        ROL_ZERO(zero(data,REG_X));
        return 6;
    }

    public int LDX_ZERO_Y(byte data) {
        LDX_ZERO(zero(data,REG_Y));
        return 4;
    }

    public int STX_ZERO_Y(byte data) {
        STX_ZERO(zero(data,REG_Y));
        return 4;
    }

    public int CLI() {
        setI((byte) 0);
        return 2;
    }

    public int LSR_ABS_X(byte low, byte high) {
        int addr = MemUtil.concatByte(low, high) + (REG_X & 0xff);
        LSR_ZERO(addr);
        return 7;
    }
}
