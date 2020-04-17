package com.iwstars.mcnes.rom;

import lombok.Data;

/**
 * @description: 头控制数据2  - 8位从左至右
 * @author: WStars
 * @date: 2020-04-16 15:33
 */
@Data
public class HeaderRomControl2Data {

    /**
     * 4bit
     * 低4位保留0
     */
    private byte[] zero1;

    /**
     * 4bit
     * rom mapper的低四位
     */
    private byte[] romMapperHigh;
}
