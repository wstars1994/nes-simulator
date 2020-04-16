package com.iwstars.mcnes.nesrom;

import lombok.Data;

/**
 * @description: 头数据
 * @author: WStars
 * @date: 2020-04-16 15:18
 */
@Data
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
     */
    private byte romPRGSize;

    /**
     * 1byte
     */
    private byte romCHRSize;

    /**
     * 1byte
     */
    private HeaderRomControl1Data headerRomControlData;

    /**
     * 1byte
     */
    private HeaderRomControl2Data headerRomControl2Data;

    /**
     * 8byte
     * 保留
     */
    private long zero;

}
