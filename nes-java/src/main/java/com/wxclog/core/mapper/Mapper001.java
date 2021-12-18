package com.wxclog.core.mapper;

import com.wxclog.core.DataBus;

/**
 * Mapper #1
 * @author WStars
 * @date 2021/10/28 10:29
 * @see <a href="https://wiki.nesdev.org/w/index.php?title=MMC1">INES_Mapper_001</a>
 */
public class Mapper001 implements IMapper {

    private byte switchBank = 0;
    private byte romPRGSize;

    public Mapper001(byte romPRGSize, byte romChrSize, byte[] romCHR) {
        this.romPRGSize = romPRGSize;
    }

    @Override
    public void write(int addr, byte data) {
        if(addr>=0x8000 & addr<=0xFFFF){

            System.out.println(data);

//            switchBank = (byte) (data&0x7);
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
