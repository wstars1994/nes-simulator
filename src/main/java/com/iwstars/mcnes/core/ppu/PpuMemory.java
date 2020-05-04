package com.iwstars.mcnes.core.ppu;

/**
 * 图形处理单元
 * 16KB space
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
public class PpuMemory {

    private static byte[] ppuData = new byte[16 * 1024];

    private static byte[] sprRam = new byte[256];

    public static void write(short addr, byte data) {
        System.out.printf(" write ppu memory addr=%02X[%d],val=%d",addr,addr,data);
        ppuData[addr] = data;
    }

    /**
     * 写入图案表
     * @param data
     */
    public static void writePattern(byte[] data) {
        System.arraycopy(data,0,ppuData,0,data.length);
    }

    /**
     * 写入精灵数据
     * @param addr
     * @param data
     */
    public static void writeSprRam(byte addr, byte data) {
        PpuMemory.sprRam[addr&0xFF] = data;
    }

    public static byte read(int addr) {
        return ppuData[addr];
    }
}
