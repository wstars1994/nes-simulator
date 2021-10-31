package com.wxclog.core.mapper;

import com.wxclog.core.DataBus;

/**
 * Mapper #87
 * @author WStars
 * @date 2021/10/28 10:29
 * @see <a href="https://wiki.nesdev.org/w/index.php?title=INES_Mapper_087">INES_Mapper_087</a>
 * 调式游戏:七宝奇谋
 */
public class Mapper087 implements IMapper {

    private byte switchCHRBank = 0;
    private byte[] cardChrBank;
    private byte romChrSize;

    public Mapper087(byte romPRGSize, byte romChrSize, byte[] romCHR) {
        this.romChrSize = romChrSize;
        if(romChrSize > 1){
            cardChrBank = new byte[(romChrSize-1)*8*1024];
            System.arraycopy(romCHR,1*8*1024,cardChrBank,0,cardChrBank.length);
        }
    }

    @Override
    public void write(int addr, byte data) {
        if(addr>=0x6000 && addr<=0x7FFF){
            if(this.romChrSize>2){
                switchCHRBank = (byte) ((data&1<<1)|((data>>1)&1));
            }else{
                switchCHRBank = (byte) ((data>>1)&1);
            }
            return;
        }
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
        if(switchCHRBank>0&&addr>=0x0000&&addr<=0x1FFF){
            return cardChrBank[addr+(switchCHRBank-1)*8*1024];
        }
        return DataBus.ppuMemory.readMem(addr);
    }
}
