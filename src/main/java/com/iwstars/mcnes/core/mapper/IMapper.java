package com.iwstars.mcnes.core.mapper;

public interface IMapper {

    static IMapper getMapper(byte mapperNo){
        switch (mapperNo){
            case 67:
                return new Mapper067();
            case 2:
                return new Mapper002();
        }
        return null;
    }

    void write(int addr,byte data);

    byte read(int addr);
}
