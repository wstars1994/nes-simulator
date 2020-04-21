package com.iwstars.mcnes.core.ppu;

import lombok.Data;

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
@Data
public class PpuMemory {

    private byte[] ppuData;

    /**
     * 图案表$0000-$1FFF
     */
    private byte[] pattern_0 = new byte[0x1000];
    private byte[] pattern_1 = new byte[0x1000];
    /**
     * 命名表$2000-$2FFF
     */
    private byte[] nameTable_0 = new byte[0x0400];
    private byte[] nameTable_1 = new byte[0x0400];
    private byte[] nameTable_2 = new byte[0x0400];
    private byte[] nameTable_3 = new byte[0x0400];

    /**
     * 命名表镜像 $2000-$2EFF
     */
    private byte[] nameTableMirrors = new byte[0x0F00];
    /**
     * 调色板(任天堂官方文档 http://nesdev.com/NESDoc.pdf 第45页 或者 http://wiki.nesdev.com/w/index.php/PPU_palettes)
     */
    private byte[] palette = new byte[0x0020];
    /**
     * 调色板镜像(任天堂官方固定)
     */
    private byte[] paletteMirrors = new byte[0x00E0];

    /**
     * 子画面 256byte
     */
    private int[] sprRam = new int[0x100];

}
