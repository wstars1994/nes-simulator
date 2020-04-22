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
     * 1.79*Math.pow(10,6)/60/262 =
     */
    private int cpuCycle = 113;

    /**
     * CPU内存数据
     */
    private CpuMemory cpuMemory = new CpuMemory();

    public Cpu6502(byte[] prgData){
        cpuMemory.write(0x8000,prgData);
    }

    public void go(){
        for (int i = 0; i < 240; i++) {
            System.out.println("scan line :" + i);
            this.runProgram();
            this.resetCpuCycle();
        }
    }

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
                    CpuReg.SEI();
                    cpuCycle-=2;
                    break;
                //CLD 清除十进制模式状态标记
                case 0xD8:
                    CpuReg.CLD();
                    cpuCycle-=2;
                    break;
                //LDA 将数据存入累加器A
                case 0xA9:
                    CpuReg.LDA(iterator.next());
                    cpuCycle-=2;
                    break;
                //STA 将累加器A的数据存入CPU内存
                case 0x8D:
                    CpuReg.STA_ABS(cpuMemory,iterator.next(),iterator.next());
                    cpuCycle-=4;
                    break;
                //LDX 将数据存入X索引寄存器
                case 0xA2:
                    CpuReg.LDX(iterator.next());
                    cpuCycle-=2;
                    break;
                //TXS 将X索引寄存器的数据存入栈指针SP
                case 0x9A:
                    CpuReg.TXS();
                    cpuCycle-=2;
                    break;
                //LDA Absolute
                case 0xAD:
                    CpuReg.LDA_ABS(iterator.next(),iterator.next());
                    cpuCycle-=4;
                    break;
                //BPL
                case 0x10:
                    int jump = CpuReg.BPL(iterator.next());
                    cpuMemory.setPrgPc(cpuMemory.getPrgPc() + jump);
                    cpuCycle-=3;
                    break;
                //LDY 将数据存入Y索引寄存器
                case 0xA0:
                    CpuReg.LDY(iterator.next());
                    cpuCycle-=2;
                    break;
                //LDA Absolute X
                case 0xBD:
                    CpuReg.LDA_ABS_X(iterator.next(),iterator.next());
                    cpuCycle-=4;
                    break;
                default:
                    System.out.println(insCode&0xff);
                    break;
            }
        }

    }

    /**
     * 重置CPU扫描线周期
     */
    private void resetCpuCycle(){
        this.cpuCycle = 113;
    }
}
