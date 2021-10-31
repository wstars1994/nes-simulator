package com.wxclog.rom;

/**
 * @description: 头控制数据 从低位到高位
 * @author: WStars
 * @date: 2020-04-16 15:24
 */
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
     * 是否有512byte Trainer
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
    private byte fourScreen;

    /**
     * 4bit
     * rom mapper的低四位
     */
    private byte romMapperLow;

    public byte getMirrorType() {
        return mirrorType;
    }

    public void setMirrorType(byte mirrorType) {
        this.mirrorType = mirrorType;
    }

    public byte getSRAMEnabled() {
        return SRAMEnabled;
    }

    public void setSRAMEnabled(byte SRAMEnabled) {
        this.SRAMEnabled = SRAMEnabled;
    }

    public byte getTrainerPresent() {
        return trainerPresent;
    }

    public void setTrainerPresent(byte trainerPresent) {
        this.trainerPresent = trainerPresent;
    }

    public byte getFourScreen() {
        return fourScreen;
    }

    public void setFourScreen(byte fourScreen) {
        this.fourScreen = fourScreen;
    }

    public byte getRomMapperLow() {
        return romMapperLow;
    }

    public void setRomMapperLow(byte romMapperLow) {
        this.romMapperLow = romMapperLow;
    }
}
