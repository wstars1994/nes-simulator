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
public class PPU {

    /**
     * PPU内存结构
     */
    private PPUData ppuData = new PPUData();

    /**
     * 初始化PPU 必须设置,从nes文件读取到的CHR数据
     * @param patternData 图案表数据
     */
    public PPU(byte[] patternData){
        byte[] patternData0 = new byte[4 * 1024];
        byte[] patternData1 = new byte[0];
        System.arraycopy(patternData,0,patternData0,0,patternData0.length);
        if(patternData.length > 4 * 1024) {
            patternData1 = new byte[patternData.length - 4 * 1024];
            System.arraycopy(patternData,4 * 1024,patternData1,0,patternData1.length);
        }
        ppuData.setPattern_0(patternData0);
        ppuData.setPattern_1(patternData1);
    }

    /**
     * 渲染命名表
     * @param index 序号
     * @param data 数据
     */
    private void render(int index,byte[] data){
        if(index == 0) {
            ppuData.setNameTable_0(data);
        }else if(index == 1) {
            ppuData.setNameTable_1(data);
        }else if(index == 2) {
            ppuData.setNameTable_2(data);
        }else if(index == 3) {
            ppuData.setNameTable_3(data);
        }else{
            throw new RuntimeException("此序号命名表不存在");
        }
    }

    public void renderNameTable(byte[] nameTableData) {
        int PPUCTRL = nameTableData[0]&0xFF;
        System.out.println("PPUCTRL = "+Integer.toBinaryString(PPUCTRL));
        int PPUMASK = nameTableData[1]&0xFF;
        System.out.println("PPUMASK = "+Integer.toBinaryString(PPUMASK));
//        int PPUSTATUS = nameTableData[2]&0xFF;
//        System.out.println("PPUSTATUS = "+Integer.toBinaryString(PPUSTATUS));
        int OAMADDR = nameTableData[2]&0xFF;
        System.out.println("OAMADDR = "+Integer.toBinaryString(OAMADDR));

        int OAMDATA = nameTableData[3]&0xFF;
        System.out.println("OAMDATA = "+Integer.toBinaryString(OAMDATA));
        ppuData.getSprRam()[OAMADDR] = OAMDATA;

        int PPUSCROLL = nameTableData[4]&0xFF;
        System.out.println("PPUSCROLL = "+Integer.toBinaryString(PPUSCROLL));
        int PPUADDR = nameTableData[5]&0xFF;
        System.out.println("PPUADDR = "+Integer.toBinaryString(PPUADDR));
        int PPUDATA = nameTableData[6]&0xFF;
        System.out.println("PPUDATA = "+Integer.toBinaryString(PPUDATA));
        System.out.println("------------------------------------");
        this.getPpuCtrl(PPUCTRL);
        System.out.println("------------------------------------");
        this.getPpuMask(PPUMASK);
        System.out.println("------------------------------------");
        System.out.println("------------------------------------");
    }

    /**
     * PPU控制寄存器(PPU control register)
     * @param PPUCTRL
     */
    private void getPpuCtrl(int PPUCTRL){
        //Base nametable address (0 = $2000; 1 = $2400; 2 = $2800; 3 = $2C00)
        int nameTableAddressIndex = PPUCTRL & 0x3;
        System.out.println("命名表选择 : " + nameTableAddressIndex);
        this.render(nameTableAddressIndex,null);

        //VRAM address increment per CPU read/write of PPUDATA (0: add 1, going across; 1: add 32, going down)
        int addressIncrementVRAMType  = (PPUCTRL>>2) & 0x1;
//        System.out.println("addressIncrementVRAMType select : " + addressIncrementVRAMType);

        //Sprite pattern table address for 8x8 sprites (0: $0000; 1: $1000; ignored in 8x16 mode)
        int spritePatternTableAddress  = (PPUCTRL>>3) & 0x1;
        System.out.println("精灵图案表选择 : " + spritePatternTableAddress);

        //Background pattern table address (0: $0000; 1: $1000)
        int backgroundPatternTableAddress = (PPUCTRL>>4) & 0x1;
        System.out.println("背景图案表选择 : " + backgroundPatternTableAddress);

        //Sprite size (0: 8x8 pixels; 1: 8x16 pixels)
        int spriteSize = (PPUCTRL>>5) & 0x1;
        System.out.println("精灵大小 : " + spriteSize);

        //PPU master/slave select (0: read backdrop from EXT pins; 1: output color on EXT pins)
        int ppuMLSelect = (PPUCTRL>>6) & 0x1;

        //Generate an NMI at the start of the vertical blanking interval (0: off; 1: on)
        int nmi = (PPUCTRL>>7) & 0x1;
//        System.out.println("nmi : " + nmi);
    }
    /**
     * PPU掩膜寄存器(PPU mask register)
     * 渲染背景和精灵sprite以及色彩效果
     * @param ppumask
     */
    private void getPpuMask(int ppumask) {
        //Greyscale (0: normal color, 1: produce a greyscale display)
        int greyscale = ppumask & 0x1;
        System.out.println("灰度级 : " + greyscale);

        //1: Show background in leftmost 8 pixels of screen, 0: Hide
        int bgShowLeftmost = (ppumask>>1) & 0x1;
        System.out.println("显示背景在屏幕左8像素 : " + bgShowLeftmost);

        //1: Show sprites in leftmost 8 pixels of screen, 0: Hide
        int spritesShowLeftmost = (ppumask>>2) & 0x1;
        System.out.println("显示精灵在屏幕左8像素 : " + spritesShowLeftmost);

        //1: Show background
        int bgShow = (ppumask>>3) & 0x1;
        System.out.println("显示背景 : " + bgShow);

        //1: Show sprites
        int spritesShow = (ppumask>>4) & 0x1;
        System.out.println("显示精灵 : " + spritesShow);

        //Emphasize red
        int red = (ppumask>>5) & 0x1;
        System.out.println("red : " + red);

        //Emphasize green
        int green = (ppumask>>6) & 0x1;
        System.out.println("green : " + red);

        //Emphasize blue
        int blue = (ppumask>>7) & 0x1;
        System.out.println("blue : " + blue);
    }
}
