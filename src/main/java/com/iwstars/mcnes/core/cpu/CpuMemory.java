package com.iwstars.mcnes.core.cpu;

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
     */
    private int prgPc = 0x8000;

    /**
     * 栈指针
     */
    private int sp = 0x0;

    /**
     * cpu内存
     */
    private byte[] data = new byte[0xFFFF+1];
    /**
     * 栈
     */
    private int[] stack = new int[0x800];

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
        System.out.printf(" -->Write $%02X", addr);
        this.data[addr] = data;
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
     * 读数据
     * @param addr
     * @return
     */
    public byte read(int addr){
        return this.data[addr];
    }

    /**
     * 入栈
     * @param data
     */
    public void pushStack(int data){
        this.stack[sp++] = data;
    }

    /**
     * 出栈
     */
    public int popStack(){
        return this.stack[--sp];
    }

    public static void main(String[] args) {
        System.out.println(-32721&0xFFFF);
    }
}
