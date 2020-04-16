package com.iwstars.mcnes.nesrom;

import lombok.Data;

/**
 * @description: 头控制数据 从低位到高位
 * @author: WStars
 * @date: 2020-04-16 15:24
 */
@Data
public class HeaderRomControl1Data {

    /**
     * 1bit
     * 0 : Horizontal
     * 1 : Vertical
     */
    private byte mirrorType;

    /**
     * 1bit
     * SRAM (可以理解为存档,电池记忆)
     * 1有存档功能
     * 0没有
     */
    private byte SRAMEnabled;

    /**
     * 1bit
     * 是否有Trainer
     * 1有
     * 0没有
     */
    private byte trainerPresent;

    /**
     * 1bit
     * 4屏幕VRAM布局
     * 1是
     * 0否
     */
    private byte vram4;

    /**
     * 4bit
     * rom mapper的低四位
     */
    private byte[] romMapperLow;
}
