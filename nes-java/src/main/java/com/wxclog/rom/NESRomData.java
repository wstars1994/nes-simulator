package com.wxclog.rom;

/**
 * @description: .nes rom数据
 * @author: WStars
 * @date: 2020-04-16 15:15
 */
public class NESRomData {

    /**
     * 头信息
     */
    private HeaderData headerData;

    /**
     * 程序数据 size =  16k *  headerData.romPRGSize
     */
    private byte[] romPRG;

    /**
     * 图案数据 size =  8k *  headerData.romCHRSize
     */
    private byte[] romCHR;


    public HeaderData getHeaderData() {
        return headerData;
    }

    public void setHeaderData(HeaderData headerData) {
        this.headerData = headerData;
    }

    public byte[] getRomPRG() {
        return romPRG;
    }

    public void setRomPRG(byte[] romPRG) {
        this.romPRG = romPRG;
    }

    public byte[] getRomCHR() {
        return romCHR;
    }

    public void setRomCHR(byte[] romCHR) {
        this.romCHR = romCHR;
    }
}
