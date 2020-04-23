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
     * 1.79*Math.pow(10,6)/60/262 = 113
     */
    private int cpuCycle = 113;
    /**
     * CPU内存数据
     */
    protected CpuMemory cpuMemory = new CpuMemory();

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
            if(cpuCycle <= 0) {
                break;
            };
            byte insCode = iterator.next();
            //执行程序(超级玛丽的执行顺序)
            switch(insCode&0xff) {
                //SEI 禁止中断
                case 0x78:
                    System.out.println("SEI");
                    cpuCycle-=CpuReg.SEI();
                    break;
                //CLD 清除十进制模式状态标记
                case 0xD8:
                    System.out.println("CLD");
                    cpuCycle-=CpuReg.CLD();
                    break;
                //LDA 将数据存入累加器A
                case 0xA9:
                    System.out.println("LDA");
                    cpuCycle-=CpuReg.LDA(iterator.next());
                    break;
                //STA 将累加器A的数据存入CPU内存
                case 0x8D:
                    System.out.println("STA_ABS");
                    cpuCycle-=CpuReg.STA_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //LDX 将数据存入X索引寄存器
                case 0xA2:
                    System.out.println("LDX");
                    cpuCycle-=CpuReg.LDX(iterator.next());
                    break;
                //TXS 将X索引寄存器的数据存入栈指针SP
                case 0x9A:
                    System.out.println("TXS");
                    cpuCycle-=CpuReg.TXS();
                    break;
                //LDA Absolute
                case 0xAD:
                    System.out.println("LDA_ABS");
                    cpuCycle-=CpuReg.LDA_ABS(iterator.next(),iterator.next());
                    break;
                //BPL
                case 0x10:
                    System.out.println("BPL");
                    cpuCycle-= CpuReg.BPL(cpuMemory,iterator.next());
                    break;
                //LDY 将数据存入Y索引寄存器
                case 0xA0:
                    System.out.println("LDY");
                    cpuCycle-=CpuReg.LDY(iterator.next());
                    break;
                //LDA Absolute X
                case 0xBD:
                    System.out.println("LDA_ABS_X");
                    cpuCycle-=CpuReg.LDA_ABS_X(iterator.next(),iterator.next());
                    break;
                default:
                    System.out.println(insCode&0xff);
                    break;
            }
        }
    }
}
