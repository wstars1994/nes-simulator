package com.iwstars.mcnes.core.cpu;

import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

/**
 * CPU数据
 *
 * Address range	Size	Device
 * $0000-$07FF	   $0800	2KB internal RAM
 * $0800-$0FFF	   $0800	Mirrors of $0000-$07FF
 * $1000-$17FF	   $0800    Mirrors of $0000-$07FF
 * $1800-$1FFF	   $0800    Mirrors of $0000-$07FF
 * $2000-$2007	   $0008	NES Ppu registers PPU寄存器
 * $2008-$3FFF	   $1FF8	Mirrors of $2000-2007 (repeats every 8 bytes) 镜像 每8个字节
 * $4000-$4017	   $0018	NES APU and I/O registers
 * $4018-$401F	   $0008	APU and I/O functionality that is normally disabled. See CPU Test Mode.
 * $4020-$FFFF	   $BFE0	Cartridge space: PRG ROM, PRG RAM, and mapper registers
 *
 * @author WStars
 * @date 2020/4/18 15:25
 */
@Setter
@Getter
public class CpuMemory implements Iterable<Byte> {

    int index = 0;

    private byte[] prgData;
    /**
     * cpu内存
     */
    private byte[] data = new byte[0xFFFF];

    public Iterator<Byte> iterator() {
        return new Iterator<Byte>() {

            public boolean hasNext() {
                return index < 50;
            }

            public Byte next() {
                return prgData[index++];
            }

            public void remove() {
            }
        };
    }

    /**
     * 写数据
     * @param addr
     * @param data
     */
    public void write(short addr,byte data){
        this.data[addr] = data;
    }

    /**
     * 读数据
     * @param addr
     * @return
     */
    public byte read(short addr){
        return this.data[addr];
    }
}
