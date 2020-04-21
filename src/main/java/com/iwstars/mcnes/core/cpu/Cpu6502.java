package com.iwstars.mcnes.core.cpu;

import java.util.Iterator;

/**
 * @description: 模拟6502cpu
 * @author: WStars
 * @date: 2020-04-16 15:12
 */
public class Cpu6502{

    /**
     * CPU内存数据
     */
    private CpuMemory cpuMemory = new CpuMemory();

    public Cpu6502(byte[] prgData){
        cpuMemory.setPrgData(prgData);
    }

    public void go(){
        Iterator<Byte> iterator = cpuMemory.iterator();
        while (iterator.hasNext()) {
            byte insCode = iterator.next();
            //执行程序(超级玛丽的执行顺序)
            switch(insCode&0xff) {
                //SEI
                case 0x78:
                    System.out.println("SEI");
                    break;
                //CLD
                case 0xD8:
                    System.out.println("CLD");
                    break;
                //LDA 将数据存入累加器A
                case 0xA9:
                    System.out.println("LDA");
                    CpuReg.LDA(iterator.next());
                    break;
                //STA 将累加器A的数据存入CPU内存
                case 0x8D:
                    System.out.println("STA_ABS");
                    CpuReg.STA_ABS(cpuMemory,iterator.next(),iterator.next());
                    break;
                //LDX 将数据存入X索引寄存器
                case 0xA2:
                    System.out.println("LDX");
                    CpuReg.LDX(iterator.next());
                    break;
                //TXS 将X索引寄存器的数据存入栈指针SP
                case 0x9A:
                    System.out.println("TXS");
                    CpuReg.TXS();
                    break;
                //LDA Absolute
                case 0xAD:
                    System.out.println("LDA_ABS");
                    CpuReg.LDA_ABS(iterator.next(),iterator.next());
                    break;
                //BPL
                case 0x10:
                    System.out.println("BPL");
                    CpuReg.LDX(iterator.next());
                    break;
                default:
                    System.out.println(insCode&0xff);
                    break;
            }
        }
    }
}
