package com.iwstars.mcnes.core.cpu;

import com.iwstars.mcnes.util.LogUtil;

import java.util.Iterator;

/**
 * @description: 模拟6502cpu
 * @author: WStars
 * @date: 2020-04-18 20:12
 */
public class Cpu6502{

    /**
     * 1.79 = CPU频率约1.79MHz
     * 60 = NTSC制式 262条扫描线
     * 计算出每条扫描数所需要的CPU周期
     * 1.79*Math.pow(10,6)/60/262 ≈ 113
     */
    private int cpuCycle = 114;

    private static int runCycleNum = 1;

    /**
     * CPU内存数据
     */
    private CpuMemory cpuMemory = new CpuMemory();


    public Cpu6502(byte[] prgData){
        cpuMemory.write(0x8000,prgData);
    }

    public void go(){
        this.runProgram();
        this.cpuCycle = 113;
    }
    /**
     * 运行程序
     */
    private void runProgram(){
        Iterator<Byte> iterator = cpuMemory.iteratorPrgData();
        while (iterator.hasNext()) {
            LogUtil.logLn("");
            int prgPc = cpuMemory.getPrgPc();
            byte insCode = iterator.next();
            LogUtil.logf("insNum=%06d cycle=%03d A=$%02X,X=$%02X,Y=$%02X,S=$%02X pc=$%02X P:%c%c%c%c%c%c%c ",
                    runCycleNum++,cpuCycle, CpuRegister.getReg_A()&0xFF, CpuRegister.getReg_X()&0xFF, CpuRegister.getReg_Y()&0xFF, CpuRegister.getReg_S()&0xFF,prgPc&0xFFFF
            ,CpuRegister.getN() != 0 ? 'N'
                            : 'n', CpuRegister.getV() != 0 ? 'V' : 'v', CpuRegister.getB() != 0 ? 'B'
                            : 'b', CpuRegister.getD() != 0 ? 'D' : 'd', CpuRegister.getI() != 0 ? 'I'
                            : 'i', CpuRegister.getZ() != 0 ? 'Z' : 'z', CpuRegister.getC() != 0 ? 'C'
                            : 'c'
            );
            //执行程序(超级玛丽的执行顺序)
            switch(insCode&0xff) {
                //SEI 禁止中断
                case 0x78:
                    LogUtil.log("SEI");
                    cpuCycle-= CpuRegister.SEI();
                    break;
                //CLD 清除十进制模式状态标记
                case 0xD8:
                    LogUtil.log("CLD");
                    cpuCycle-= CpuRegister.CLD();
                    break;
                //LDA 将数据存入累加器A
                case 0xA9:
                    LogUtil.log("LDA");
                    cpuCycle-= CpuRegister.LDA(iterator.next());
                    break;
                //STA 将累加器A的数据存入CPU内存
                case 0x8D:
                    LogUtil.log("STA_ABS");
                    cpuCycle-= CpuRegister.STA_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //LDX 将数据存入X索引寄存器
                case 0xA2:
                    LogUtil.log("LDX");
                    cpuCycle-= CpuRegister.LDX(iterator.next());
                    break;
                //TXS 将X索引寄存器的数据存入栈指针SP
                case 0x9A:
                    LogUtil.log("TXS");
                    cpuCycle-= CpuRegister.TXS();
                    break;
                //LDA Absolute
                case 0xAD:
                    LogUtil.log("LDA_ABS");
                    cpuCycle-= CpuRegister.LDA_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //BPL
                case 0x10:
                    LogUtil.log("BPL");
                    cpuCycle-= CpuRegister.BPL(cpuMemory,iterator.next());
                    break;
                //LDY 将数据存入Y索引寄存器
                case 0xA0:
                    LogUtil.log("LDY");
                    cpuCycle-= CpuRegister.LDY(iterator.next());
                    break;
                //LDA Absolute X
                case 0xBD:
                    LogUtil.log("LDA_ABS_X");
                    cpuCycle-= CpuRegister.LDA_ABS_X(cpuMemory,iterator.next(),iterator.next());
                    break;
                //CMP
                case 0xC9:
                    LogUtil.log("CMP");
                    cpuCycle-= CpuRegister.CMP(iterator.next());
                    break;
                //BCS
                case 0xB0:
                    LogUtil.log("BCS");
                    cpuCycle-= CpuRegister.BCS(cpuMemory,iterator.next());
                    break;
                //DEX
                case 0xCA:
                    LogUtil.log("DEX");
                    cpuCycle-= CpuRegister.DEX();
                    break;
                //BNE
                case 0xD0:
                    LogUtil.log("BNE");
                    cpuCycle-= CpuRegister.BNE(cpuMemory,iterator.next());
                    break;
                //JSR
                case 0x20:
                    LogUtil.log("JSR");
                    cpuCycle-= CpuRegister.JSR(cpuMemory,iterator.next(),iterator.next());
                    break;
                //STA 将累加器A的数据存入CPU内存
                case 0x85:
                    LogUtil.log("STA_ZERO");
                    cpuCycle-= CpuRegister.STA_ZERO(cpuMemory,iterator.next());
                    break;
                //STX 将寄存器X的数据存入CPU内存
                case 0x86:
                    LogUtil.log("STX_ZERO");
                    cpuCycle-= CpuRegister.STX_ZERO(cpuMemory,iterator.next());
                    break;
                //CPX
                case 0xE0:
                    LogUtil.log("CPX");
                    cpuCycle-= CpuRegister.CPX(iterator.next());
                    break;
                //STA_INDIRECT_Y
                case 0x91:
                    LogUtil.log("STA_INDIRECT_Y");
                    cpuCycle-= CpuRegister.STA_INDIRECT_Y(cpuMemory,iterator.next());
                    break;
                //DEY
                case 0x88:
                    LogUtil.log("DEY");
                    cpuCycle-= CpuRegister.DEY();
                    break;
                //CPY
                case 0xC0:
                    LogUtil.log("CPY");
                    cpuCycle-= CpuRegister.CPY(iterator.next());
                    break;
                //RTS
                case 0x60:
                    LogUtil.log("RTS");
                    cpuCycle-= CpuRegister.RTS(cpuMemory);
                    break;
                //BRK
                case 0x00:
                    LogUtil.log("BRK");
                    cpuCycle-= CpuRegister.BRK(cpuMemory);
                    break;
                //SED
                case 0xF8:
                    LogUtil.log("SED");
                    cpuCycle-= CpuRegister.SED();
                    break;
                //BIT
                case 0x2C:
                    LogUtil.log("BIT_ABS");
                    cpuCycle-= CpuRegister.BIT_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //BIT
                case 0x99:
                    LogUtil.log("STA_ABS_Y");
                    cpuCycle-= CpuRegister.STA_ABS_Y(cpuMemory,iterator.next(),iterator.next());
                    break;
                //INY
                case 0xC8:
                    LogUtil.log("INY");
                    cpuCycle-= CpuRegister.INY();
                    break;
                //ORA
                case 0x09:
                    LogUtil.log("ORA");
                    cpuCycle-= CpuRegister.ORA(iterator.next());
                    break;
                //AND
                case 0x29:
                    LogUtil.log("AND");
                    cpuCycle-= CpuRegister.AND(iterator.next());
                    break;
                //TXA
                case 0x8A:
                    LogUtil.log("TXA");
                    cpuCycle-= CpuRegister.TXA();
                    break;
                //JMP_ABS
                case 0x4C:
                    LogUtil.log("JMP_ABS");
                    cpuCycle-= CpuRegister.JMP_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //INC_ABS
                case 0xEE:
                    LogUtil.log("INC_ABS");
                    cpuCycle-= CpuRegister.INC_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //LDY_ABS
                case 0xAC:
                    LogUtil.log("LDY_ABS");
                    cpuCycle-= CpuRegister.LDY_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //LDX_ABS
                case 0xAE:
                    LogUtil.log("LDX_ABS");
                    cpuCycle-= CpuRegister.LDX_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //LDA_INDIRECT_Y
                case 0xB1:
                    LogUtil.log("LDA_INDIRECT_Y");
                    cpuCycle-= CpuRegister.LDA_INDIRECT_Y(cpuMemory,iterator.next());
                    break;
                //LDX_ABS_Y
                case 0xBE:
                    LogUtil.log("LDX_ABS_Y");
                    cpuCycle-= CpuRegister.LDX_ABS_Y(cpuMemory,iterator.next(),iterator.next());
                    break;
                //STA_ABS_X
                case 0x9D:
                    LogUtil.log("STA_ABS_X");
                    cpuCycle-= CpuRegister.STA_ABS_X(cpuMemory,iterator.next(),iterator.next());
                    break;
                //LSR
                case 0x4A:
                    LogUtil.log("LSR");
                    cpuCycle-= CpuRegister.LSR();
                    break;
                //TAX
                case 0xAA:
                    LogUtil.log("TAX");
                    cpuCycle-= CpuRegister.TAX();
                    break;
                //PHA
                case 0x48:
                    LogUtil.log("PHA");
                    cpuCycle-= CpuRegister.PHA(cpuMemory);
                    break;
                //ORA_ZERO
                case 0x05:
                    LogUtil.log("ORA_ZERO");
                    cpuCycle-= CpuRegister.ORA_ZERO(cpuMemory,iterator.next());
                    break;
                //PLA
                case 0x68:
                    LogUtil.log("PLA");
                    cpuCycle-= CpuRegister.PLA(cpuMemory);
                    break;
                //ROL
                case 0x2A:
                    LogUtil.log("ROL");
                    cpuCycle-= CpuRegister.ROL();
                    break;
                //AND_ABS
                case 0x3D:
                    LogUtil.log("AND_ABS");
                    cpuCycle-= CpuRegister.AND_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //BEQ
                case 0xF0:
                    LogUtil.log("BEQ");
                    cpuCycle-= CpuRegister.BEQ(cpuMemory,iterator.next());
                    break;
                //INX
                case 0xE8:
                    LogUtil.log("INX");
                    cpuCycle-= CpuRegister.INX();
                    break;
                //INX
                case 0x38:
                    LogUtil.log("SEC");
                    cpuCycle-= CpuRegister.SEC();
                    break;
                //SBC
                case 0xF9:
                    LogUtil.log("SBC");
                    cpuCycle-= CpuRegister.SBC_ABS_Y(cpuMemory,iterator.next(),iterator.next());
                    break;
                //BCC
                case 0x90:
                    LogUtil.log("BCC");
                    cpuCycle-= CpuRegister.BCC(cpuMemory,iterator.next());
                    break;
                //EOR_ZERO
                case 0x45:
                    LogUtil.log("EOR_ZERO");
                    cpuCycle-= CpuRegister.EOR_ZERO(cpuMemory,iterator.next());
                    break;
                //CLC
                case 0x18:
                    LogUtil.log("CLC");
                    cpuCycle-= CpuRegister.CLC();
                    break;
                //ROR_ABS_X
                case 0x7E:
                    LogUtil.log("ROR_ABS_X");
                    cpuCycle-= CpuRegister.ROR_ABS_X(cpuMemory,iterator.next(),iterator.next());
                    break;
                //RTI
                case 0x40:
                    LogUtil.log("RTI");
                    cpuCycle-= CpuRegister.RTI(cpuMemory);
                    break;
                //DEC_ABS
                case 0xCE:
                    LogUtil.log("DEC_ABS");
                    cpuCycle-= CpuRegister.DEC_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //INC_ZERO
                case 0xE6:
                    LogUtil.log("INC_ZERO");
                    cpuCycle-= CpuRegister.INC_ZERO(cpuMemory,iterator.next());
                    break;
                //ASL
                case 0x0A:
                    LogUtil.log("ASL");
                    cpuCycle-= CpuRegister.ASL();
                    break;
                //TAY
                case 0xA8:
                    LogUtil.log("TAY");
                    cpuCycle-= CpuRegister.TAY();
                    break;
                //JMP_INDIRECT
                case 0x6C:
                    LogUtil.log("JMP_INDIRECT");
                    cpuCycle-= CpuRegister.JMP_INDIRECT(cpuMemory,iterator.next(),iterator.next());
                    break;
                //LDA_ABS_Y
                case 0xB9:
                    LogUtil.log("LDA_ABS_Y");
                    cpuCycle-= CpuRegister.LDA_ABS_Y(cpuMemory,iterator.next(),iterator.next());
                    break;
                //ADC_ABS
                case 0x6D:
                    LogUtil.log("ADC_ABS");
                    cpuCycle-= CpuRegister.ADC_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //ADC
                case 0x69:
                    LogUtil.log("ADC");
                    cpuCycle-= CpuRegister.ADC(iterator.next());
                    break;
                //STY_ABS
                case 0x8C:
                    LogUtil.log("STY_ABS");
                    cpuCycle-= CpuRegister.STY_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //LDA_ZERO
                case 0xA5:
                    LogUtil.log("LDA_ZERO");
                    cpuCycle-= CpuRegister.LDA_ZERO(cpuMemory,iterator.next());
                    break;
                //DEC_ZERO
                case 0xC6:
                    LogUtil.log("DEC_ZERO");
                    cpuCycle-= CpuRegister.DEC_ZERO(cpuMemory,iterator.next());
                    break;
                //TYA
                case 0x98:
                    LogUtil.log("TYA");
                    cpuCycle-= CpuRegister.TYA();
                    break;
                //ADC_ZERO
                case 0x65:
                    LogUtil.log("ADC_ZERO");
                    cpuCycle-= CpuRegister.ADC_ZERO(cpuMemory,iterator.next());
                    break;
                //LDX_ZERO
                case 0xA6:
                    LogUtil.log("LDX_ZERO");
                    cpuCycle-= CpuRegister.LDX_ZERO(cpuMemory,iterator.next());
                    break;
                //STX_ABS
                case 0x8E:
                    LogUtil.log("STX_ABS");
                    cpuCycle-= CpuRegister.STX_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //BMI
                case 0x30:
                    LogUtil.log("BMI");
                    cpuCycle-= CpuRegister.BMI(cpuMemory,iterator.next());
                    break;
                //ADC_ABS_Y
                case 0x79:
                    LogUtil.log("ADC_ABS_Y");
                    cpuCycle-= CpuRegister.ADC_ABS_Y(cpuMemory,iterator.next(),iterator.next());
                    break;
                //SBC
                case 0xE9:
                    LogUtil.log("SBC");
                    cpuCycle-= CpuRegister.SBC(iterator.next());
                    break;
                //STY_ZERO
                case 0x84:
                    LogUtil.log("STY_ZERO");
                    cpuCycle-= CpuRegister.STY_ZERO(cpuMemory,iterator.next());
                    break;
                //BIT_ZERO
                case 0x24:
                    LogUtil.log("BIT_ZERO");
                    cpuCycle-= CpuRegister.BIT_ZERO(cpuMemory,iterator.next());
                    break;
                //LDY_ZERO
                case 0xA4:
                    LogUtil.log("LDY_ZERO");
                    cpuCycle-= CpuRegister.LDY_ZERO(cpuMemory,iterator.next());
                    break;
                //CMP_ABS
                case 0xCD:
                    LogUtil.log("CMP_ABS");
                    cpuCycle-= CpuRegister.CMP_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //CMP_ABS_Y
                case 0xD9:
                    LogUtil.log("CMP_ABS_Y");
                    cpuCycle-= CpuRegister.CMP_ABS_Y(cpuMemory,iterator.next(),iterator.next());
                    break;
                //EOR
                case 0x49:
                    LogUtil.log("EOR");
                    cpuCycle-= CpuRegister.EOR_A(iterator.next());
                    break;
                //ROL_ZERO
                case 0x26:
                    LogUtil.log("ROL_ZERO");
                    cpuCycle-= CpuRegister.ROL_ZERO(cpuMemory,iterator.next());
                    break;
                //LSR_ZERO
                case 0x46:
                    LogUtil.log("LSR_ZERO");
                    cpuCycle-= CpuRegister.LSR_ZERO(cpuMemory,iterator.next());
                    break;
                //DEC_ABS_X
                case 0xDE:
                    LogUtil.log("DEC_ABS_X");
                    cpuCycle-= CpuRegister.DEC_ABS_X(cpuMemory,iterator.next(),iterator.next());
                    break;
                //LSR_ABS
                case 0x4E:
                    LogUtil.log("LSR_ABS");
                    cpuCycle-= CpuRegister.LSR_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //ROR_A
                case 0x6A:
                    LogUtil.log("ROR_A");
                    cpuCycle-= CpuRegister.ROR_A();
                    break;
                //ROL_ABS
                case 0x2E:
                    LogUtil.log("ROL_ABS");
                    cpuCycle-= CpuRegister.ROL_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //CMP_ZERO
                case 0xC5:
                    LogUtil.log("CMP_ZERO");
                    cpuCycle-= CpuRegister.CMP_ZERO(cpuMemory,iterator.next());
                    break;
                //LDY_ABS_X
                case 0xBC:
                    LogUtil.log("LDY_ABS_X");
                    cpuCycle-= CpuRegister.LDY_ABS_X(cpuMemory,iterator.next(),iterator.next());
                    break;
                default:
                    LogUtil.logf("%02X",insCode);
                    break;
            }
            if(cpuCycle <= 0) {
                break;
            }
        }
    }

    public CpuMemory getCpuMemory() {
        return cpuMemory;
    }
}
