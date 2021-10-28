package com.iwstars.mcnes.core.mapper;

import com.iwstars.mcnes.core.DataBus;

/**
 * Mapper #67
 * @author WStars
 * @date 2021/10/28 10:29
 * @see <a href="https://wiki.nesdev.org/w/index.php?title=INES_Mapper_067">INES_Mapper_067</a>
 * 调式游戏: 俄罗斯方块
 */
public class Mapper067 implements IMapper {

    private byte switchBank = 0;
    private byte romPRGSize;
    private byte[] cardChr;

    public Mapper067(byte romPRGSize, byte romChrSize,byte[] romCHR) {
        this.romPRGSize = romPRGSize;
        if(romChrSize > 1){
            cardChr = new byte[(romChrSize-1)*8*1024];
            System.arraycopy(romCHR,1*8*1024,cardChr,0,cardChr.length);
        }
    }

    @Override
    public void write(int addr, byte data) {
        switch (addr){
            //Interrupt Acknowledge ($8000)
            case 0x8000:
                break;
            //CHR bank 0…3 ($8800..$BFFF)
            case 0x8800:
            case 0x9800:
            case 0xA800:
            case 0xB800:
                break;
            //IRQ load ($C800, write twice)
            case 0xC800:
                break;
            //IRQ enable ($D800)
            case 0xD800:
                break;
            //Mirroring ($E800)
            case 0xE800:
                break;
            //PRG bank ($F800)
            case 0xF800:
                break;
        }
        DataBus.cpuMemory.writeMem(addr,data);
    }

    @Override
    public byte read(int addr) {
        if(addr >= 0xC000 && addr <= 0xFFFF){
            addr-=0xC000;
            addr = 0x8000 + (romPRGSize-1)*16*1024+addr;
        }else if(addr>=0x8000 && addr<=0xBFFF && switchBank>0){
            addr = addr + switchBank*16*1024;
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
