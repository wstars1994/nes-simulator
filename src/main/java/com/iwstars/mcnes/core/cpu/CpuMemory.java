package com.iwstars.mcnes.core.cpu;

import com.iwstars.mcnes.core.DataBus;
import com.iwstars.mcnes.util.MemUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

/**
 * CPU数据
 * +---------+-------+-------+-----------------------+
 * | 地址    | 大小  | 标记  |         描述          |
 * +---------+-------+-------+-----------------------+
 * | $0000   | $800  |       | RAM                   |
 * | $0800   | $800  | M     | RAM                   |
 * | $1000   | $800  | M     | RAM                   |
 * | $1800   | $800  | M     | RAM                   |
 * | $2000   | 8     |       | Registers             |
 * | $2008   | $1FF8 |  R    | Registers             |
 * | $4000   | $20   |       | Registers             |
 * | $4020   | $1FDF |       | Expansion ROM         |
 * | $6000   | $2000 |       | SRAM                  |
 * | $8000   | $4000 |       | PRG-ROM               |
 * | $C000   | $4000 |       | PRG-ROM               |
 * +---------+-------+-------+-----------------------+
 * @author WStars
 * @date 2020/4/18 15:25
 */
@Setter
@Getter
public class CpuMemory {

    /**
     * PRG程序计数器指针
     * 2字节指针
     */
    private int prgPc = 0x8000;

    /**
     * cpu内存
     */
    private byte[] data = new byte[0xFFFF+1];

    /**
     * PRG数据迭代器
     * @return
     */
    public Iterator<Byte> iteratorPrgData() {
        return new Iterator<Byte>() {

            public boolean hasNext() {
                return prgPc < data.length;
            }

            public Byte next() {
                return data[prgPc++];
            }

            public void remove() {}
        };
    }

    /**
     * 写数据
     * @param addr
     * @param data
     */
    public void write(int addr,byte data){
        System.out.printf(" --> Write to memory:addr=$%02X(index=%d),val=%d", addr,addr,data);
        this.data[addr] = data;
        switch (addr) {
            case 0x2000:
                DataBus.p_2000 = MemUtil.toBits(data);
                break;
            case 0x2001:
                DataBus.p_2001 = MemUtil.toBits(data);
                break;
            case 0x2003:
                DataBus.p_2003 = MemUtil.toBits(data);
                break;
            case 0x2004:
                DataBus.p_2004 = MemUtil.toBits(data);
                break;
            case 0x2005:
                DataBus.p_2005 = MemUtil.toBits(data);
                break;
            case 0x2006:
                if(!DataBus.p_2006_flag) {
                    //第一次写将写入高8位;
                    DataBus.p_2006_data = (short) ((data&0xFF) << 8);
                }else {
                    //第二次写将写入低6位
                    DataBus.p_2006_data|=(data&0x3F);
                }
                DataBus.p_2006_flag = !DataBus.p_2006_flag;
                break;
            case 0x2007:
                DataBus.p_2007 = MemUtil.toBits(data);
                DataBus.writePpuNameTable(DataBus.p_2006_data,data);
                DataBus.p_2006_data+=(DataBus.p_2000[2]==0?1:32);
                break;
            //OAM DMA register (high byte)
            case 0x4014:
//                DataBus.pcr_2000 = MemUtil.toBits(CpuRegister.REG_A);
                System.out.print(" OAM DMA register (high byte)");
                break;
        }
    }

    /**
     * 写块数据
     * @param addr
     * @param prgData
     */
    public void write(int addr,byte[] prgData){
        System.arraycopy(prgData,0,data,addr,prgData.length);
    }

    /**
     * 入栈
     * @param data
     */
    public void pushStack(byte data){
        byte sp = CpuRegister.getReg_S();
        this.data[0x0100 + (sp&0xFF)] = data;
        CpuRegister.setReg_S((byte) (sp-1));
    }
    /**
     * 入栈16位
     */
    public void push16Stack(short data){
        pushStack((byte) (data&0xFF));
        pushStack((byte) ((data>>8)&0xFF));
    }

    /**
     * 出栈
     */
    public byte popStack(){
        int sp = CpuRegister.getReg_S();
        CpuRegister.setReg_S((byte) (sp+1));
        return this.data[0x0100 + ((sp + 1)&0xFF)];
    }

    /**
     * 出栈
     */
    public int pop16Stack(){
        byte pcLow16 =  popStack();
        byte pcLow8 = (byte) (popStack()&0xFF);
        return  (pcLow16 << 8) | pcLow8;
    }

    /**
     * 读数据
     * @param addr
     * @return
     */
    public byte read(int addr){
        if(addr == 0x2002 || addr == 0x2007) {
            System.out.printf(" read addr = %02X",addr);
            switch (addr) {
                //读PPUSTATUS状态寄存器
                case 0x2002:
                    //当CPU读取$2002后vblank标志设置为0
                    byte readData = MemUtil.bitsToByte(DataBus.p_2002);
                    DataBus.p_2002[7] = 0;
                    return readData;
                case 0x2007:
                    System.out.println("2007");
                    break;
            }
        }
        return this.data[addr];
    }
}
