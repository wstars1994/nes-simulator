package com.iwstars.mcnes.core.cpu;

import java.util.Iterator;

/**
 * @description: 模拟6502cpu
 * @author: WStars
 * @date: 2020-04-18 20:12
 */
public class Cpu6502{

    /**
     * CPU内存数据
     */
    private CpuMemory cpuMemory = new CpuMemory();

    public Cpu6502(byte[] prgData){
        cpuMemory.write(0x8000,prgData);
    }

    public void go(){
        Iterator<Byte> iterator = cpuMemory.iteratorPrgData();
        while (iterator.hasNext()) {
            byte insCode = iterator.next();
            //执行程序(超级玛丽的执行顺序)
            switch(insCode&0xff) {
                //SEI 禁止中断
                case 0x78:
                    CpuReg.SEI();
                    break;
                //CLD 清除十进制模式状态标记
                case 0xD8:
                    CpuReg.CLD();
                    break;
                //LDA 将数据存入累加器A
                case 0xA9:
                    CpuReg.LDA(iterator.next());
                    break;
                //STA 将累加器A的数据存入CPU内存
                case 0x8D:
                    CpuReg.STA_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //LDX 将数据存入X索引寄存器
                case 0xA2:
                    CpuReg.LDX(iterator.next());
                    break;
                //TXS 将X索引寄存器的数据存入栈指针SP
                case 0x9A:
                    CpuReg.TXS();
                    break;
                //LDA Absolute
                case 0xAD:
                    CpuReg.LDA_ABS(iterator.next(),iterator.next());
                    break;
                //BPL
                case 0x10:
                    iterator.next();
//                    CpuReg.BPL();
                    break;
                //LDY 将数据存入Y索引寄存器
                case 0xA0:
                    CpuReg.LDY(iterator.next());
                    break;
                //LDA Absolute X
                case 0xBD:
                    CpuReg.LDA_ABS_X(iterator.next(),iterator.next());
                    break;
                default:
                    System.out.println(insCode&0xff);
                    break;
            }
        }
    }
}
