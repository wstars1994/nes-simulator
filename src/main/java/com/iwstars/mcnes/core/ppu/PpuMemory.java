package com.iwstars.mcnes.core.ppu;

import com.iwstars.mcnes.util.LogUtil;

/**
 * <p>图形处理单元</p>
 * 64KB space
 *
 * Address range     Size	     Description
 * 图案表部分
 * $0000-$0FFF	    $1000	    Pattern table 0         图案表0 - NES文件CHR部分 - Tile
 * $1000-$1FFF	    $1000	    Pattern table 1         图案表1 - NES文件CHR部分 - Tile
 * 命名表部分
 * $2000-$23FF	    $0400	    Nametable 0             命名表0 - 32x30的小格子,每个小格子由2个8x8像素的Tile组成
 * $2400-$27FF	    $0400	    Nametable 1             命名表1 - 32x30的小格子,每个小格子由2个8x8像素的Tile组成
 * $2800-$2BFF	    $0400	    Nametable 2             命名表2 - 32x30的小格子,每个小格子由2个8x8像素的Tile组成
 * $2C00-$2FFF	    $0400	    Nametable 3             命名表3 - 32x30的小格子,每个小格子由2个8x8像素的Tile组成
 * 镜像 : $2000-$2EFF
 * $3000-$3EFF	    $0F00	    Mirrors of $2000-$2EFF
 * 调色板
 * $3F00-$3F1F	    $0020	    Palette RAM indexes
 * 调色板镜像
 * $3F20-$3FFF	    $00E0	    Mirrors of $3F00-$3F1F
 *
 * @author WStars
 * @date 2020/4/18 13:46
 */
public class PpuMemory{

    private byte[] ppuData = new byte[65535];

    /**
     * 精灵数据
     */
    private byte[] sprRam = new byte[256];

    /**
     * 调色板
     */
    public short[][] palettes = {
            { 84, 84, 84},{0,30,116},{8,16,144},{48,0,136},{68,0,100},{92,0,48},{84,4,0},{60,24,0},{32,42,0},{8,58,0},{0,64,0},{0,60,0},{0,50,60},{0,0,0},{0,0,0},{0,0,0},
            {152,150,152},{8,76,196},{48,50,236},{92,30,228},{136,20,176},{160,20,100},{152,34,32},{120,60,0},{84,90,0},{40,114,0},{8,124,0},{0,118,40},{0,102,120},{0,0,0},{0,0,0},{0,0,0},
            {236,238,236},{76,154,236},{120,124,236},{176,98,236},{228,84,236},{236,88,180},{236,106,100},{212,136,32},{160,170,0},{116,196,0},{76,208,32},{56,204,108},{56,180,204},{60,60,60},{0,0,0},{0,0,0},
            {236,238,236},{168,204,236},{188,188,236},{212,178,236},{236,174,236},{236,174,212},{236,180,176},{228,196,144},{204,210,120},{180,222,120},{168,226,144},{152,226,180},{160,214,228},{160,162,160},{0,0,0},{0,0,0}};

    public void write(int addr, byte data) {
        LogUtil.logf(" write PPU memory addr=%02X[%d],val=%d",addr,addr,data);
        //写入 $3F00-3FFF 的 D7-D6 字节被忽略.
        if(addr >= 0x3F00 && addr <= 0x3FFF) {
            data = (byte) (data & 0x3f);
            if ((addr & 3) == 0){
                addr &= ~0x10;
            }
        }
        ppuData[addr] = data;
    }

    public byte read(int addr) {
        return this.ppuData[addr];
    }

    /**
     * 写入图案表
     * @param data
     */
    public void writePattern(byte[] data) {
        System.arraycopy(data,0,ppuData,0,data.length);
    }

    /**
     * 写入精灵数据
     * @param addr
     * @param data
     */
    public void writeSprRam(byte addr, byte data) {
        this.sprRam[addr&0xFF] = data;
    }
}
