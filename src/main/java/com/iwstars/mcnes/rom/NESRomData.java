package com.iwstars.mcnes.rom;

import lombok.Data;

/**
 * @description: .nes rom数据
 * @author: WStars
 * @date: 2020-04-16 15:15
 */
@Data
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
}
