package com.iwstars.mcnes.core.mapper;

import com.iwstars.mcnes.core.DataBus;

/**
 * Mapper #66
 * @author WStars
 * @date 2021/10/28 10:29
 * @see <a href="https://wiki.nesdev.org/w/index.php?title=GxROM">INES_Mapper_066</a>
 * 调式游戏: AV麻将
 */
public class Mapper066 implements IMapper {

    private byte switchPRGBank = 0,switchCHRBank = 0;
    private byte[] cardChr;

    public Mapper066(byte romPRGSize, byte romChrSize, byte[] romCHR) {
        if(romChrSize > 1){
            cardChr = new byte[(romChrSize-1)*8*1024];
            System.arraycopy(romCHR,1*8*1024,cardChr,0,cardChr.length);
        }
    }

    @Override
    public void write(int addr, byte data) {
        if(addr>=0x8000 && addr<=0xFFFF){
            switchCHRBank = (byte) (data&0x3);
            switchPRGBank = (byte) ((data>>4)&0x1);
            return;
        }
        DataBus.cpuMemory.writeMem(addr,data);
    }

    @Override
    public byte read(int addr) {
        if(switchPRGBank>0&&addr>=0x8000 && addr<=0xFFFF){
            addr += switchPRGBank*32*1024;
        }
        return DataBus.cpuMemory.readMem(addr);
    }

    @Override
    public void writePpu(int addr, byte data) {
        DataBus.ppuMemory.write(addr,data);
    }

    @Override
    public byte readPpu(int addr) {
        if(switchCHRBank>0&&addr>=0x0000&&addr<=0x1FFF){
            return cardChr[addr+(switchCHRBank-1)*8*1024];
        }
        return DataBus.ppuMemory.readMem(addr);
    }
}
