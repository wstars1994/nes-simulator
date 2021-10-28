package com.iwstars.mcnes.core.mapper;

import com.iwstars.mcnes.core.DataBus;

/**
 * Mapper #2
 * @author WStars
 * @date 2021/10/28 10:29
 * @see <a href="https://wiki.nesdev.org/w/index.php?title=UxROM">INES_Mapper_002</a>
 */
public class Mapper002 implements IMapper {

    private byte switchBank = -1;
    private byte romPRGSize;
    private byte romChrSize;

    public Mapper002(byte romPRGSize, byte romChrSize) {
        this.romPRGSize = romPRGSize;
        this.romChrSize = romChrSize;
    }

    @Override
    public void write(int addr, byte data) {
        if(addr>=0x8000 & addr<=0xFFFF){
            switchBank = (byte) (data&0x7);
            return;
        }
        DataBus.cpuMemory.writeMem(addr,data);
    }

    @Override
    public byte read(int addr) {
        if(addr>=0xC000 && addr<=0xFFFF){
            addr-=0xC000;
            addr = 0x8000 + (romPRGSize-1)*16*1024+addr;
        }else if(addr>=0x8000 && addr<=0xBFFF && switchBank>=0){
            addr = addr+switchBank*16*1024;
        }
        return DataBus.cpuMemory.readMem(addr);
    }

    @Override
    public void writePpu(int addr, byte data) {
        DataBus.ppuMemory.write(addr,data);
    }

    @Override
    public byte readPpu(int addr) {
        return DataBus.ppuMemory.read(addr);
    }
}
