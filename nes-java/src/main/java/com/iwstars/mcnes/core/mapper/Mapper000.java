package com.iwstars.mcnes.core.mapper;

import com.iwstars.mcnes.core.DataBus;

public class Mapper000 implements IMapper {

    @Override
    public void write(int addr, byte data) {
        DataBus.cpuMemory.writeMem(addr,data);
    }

    @Override
    public byte read(int addr) {
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
