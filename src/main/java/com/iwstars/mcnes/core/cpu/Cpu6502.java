package com.iwstars.mcnes.core.cpu;

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
    private int cpuCycle = 113;

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
            System.out.println("");
            int prgPc = cpuMemory.getPrgPc();
            byte insCode = iterator.next();
            System.out.printf("insNum=%06d cycle=%03d A=$%02X,X=$%02X,Y=$%02X pc=$%02X ",runCycleNum++,cpuCycle, CpuRegister.getReg_A()&0xFF, CpuRegister.getReg_X()&0xFF, CpuRegister.getReg_Y()&0xFF,prgPc&0xFFFF);
            //执行程序(超级玛丽的执行顺序)
            switch(insCode&0xff) {
                //SEI 禁止中断
                case 0x78:
                    System.out.print("SEI");
                    cpuCycle-= CpuRegister.SEI();
                    break;
                //CLD 清除十进制模式状态标记
                case 0xD8:
                    System.out.print("CLD");
                    cpuCycle-= CpuRegister.CLD();
                    break;
                //LDA 将数据存入累加器A
                case 0xA9:
                    System.out.print("LDA");
                    cpuCycle-= CpuRegister.LDA(iterator.next());
                    break;
                //STA 将累加器A的数据存入CPU内存
                case 0x8D:
                    System.out.print("STA_ABS");
                    cpuCycle-= CpuRegister.STA_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //LDX 将数据存入X索引寄存器
                case 0xA2:
                    System.out.print("LDX");
                    cpuCycle-= CpuRegister.LDX(iterator.next());
                    break;
                //TXS 将X索引寄存器的数据存入栈指针SP
                case 0x9A:
                    System.out.print("TXS");
                    cpuCycle-= CpuRegister.TXS();
                    break;
                //LDA Absolute
                case 0xAD:
                    System.out.print("LDA_ABS");
                    cpuCycle-= CpuRegister.LDA_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //BPL
                case 0x10:
                    System.out.print("BPL");
                    cpuCycle-= CpuRegister.BPL(cpuMemory,iterator.next());
                    break;
                //LDY 将数据存入Y索引寄存器
                case 0xA0:
                    System.out.print("LDY");
                    cpuCycle-= CpuRegister.LDY(iterator.next());
                    break;
                //LDA Absolute X
                case 0xBD:
                    System.out.print("LDA_ABS_X");
                    cpuCycle-= CpuRegister.LDA_ABS_X(cpuMemory,iterator.next(),iterator.next());
                    break;
                //CMP
                case 0xC9:
                    System.out.print("CMP");
                    cpuCycle-= CpuRegister.CMP(iterator.next());
                    break;
                //BCS
                case 0xB0:
                    System.out.print("BCS");
                    cpuCycle-= CpuRegister.BCS(cpuMemory,iterator.next());
                    break;
                //DEX
                case 0xCA:
                    System.out.print("DEX");
                    cpuCycle-= CpuRegister.DEX();
                    break;
                //BNE
                case 0xD0:
                    System.out.print("BNE");
                    cpuCycle-= CpuRegister.BNE(cpuMemory,iterator.next());
                    break;
                //JSR
                case 0x20:
                    System.out.print("JSR");
                    cpuCycle-= CpuRegister.JSR(cpuMemory,iterator.next(),iterator.next());
                    break;
                //STA 将累加器A的数据存入CPU内存
                case 0x85:
                    System.out.print("STA_ZERO");
                    cpuCycle-= CpuRegister.STA_ZERO(cpuMemory,iterator.next());
                    break;
                //STX 将寄存器X的数据存入CPU内存
                case 0x86:
                    System.out.print("STX_ZERO");
                    cpuCycle-= CpuRegister.STX_ZERO(cpuMemory,iterator.next());
                    break;
                //CPX
                case 0xE0:
                    System.out.print("CPX");
                    cpuCycle-= CpuRegister.CPX(iterator.next());
                    break;
                //STA_INDIRECT_Y
                case 0x91:
                    System.out.print("STA_INDIRECT_Y");
                    cpuCycle-= CpuRegister.STA_INDIRECT_Y(cpuMemory,iterator.next());
                    break;
                //DEY
                case 0x88:
                    System.out.print("DEY");
                    cpuCycle-= CpuRegister.DEY();
                    break;
                //CPY
                case 0xC0:
                    System.out.print("CPY");
                    cpuCycle-= CpuRegister.CPY(iterator.next());
                    break;
                //RTS
                case 0x60:
                    System.out.print("RTS");
                    cpuCycle-= CpuRegister.RTS(cpuMemory);
                    break;
                //BRK
                case 0x00:
                    System.out.print("BRK");
                    cpuCycle-= CpuRegister.BRK(cpuMemory);
                    break;
                //SED
                case 0xF8:
                    System.out.print("SED");
                    cpuCycle-= CpuRegister.SED();
                    break;
                //BIT
                case 0x2C:
                    System.out.print("BIT_ABS");
                    cpuCycle-= CpuRegister.BIT_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //BIT
                case 0x99:
                    System.out.print("STA_ABS_Y");
                    cpuCycle-= CpuRegister.STA_ABS_Y(cpuMemory,iterator.next(),iterator.next());
                    break;
                //INY
                case 0xC8:
                    System.out.print("INY");
                    cpuCycle-= CpuRegister.INY();
                    break;
                //ORA
                case 0x09:
                    System.out.print("ORA");
                    cpuCycle-= CpuRegister.ORA(iterator.next());
                    break;
                //AND
                case 0x29:
                    System.out.print("AND");
                    cpuCycle-= CpuRegister.AND(iterator.next());
                    break;
                //TXA
                case 0x8A:
                    System.out.print("TXA");
                    cpuCycle-= CpuRegister.TXA();
                    break;
                //JMP_ABS
                case 0x4C:
                    System.out.print("JMP_ABS");
                    cpuCycle-= CpuRegister.JMP_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //INC_ABS
                case 0xEE:
                    System.out.print("INC_ABS");
                    cpuCycle-= CpuRegister.INC_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //LDY_ABS
                case 0xAC:
                    System.out.print("LDY_ABS");
                    cpuCycle-= CpuRegister.LDY_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //LDX_ABS
                case 0xAE:
                    System.out.print("LDX_ABS");
                    cpuCycle-= CpuRegister.LDX_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //LDA_INDIRECT_Y
                case 0xB1:
                    System.out.print("LDA_INDIRECT_Y");
                    cpuCycle-= CpuRegister.LDA_INDIRECT_Y(cpuMemory,iterator.next());
                    break;
                default:
                    System.out.print(insCode&0xff);
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
