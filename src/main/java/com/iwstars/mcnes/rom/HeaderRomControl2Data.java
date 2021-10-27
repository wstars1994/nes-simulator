package com.iwstars.mcnes.rom;

/**
 * @description: 头控制数据2  - 8位从左至右
 * @author: WStars
 * @date: 2020-04-16 15:33
 */
public class HeaderRomControl2Data {

    /**
     * 4bit
     * 低4位保留0
     */
    private byte[] zero1;

    /**
     * 4bit
     * rom mapper的高四位
     */
    private byte romMapperHigh;

    public byte[] getZero1() {
        return zero1;
    }

    public void setZero1(byte[] zero1) {
        this.zero1 = zero1;
    }

    public byte getRomMapperHigh() {
        return romMapperHigh;
    }

    public void setRomMapperHigh(byte romMapperHigh) {
        this.romMapperHigh = romMapperHigh;
    }
}
