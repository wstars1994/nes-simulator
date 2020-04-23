package com.iwstars.mcnes.core.ppu;

import lombok.Getter;
import lombok.Setter;

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
@Setter
@Getter
public class Ppu {

    /**
     * PPU内存结构
     */
    private PpuMemory ppuMemory = new PpuMemory();

    /**
     * 初始化PPU 必须设置,从nes文件读取到的CHR数据
     * @param patternData 图案表数据
     */
    public Ppu(byte[] patternData){
        byte[] patternData0 = new byte[4 * 1024];
        byte[] patternData1 = new byte[0];
        System.arraycopy(patternData,0,patternData0,0,patternData0.length);
        if(patternData.length > 4 * 1024) {
            patternData1 = new byte[patternData.length - 4 * 1024];
            System.arraycopy(patternData,4 * 1024,patternData1,0,patternData1.length);
        }
        ppuMemory.setPattern_0(patternData0);
        ppuMemory.setPattern_1(patternData1);
    }

    /**
     * 渲染命名表
     * @param index 序号
     * @param data 数据
     */
    private void render(int index,byte[] data){
        if(index == 0) {
            ppuMemory.setNameTable_0(data);
        }else if(index == 1) {
            ppuMemory.setNameTable_1(data);
        }else if(index == 2) {
            ppuMemory.setNameTable_2(data);
        }else if(index == 3) {
            ppuMemory.setNameTable_3(data);
        }else{
            throw new RuntimeException("此序号命名表不存在");
        }
    }

    /**
     * 开始绘制
     */
    public void startRender() {



    }
}
