package com.iwstars.mcnes.nesrom;

import lombok.Data;

/**
 * @description: .nes rom数据
 * @author: WStars
 * @date: 2020-04-16 15:15
 */
@Data
public class RomData {

    private HeaderData headerData;

    private byte[] romPRG;

    private byte[] romCHR;
}
