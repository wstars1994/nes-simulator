package com.iwstars.mcnes.core.ppu;

import com.iwstars.mcnes.core.DataBus;
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
     * 初始化PPU 必须设置,从nes文件读取到的CHR数据
     * @param patternData 图案表数据
     */
    public Ppu(byte[] patternData){
        PpuMemory.writePattern(patternData);
    }

    /**
     * 开始绘制
     */
    public void startRender() {
        byte[] b2000 = DataBus.p_2000;
        byte[] b2001 = DataBus.p_2001;
        if(b2001[3] == 1) {
            byte addr = b2000[0];
            short nameTableAddr = 0;
            if(addr == 0x00) {
                nameTableAddr = 0x2000;
            }else if (addr == 0x01){
                nameTableAddr = 0x2400;
            }else if (addr == 0x10){
                nameTableAddr = 0x2800;
            }else if (addr == 0x11){
                nameTableAddr = 0x2C00;
            }
            if(nameTableAddr!=0) {
                renderNameTable(nameTableAddr);
            }
        }
        if(b2001[4] == 1) {

        }
        System.out.println("--------------------Render end--------------------");
    }
    private void renderNameTable(short addr) {
        System.out.println(addr);
    }
}
