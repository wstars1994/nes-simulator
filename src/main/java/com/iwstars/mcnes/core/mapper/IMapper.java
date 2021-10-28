package com.iwstars.mcnes.core.mapper;

public interface IMapper {

    static IMapper getMapper(byte mapperNo, byte romPRGSize, byte romChrSize, byte[] romCHR){
        switch (mapperNo){
            case 0:
                return new Mapper000();
            case 2:
                return new Mapper002(romPRGSize,romChrSize);
            case 3:
                return new Mapper003(romPRGSize,romChrSize,romCHR);
            case 66:
                return new Mapper066(romPRGSize,romChrSize,romCHR);
            case 67:
                return new Mapper067(romPRGSize,romChrSize,romCHR);
            case 87:
                return new Mapper087(romPRGSize,romChrSize,romCHR);

        }
        throw new RuntimeException("mapper"+mapperNo+"未实现,此nes无法运行!");
    }

    void write(int addr,byte data);

    byte read(int addr);

    void writePpu(int addr,byte data);

    byte readPpu(int addr);
}
