package com.iwstars.mcnes.core.mapper;

import com.iwstars.mcnes.core.DataBus;

public class Mapper067 implements IMapper {

    private byte switchBank = -1;

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
            addr = 0x8000+1*16*1024+addr;
        }else if(addr>=0x8000 && addr<=0xBFFF && switchBank>=0){
            addr = addr + switchBank*16*1024;
        }
        return DataBus.cpuMemory.readMem(addr);
    }
}
