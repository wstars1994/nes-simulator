package com.iwstars.mcnes.core.cpu;

import com.iwstars.mcnes.util.LogUtil;
import lombok.Getter;

import java.util.Iterator;

/**
 * @description: 模拟6502cpu
 * @author: WStars
 * @date: 2020-04-18 20:12
 */
@Getter
public class Cpu6502{

    /**
     * 1.79 = CPU频率约1.79MHz
     * 60 = NTSC制式 262条扫描线
     * 计算出每条扫描数所需要的CPU周期
     * 1.79*Math.pow(10,6)/60/262 ≈ 113
     */
    private int cpuCycle = 113;

    private static int runCycleNum = 1;
    /**
     * CPU内存数据
     */
    private CpuMemory cpuMemory = new CpuMemory();
    /**
     * CPU寄存器
     */
    private CpuRegister cpuRegister;


    public Cpu6502(byte[] prgData){
        cpuMemory.write(0x8000,prgData);
        cpuRegister = new CpuRegister();
        cpuRegister.setCpuMemory(cpuMemory);
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

            LogUtil.logf("insNum=%06d cycle=%03d A=$%02X,X=$%02X,Y=$%02X,S=$%02X pc=$%02X P:%c%c%c%c%c%c%c ",
                    runCycleNum++,cpuCycle, cpuRegister.getReg_A()&0xFF, cpuRegister.getReg_X()&0xFF, cpuRegister.getReg_Y()&0xFF, cpuRegister.getReg_S()&0xFF,prgPc&0xFFFF
                    ,cpuRegister.getN() != 0 ? 'N'
                            : 'n', cpuRegister.getV() != 0 ? 'V' : 'v', cpuRegister.getB() != 0 ? 'B'
                            : 'b', cpuRegister.getD() != 0 ? 'D' : 'd', cpuRegister.getI() != 0 ? 'I'
                            : 'i', cpuRegister.getZ() != 0 ? 'Z' : 'z', cpuRegister.getC() != 0 ? 'C'
                            : 'c'
            );
            this.execInstrcution(iterator);
            if(cpuCycle < 0) {
                break;
            }
        }
    }

    /**
     * 执行指令
     * @param iterator
     */
    private void execInstrcution(Iterator<Byte> iterator) {
        byte insCode = iterator.next();
        //执行程序(超级玛丽的执行顺序)
        switch(insCode&0xff) {
            //SEI 禁止中断
            case 0x78:
                LogUtil.log("SEI");
                cpuCycle-= cpuRegister.SEI();
                break;
            //CLD 清除十进制模式状态标记
            case 0xD8:
                LogUtil.log("CLD");
                cpuCycle-= cpuRegister.CLD();
                break;
            //LDA 将数据存入累加器A
            case 0xA9:
                LogUtil.log("LDA");
                cpuCycle-= cpuRegister.LDA(iterator.next());
                break;
            //STA 将累加器A的数据存入CPU内存
            case 0x8D:
                LogUtil.log("STA_ABS");
                cpuCycle-= cpuRegister.STA_ABS(iterator.next(),iterator.next());
                break;
            //LDX 将数据存入X索引寄存器
            case 0xA2:
                LogUtil.log("LDX");
                cpuCycle-= cpuRegister.LDX(iterator.next());
                break;
            //TXS 将X索引寄存器的数据存入栈指针SP
            case 0x9A:
                LogUtil.log("TXS");
                cpuCycle-= cpuRegister.TXS();
                break;
            //LDA Absolute
            case 0xAD:
                LogUtil.log("LDA_ABS");
                cpuCycle-= cpuRegister.LDA_ABS(iterator.next(),iterator.next());
                break;
            //BPL
            case 0x10:
                LogUtil.log("BPL");
                cpuCycle-= cpuRegister.BPL(iterator.next());
                break;
            //LDY 将数据存入Y索引寄存器
            case 0xA0:
                LogUtil.log("LDY");
                cpuCycle-= cpuRegister.LDY(iterator.next());
                break;
            //LDA Absolute X
            case 0xBD:
                LogUtil.log("LDA_ABS_X");
                cpuCycle-= cpuRegister.LDA_ABS_X(iterator.next(),iterator.next());
                break;
            //CMP
            case 0xC9:
                LogUtil.log("CMP");
                cpuCycle-= cpuRegister.CMP(iterator.next());
                break;
            //BCS
            case 0xB0:
                LogUtil.log("BCS");
                cpuCycle-= cpuRegister.BCS(iterator.next());
                break;
            //DEX
            case 0xCA:
                LogUtil.log("DEX");
                cpuCycle-= cpuRegister.DEX();
                break;
            //BNE
            case 0xD0:
                LogUtil.log("BNE");
                cpuCycle-= cpuRegister.BNE(iterator.next());
                break;
            //JSR
            case 0x20:
                LogUtil.log("JSR");
                cpuCycle-= cpuRegister.JSR(iterator.next(),iterator.next());
                break;
            //STA 将累加器A的数据存入CPU内存
            case 0x85:
                LogUtil.log("STA_ZERO");
                cpuCycle-= cpuRegister.STA_ZERO(iterator.next());
                break;
            //STX 将寄存器X的数据存入CPU内存
            case 0x86:
                LogUtil.log("STX_ZERO");
                cpuCycle-= cpuRegister.STX_ZERO(iterator.next());
                break;
            //CPX
            case 0xE0:
                LogUtil.log("CPX");
                cpuCycle-= cpuRegister.CPX(iterator.next());
                break;
            //STA_INDIRECT_Y
            case 0x91:
                LogUtil.log("STA_INDIRECT_Y");
                cpuCycle-= cpuRegister.STA_INDIRECT_Y(iterator.next());
                break;
            //DEY
            case 0x88:
                LogUtil.log("DEY");
                cpuCycle-= cpuRegister.DEY();
                break;
            //CPY
            case 0xC0:
                LogUtil.log("CPY");
                cpuCycle-= cpuRegister.CPY(iterator.next());
                break;
            //RTS
            case 0x60:
                LogUtil.log("RTS");
                cpuCycle-= cpuRegister.RTS();
                break;
            //BRK
            case 0x00:
                LogUtil.log("BRK");
                cpuCycle-= cpuRegister.BRK();
                break;
            //SED
            case 0xF8:
                LogUtil.log("SED");
                cpuCycle-= cpuRegister.SED();
                break;
            //BIT
            case 0x2C:
                LogUtil.log("BIT_ABS");
                cpuCycle-= cpuRegister.BIT_ABS(iterator.next(),iterator.next());
                break;
            //BIT
            case 0x99:
                LogUtil.log("STA_ABS_Y");
                cpuCycle-= cpuRegister.STA_ABS_Y(iterator.next(),iterator.next());
                break;
            //INY
            case 0xC8:
                LogUtil.log("INY");
                cpuCycle-= cpuRegister.INY();
                break;
            //ORA
            case 0x09:
                LogUtil.log("ORA");
                cpuCycle-= cpuRegister.ORA(iterator.next());
                break;
            //AND
            case 0x29:
                LogUtil.log("AND");
                cpuCycle-= cpuRegister.AND(iterator.next());
                break;
            //TXA
            case 0x8A:
                LogUtil.log("TXA");
                cpuCycle-= cpuRegister.TXA();
                break;
            //JMP_ABS
            case 0x4C:
                LogUtil.log("JMP_ABS");
                cpuCycle-= cpuRegister.JMP_ABS(iterator.next(),iterator.next());
                break;
            //INC_ABS
            case 0xEE:
                LogUtil.log("INC_ABS");
                cpuCycle-= cpuRegister.INC_ABS(iterator.next(),iterator.next());
                break;
            //LDY_ABS
            case 0xAC:
                LogUtil.log("LDY_ABS");
                cpuCycle-= cpuRegister.LDY_ABS(iterator.next(),iterator.next());
                break;
            //LDX_ABS
            case 0xAE:
                LogUtil.log("LDX_ABS");
                cpuCycle-= cpuRegister.LDX_ABS(iterator.next(),iterator.next());
                break;
            //LDA_INDIRECT_Y
            case 0xB1:
                LogUtil.log("LDA_INDIRECT_Y");
                cpuCycle-= cpuRegister.LDA_INDIRECT_Y(iterator.next());
                break;
            //LDX_ABS_Y
            case 0xBE:
                LogUtil.log("LDX_ABS_Y");
                cpuCycle-= cpuRegister.LDX_ABS_Y(iterator.next(),iterator.next());
                break;
            //STA_ABS_X
            case 0x9D:
                LogUtil.log("STA_ABS_X");
                cpuCycle-= cpuRegister.STA_ABS_X(iterator.next(),iterator.next());
                break;
            //LSR
            case 0x4A:
                LogUtil.log("LSR");
                cpuCycle-= cpuRegister.LSR();
                break;
            //TAX
            case 0xAA:
                LogUtil.log("TAX");
                cpuCycle-= cpuRegister.TAX();
                break;
            //PHA
            case 0x48:
                LogUtil.log("PHA");
                cpuCycle-= cpuRegister.PHA();
                break;
            //ORA_ZERO
            case 0x05:
                LogUtil.log("ORA_ZERO");
                cpuCycle-= cpuRegister.ORA_ZERO(iterator.next());
                break;
            //PLA
            case 0x68:
                LogUtil.log("PLA");
                cpuCycle-= cpuRegister.PLA();
                break;
            //ROL
            case 0x2A:
                LogUtil.log("ROL");
                cpuCycle-= cpuRegister.ROL();
                break;
            //AND_ABS_X
            case 0x3D:
                LogUtil.log("AND_ABS_X");
                cpuCycle-= cpuRegister.AND_ABS_X(iterator.next(),iterator.next());
                break;
            //BEQ
            case 0xF0:
                LogUtil.log("BEQ");
                cpuCycle-= cpuRegister.BEQ(iterator.next());
                break;
            //INX
            case 0xE8:
                LogUtil.log("INX");
                cpuCycle-= cpuRegister.INX();
                break;
            //INX
            case 0x38:
                LogUtil.log("SEC");
                cpuCycle-= cpuRegister.SEC();
                break;
            //SBC
            case 0xF9:
                LogUtil.log("SBC");
                cpuCycle-= cpuRegister.SBC_ABS_Y(iterator.next(),iterator.next());
                break;
            //BCC
            case 0x90:
                LogUtil.log("BCC");
                cpuCycle-= cpuRegister.BCC(iterator.next());
                break;
            //EOR_ZERO
            case 0x45:
                LogUtil.log("EOR_ZERO");
                cpuCycle-= cpuRegister.EOR_ZERO(iterator.next());
                break;
            //CLC
            case 0x18:
                LogUtil.log("CLC");
                cpuCycle-= cpuRegister.CLC();
                break;
            //ROR_ABS_X
            case 0x7E:
                LogUtil.log("ROR_ABS_X");
                cpuCycle-= cpuRegister.ROR_ABS_X(iterator.next(),iterator.next());
                break;
            //RTI
            case 0x40:
                LogUtil.log("RTI");
                cpuCycle-= cpuRegister.RTI();
                break;
            //DEC_ABS
            case 0xCE:
                LogUtil.log("DEC_ABS");
                cpuCycle-= cpuRegister.DEC_ABS(iterator.next(),iterator.next());
                break;
            //INC_ZERO
            case 0xE6:
                LogUtil.log("INC_ZERO");
                cpuCycle-= cpuRegister.INC_ZERO(iterator.next());
                break;
            //ASL_A
            case 0x0A:
                LogUtil.log("ASL_A");
                cpuCycle-= cpuRegister.ASL_A();
                break;
            //TAY
            case 0xA8:
                LogUtil.log("TAY");
                cpuCycle-= cpuRegister.TAY();
                break;
            //JMP_INDIRECT
            case 0x6C:
                LogUtil.log("JMP_INDIRECT");
                cpuCycle-= cpuRegister.JMP_INDIRECT(iterator.next(),iterator.next());
                break;
            //LDA_ABS_Y
            case 0xB9:
                LogUtil.log("LDA_ABS_Y");
                cpuCycle-= cpuRegister.LDA_ABS_Y(iterator.next(),iterator.next());
                break;
            //ADC_ABS
            case 0x6D:
                LogUtil.log("ADC_ABS");
                cpuCycle-= cpuRegister.ADC_ABS(iterator.next(),iterator.next());
                break;
            //ADC
            case 0x69:
                LogUtil.log("ADC");
                cpuCycle-= cpuRegister.ADC(iterator.next());
                break;
            //STY_ABS
            case 0x8C:
                LogUtil.log("STY_ABS");
                cpuCycle-= cpuRegister.STY_ABS(iterator.next(),iterator.next());
                break;
            //LDA_ZERO
            case 0xA5:
                LogUtil.log("LDA_ZERO");
                cpuCycle-= cpuRegister.LDA_ZERO(iterator.next());
                break;
            //DEC_ZERO
            case 0xC6:
                LogUtil.log("DEC_ZERO");
                cpuCycle-= cpuRegister.DEC_ZERO(iterator.next());
                break;
            //TYA
            case 0x98:
                LogUtil.log("TYA");
                cpuCycle-= cpuRegister.TYA();
                break;
            //ADC_ZERO
            case 0x65:
                LogUtil.log("ADC_ZERO");
                cpuCycle-= cpuRegister.ADC_ZERO(iterator.next());
                break;
            //LDX_ZERO
            case 0xA6:
                LogUtil.log("LDX_ZERO");
                cpuCycle-= cpuRegister.LDX_ZERO(iterator.next());
                break;
            //STX_ABS
            case 0x8E:
                LogUtil.log("STX_ABS");
                cpuCycle-= cpuRegister.STX_ABS(iterator.next(),iterator.next());
                break;
            //BMI
            case 0x30:
                LogUtil.log("BMI");
                cpuCycle-= cpuRegister.BMI(iterator.next());
                break;
            //ADC_ABS_Y
            case 0x79:
                LogUtil.log("ADC_ABS_Y");
                cpuCycle-= cpuRegister.ADC_ABS_Y(iterator.next(),iterator.next());
                break;
            //SBC
            case 0xE9:
                LogUtil.log("SBC");
                cpuCycle-= cpuRegister.SBC(iterator.next());
                break;
            //STY_ZERO
            case 0x84:
                LogUtil.log("STY_ZERO");
                cpuCycle-= cpuRegister.STY_ZERO(iterator.next());
                break;
            //BIT_ZERO
            case 0x24:
                LogUtil.log("BIT_ZERO");
                cpuCycle-= cpuRegister.BIT_ZERO(iterator.next());
                break;
            //LDY_ZERO
            case 0xA4:
                LogUtil.log("LDY_ZERO");
                cpuCycle-= cpuRegister.LDY_ZERO(iterator.next());
                break;
            //CMP_ABS
            case 0xCD:
                LogUtil.log("CMP_ABS");
                cpuCycle-= cpuRegister.CMP_ABS(iterator.next(),iterator.next());
                break;
            //CMP_ABS_Y
            case 0xD9:
                LogUtil.log("CMP_ABS_Y");
                cpuCycle-= cpuRegister.CMP_ABS_Y(iterator.next(),iterator.next());
                break;
            //EOR
            case 0x49:
                LogUtil.log("EOR");
                cpuCycle-= cpuRegister.EOR_A(iterator.next());
                break;
            //ROL_ZERO
            case 0x26:
                LogUtil.log("ROL_ZERO");
                cpuCycle-= cpuRegister.ROL_ZERO(iterator.next());
                break;
            //LSR_ZERO
            case 0x46:
                LogUtil.log("LSR_ZERO");
                cpuCycle-= cpuRegister.LSR_ZERO(iterator.next());
                break;
            //DEC_ABS_X
            case 0xDE:
                LogUtil.log("DEC_ABS_X");
                cpuCycle-= cpuRegister.DEC_ABS_X(iterator.next(),iterator.next());
                break;
            //LSR_ABS
            case 0x4E:
                LogUtil.log("LSR_ABS");
                cpuCycle-= cpuRegister.LSR_ABS(iterator.next(),iterator.next());
                break;
            //ROR_A
            case 0x6A:
                LogUtil.log("ROR_A");
                cpuCycle-= cpuRegister.ROR_A();
                break;
            //ROL_ABS
            case 0x2E:
                LogUtil.log("ROL_ABS");
                cpuCycle-= cpuRegister.ROL_ABS(iterator.next(),iterator.next());
                break;
            //CMP_ZERO
            case 0xC5:
                LogUtil.log("CMP_ZERO");
                cpuCycle-= cpuRegister.CMP_ZERO(iterator.next());
                break;
            //LDY_ABS_X
            case 0xBC:
                LogUtil.log("LDY_ABS_X");
                cpuCycle-= cpuRegister.LDY_ABS_X(iterator.next(),iterator.next());
                break;
            //STA_ZERO_X
            case 0x95:
                LogUtil.log("STA_ZERO_X");
                cpuCycle-= cpuRegister.STA_ZERO_X(iterator.next());
                break;
            //CMP_ABS_X
            case 0xDD:
                LogUtil.log("CMP_ABS_X");
                cpuCycle-= cpuRegister.CMP_ABS_X(iterator.next(),iterator.next());
                break;
            //PHP
            case 0x08:
                LogUtil.log("PHP");
                cpuCycle-= cpuRegister.PHP();
                break;
            //CPX_ZERO
            case 0xE4:
                LogUtil.log("CPX_ZERO");
                cpuCycle-= cpuRegister.CPX_ZERO(iterator.next());
                break;
            //ROR_ZERO
            case 0x66:
                LogUtil.log("ROR_ZERO");
                cpuCycle-= cpuRegister.ROR_ZERO(iterator.next());
                break;
            //LDA_ZERO_X
            case 0xB5:
                LogUtil.log("LDA_ZERO_X");
                cpuCycle-= cpuRegister.LDA_ZERO_X(iterator.next());
                break;
            //AND_ZERO
            case 0x25:
                LogUtil.log("AND_ZERO");
                cpuCycle-= cpuRegister.AND_ZERO(iterator.next());
                break;
            //PLP
            case 0x28:
                LogUtil.log("PLP");
                cpuCycle-= cpuRegister.PLP();
                break;
            //ASL_ZERO
            case 0x06:
                LogUtil.log("ASL_ZERO");
                cpuCycle-= cpuRegister.ASL_ZERO(iterator.next());
                break;
            //AND_INDIRECT_Y
            case 0x31:
                LogUtil.log("AND_INDIRECT_Y");
                cpuCycle-= cpuRegister.AND_INDIRECT_Y(iterator.next());
                break;
            //SBC_ZERO
            case 0xE5:
                LogUtil.log("SBC_ZERO");
                cpuCycle-= cpuRegister.SBC_ZERO(iterator.next());
                break;
            //LDY_ZERO_X
            case 0xB4:
                LogUtil.log("LDY_ZERO_X");
                cpuCycle-= cpuRegister.LDY_ZERO_X(iterator.next());
                break;
            //NOP
            case 0xEA:
                LogUtil.log("NOP");
                cpuCycle-= cpuRegister.NOP();
                break;
            //EOR_ABS
            case 0x4D:
                LogUtil.log("EOR_ABS");
                cpuCycle-= cpuRegister.EOR_ABS(iterator.next(),iterator.next());
                break;
            //TSX
            case 0xBA:
                LogUtil.log("TSX");
                cpuCycle-= cpuRegister.TSX();
                break;
            //ASL_ABS
            case 0x0E:
                LogUtil.log("ASL_ABS");
                cpuCycle-= cpuRegister.ASL_ABS(iterator.next(),iterator.next());
                break;
            //SBC_ABS
            case 0xED:
                LogUtil.log("SBC_ABS");
                cpuCycle-= cpuRegister.SBC_ABS(iterator.next(),iterator.next());
                break;
//            //EOR_INDIRECT_Y
//            case 0x51:
//                LogUtil.log("EOR_INDIRECT_Y");
//                cpuCycle-= cpuRegister.EOR_INDIRECT_Y(iterator.next());
//                break;
//            //INC_ABS_X
//            case 0xFE:
//                LogUtil.log("INC_ABS_X");
//                cpuCycle-= cpuRegister.INC_ABS_X(iterator.next(),iterator.next());
//                break;
//            //EOR_ZERO_X
//            case 0x55:
//                LogUtil.log("EOR_ZERO_X");
//                cpuCycle-= cpuRegister.EOR_ZERO_X(iterator.next());
//                break;
//            //AND_ZERO_X
//            case 0x35:
//                LogUtil.log("AND_ZERO_X");
//                cpuCycle-= cpuRegister.AND_ZERO_X(iterator.next());
//                break;
//            //STY_ZERO_X
//            case 0x94:
//                LogUtil.log("STY_ZERO_X");
//                cpuCycle-= cpuRegister.STY_ZERO_X(iterator.next());
//                break;
            //ORA_ABS
            case 0x0D:
                LogUtil.log("ORA_ABS");
                cpuCycle-= cpuRegister.ORA_ABS(iterator.next(),iterator.next());
                break;
            //CMP_ZERO_X
            case 0xD5:
                LogUtil.log("CMP_ZERO_X");
                cpuCycle-= cpuRegister.CMP_ZERO_X(iterator.next());
                break;
            //SBC_ZERO_X
            case 0xF5:
                LogUtil.log("SBC_ZERO_X");
                cpuCycle-= cpuRegister.SBC_ZERO_X(iterator.next());
                break;
            //AND_ABS_Y
            case 0x39:
                LogUtil.log("AND_ABS_Y");
                cpuCycle-= cpuRegister.AND_ABS_Y(iterator.next(),iterator.next());
                break;
            //AND_ABS
            case 0x2D:
                LogUtil.log("AND_ABS");
                cpuCycle-= cpuRegister.AND_ABS(iterator.next(),iterator.next());
                break;
            //ADC_ABS_X
            case 0x7D:
                LogUtil.log("ADC_ABS_X");
                cpuCycle-= cpuRegister.ADC_ABS_X(iterator.next(),iterator.next());
                break;
            //ADC_ZERO_X
            case 0x75:
                LogUtil.log("ADC_ZERO_X");
                cpuCycle-= cpuRegister.ADC_ZERO_X(iterator.next());
                break;
            //INC_ZERO_X
            case 0xF6:
                LogUtil.log("INC_ZERO_X");
                cpuCycle-= cpuRegister.INC_ZERO_X(iterator.next());
                break;
            //DEC_ZERO_X
            case 0xD6:
                LogUtil.log("DEC_ZERO_X");
                cpuCycle-= cpuRegister.DEC_ZERO_X(iterator.next());
                break;
            default:
                System.out.printf("%02X",insCode);
                break;
        }
    }
}
