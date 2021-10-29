package com.iwstars.mcnes.core.mapper;

import com.iwstars.mcnes.core.DataBus;

/**
 * Mapper #2
 * @author WStars
 * @date 2021/10/28 10:29
 * @see <a href="https://wiki.nesdev.org/w/index.php?title=UxROM">INES_Mapper_002</a>
 * CPU $8000-$BFFF: 16 KB switchable PRG ROM bank
 * CPU $C000-$FFFF: 16 KB PRG ROM bank, fixed to the last bank
 */
public class Mapper002 implements IMapper {

    private byte switchBank = 0;
    private byte romPRGSize;

    public Mapper002(byte romPRGSize) {
        this.romPRGSize = romPRGSize;
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
            //偏移量
            addr -= 0xC000;
            addr += 0x8000+(romPRGSize-1)*16*1024;
        }else if(addr>=0x8000 && addr<=0xBFFF && switchBank>=0){
            addr += switchBank*16*1024;
        }
        return DataBus.cpuMemory.readMem(addr);
    }

    @Override
    public void writePpu(int addr, byte data) {
        DataBus.ppuMemory.write(addr,data);
    }

    @Override
    public byte readPpu(int addr) {
        return DataBus.ppuMemory.readMem(addr);
    }
}
