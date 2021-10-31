package com.wxclog.rom;

/**
 * @description: 头数据
 * @author: WStars
 * @date: 2020-04-16 15:18
 */
public class HeaderData {

    /**
     * 3byte
     */
    private String magic;

    /**
     * 1byte
     */
    private byte magicEof;

    /**
     * 1byte
     *
     */
    private byte romPRGSize;

    /**
     * 1byte
     */
    private byte romCHRSize;

    /**
     * 1byte
     */
    private HeaderRomControl1Data controlData1;

    /**
     * 1byte
     */
    private HeaderRomControl2Data controlData2;

    /**
     * 8byte
     * 保留
     */
    private long zero;

    /**
     * mapper
     */
    private byte mapperNo;


    public String getMagic() {
        return magic;
    }

    public void setMagic(String magic) {
        this.magic = magic;
    }

    public byte getMagicEof() {
        return magicEof;
    }

    public void setMagicEof(byte magicEof) {
        this.magicEof = magicEof;
    }

    public byte getRomPRGSize() {
        return romPRGSize;
    }

    public void setRomPRGSize(byte romPRGSize) {
        this.romPRGSize = romPRGSize;
    }

    public byte getRomCHRSize() {
        return romCHRSize;
    }

    public void setRomCHRSize(byte romCHRSize) {
        this.romCHRSize = romCHRSize;
    }

    public HeaderRomControl1Data getControlData1() {
        return controlData1;
    }

    public void setControlData1(HeaderRomControl1Data controlData1) {
        this.controlData1 = controlData1;
    }

    public HeaderRomControl2Data getControlData2() {
        return controlData2;
    }

    public void setControlData2(HeaderRomControl2Data controlData2) {
        this.controlData2 = controlData2;
    }

    public long getZero() {
        return zero;
    }

    public void setZero(long zero) {
        this.zero = zero;
    }

    public byte getMapperNo() {
        return mapperNo;
    }

    public void setMapperNo(byte mapperNo) {
        this.mapperNo = mapperNo;
    }
}
