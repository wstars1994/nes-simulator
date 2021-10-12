package com.iwstars.mcnes.core.ppu;

import com.iwstars.mcnes.core.DataBus;
import com.iwstars.mcnes.util.MemUtil;
import lombok.Getter;

import java.util.Arrays;

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
@Getter
public class Ppu {

    private PpuMemory ppuMemory;

    /**
     * 初始化PPU  从nes文件读取到的CHR数据
     * @param patternData 图案表数据
     */
    public Ppu(byte[] patternData){
        ppuMemory = new PpuMemory();
        ppuMemory.writePattern(patternData);
    }

    /**
     * 开始绘制扫描线
     */
    public short[][] preRender(int scanLineIndex) {
        short[][] render = new short[256+16][3];
        //填充背景色
        Arrays.fill(render,ppuMemory.palettes[ppuMemory.read(0x3F00)]);
        byte[] b2000 = DataBus.p_2000;
        byte[] b2001 = DataBus.p_2001;
        //渲染背景
        if(b2001[3] == 1) {
            this.renderNameTable(render);
        }
        //渲染精灵
        if(b2001[4] == 1) {
            short spritePatternAddr = (short) (b2000[3]==0 ? 0:0x1000);
            byte spriteSize = b2000[5];
            this.renderSprite(scanLineIndex,spritePatternAddr,spriteSize,render);
        }
        return render;
    }

    /**
     * 绘制命名表
     * @see <a href="https://wiki.nesdev.org/w/index.php?title=PPU_scrolling">PPU_scrolling</a>
     * @param render
     */
    private void renderNameTable(short[][] render) {
        byte fine_x = DataBus.p_scroll_x;
        byte fine_y = (byte) ((DataBus.p_vram_addr >> 12) & 7);
        short nameTableAddress  = (short) (0x2000 | (DataBus.p_vram_addr & 0x0FFF));
        short patternStartAddr = (short) (DataBus.p_2000[4] == 0 ?0x0000:0x1000);
        //32*30个Tile = (256*240 像素)
        for (int i=0;i<32;i++) {
            //指示哪个tile
            byte coarse_x = (byte) (nameTableAddress&0x1F);
            byte coarse_y = (byte) ((nameTableAddress>>5)&0x1F);
            //1 读取name table数据,其实就是Tile图案表索引  (图案+颜色 = 8字节+8字节=16字节)
            byte nameTableData = ppuMemory.read(nameTableAddress);
            //2 读取图案,图案表起始地址+索引+具体渲染的8字节中的第几字节
            int patternAddress = patternStartAddr + (nameTableData&0xff) * 16 + fine_y;
            //图案表数据
            byte patternData = ppuMemory.read(patternAddress);
            //图案表颜色数据
            byte colorData = ppuMemory.read(patternAddress + 8);
            //取颜色低两位
            byte[] patternColorLowData = getPatternColorLowData(patternData,colorData);
            //取颜色高两位,属性表数据64byte,每32*32像素一个字节,每32条扫描线占用8字节
            int attributeOffset = ((coarse_y & 2) == 0 ? 0 : 4) + ((coarse_x & 2) == 0 ? 0 : 2);
            int attributeAddress = 0x23C0 | (nameTableAddress & 0x0C00) | (coarse_y>>2)<<3 | (coarse_x >> 2);
            byte pchb = (byte) ((ppuMemory.read(attributeAddress)>>attributeOffset)&3);
            //合并 取最终4位颜色
            for (int j = 0; j <8; j++) {
                int pclb = patternColorLowData[7 - j];
                //透明色 显示背景色
                if(pclb != 0) {
                    int colorAddr = 0x3f00 + (pchb<<2 | (pclb & 0x3));
                    int paletteIndex = ppuMemory.read(colorAddr);
                    render[i*8+j] = ppuMemory.palettes[paletteIndex];
                }
            }
//            // if coarse X == 31 (coarseX的最大值就是31即11111B,所以到最大值了要切换到下一个nametable)
            if ((nameTableAddress & 0x001F) == 0x1F) {
                // coarse X = 0
                nameTableAddress &= ~0x001F;
                // switch horizontal nametable
                nameTableAddress ^= 0x0400;
            }else {
                nameTableAddress++;
            }
        }
    }
    /**
     * 渲染精灵
     * @param spritePatternStartAddr 8x8精灵开始地址
     * @param spriteSize 精灵size 0=8x8;1=8x16(注意,8x16精灵上面的8x8像素在图案表0x0000,下面的在0x1000,不受spritePatternAddr控制)
     * @param render
     */
    private void renderSprite(int sl,short spritePatternStartAddr, byte spriteSize, short[][] render) {
        //获取精灵高度
        byte spriteHeight = (byte) (spriteSize == 0?8:16);
        //获取内存中的精灵数据
        byte[] sprRam = ppuMemory.getSprRam();
        for (int i = 0; i < sprRam.length; i += 4) {
            short y = (short) ((sprRam[i]&0xff));
            short patternIndex = (short) (sprRam[i+1]&0xff);
            //子图形数据
            byte attributeData = sprRam[i+2];
            //背景层级
            byte backgroundPriority = (byte) ((attributeData>>5)&1);
            //图案垂直翻转
            byte vFlip = (byte) ((attributeData>>7)&1);
            //图案水平翻转
            byte hFlip = (byte) ((attributeData>>6)&1);
            short x = (short) (sprRam[i+3]&0xff);
            if(sl >= y && sl <= y + spriteHeight) {
                //获取图案地址
                if(spriteHeight == 16) {
                    byte[] bytes = MemUtil.toBits((byte) patternIndex);
                    spritePatternStartAddr = (short) (bytes[0] == 0 ? 0x0000:0x1000);
                }
                int spritePatternAddr = spritePatternStartAddr + patternIndex * 16 + (sl - y);
                byte spritePatternData = ppuMemory.read(spritePatternAddr);
                //获取图案颜色数据
                byte colorData = ppuMemory.read(spritePatternAddr + 8);
                byte[] patternColorLowData = getPatternColorLowData(spritePatternData,colorData);
                byte patternColorHighData = (byte) (attributeData & 0x03);
                //命中非透明背景 sprite hit
                if(spritePatternData + colorData != 0 && DataBus.p_2001[1] != 0 &&DataBus.p_2001[2] != 0) {
                    DataBus.p_2002[6] = 1;
                }
                for (int i1 = 0; i1 < 8; i1++) {
                    if(backgroundPriority == 0) {
                        //获取4位颜色
                        int colorAddr = 0x3f10 + (((patternColorHighData << 2) & 0xF) | ((patternColorLowData[7 - i1]) & 0x3));
                        if(colorAddr != 0x3f10) {
                            render[x + i1] = ppuMemory.palettes[ppuMemory.read(colorAddr)];
                        }
                    }
                }
//                if(hFlip == 1) {
//                    for (int j = 0; j < 4; j++) {
//                        short[] temp = render[x + j];
//                        render[x + j] = render[(x+7)-j];
//                        render[(x+7)-j] =temp;
//                    }
//                }
//                if(vFlip == 1) {
//                    for (int j = 0; j < 4; j++) {
//                        short[] temp = render[x + j];
//                        render[x + j] = render[(x+7)-j];
//                        render[(x+7)-j] =temp;
//                    }
//                }
            }
        }
    }

    /**
     * 获取图案的低两位颜色
     * @param patternData
     * @param colorData
     * @return
     */
    private byte[] getPatternColorLowData(byte patternData, byte colorData) {
        byte[] patternDatas = MemUtil.toBits(patternData);
        byte[] colorDatas = MemUtil.toBits(colorData);
        byte patternColorData[] = new byte[8];
        for(int i=7;i>=0;i--) {
            patternColorData[i] = (byte) (((colorDatas[i]<<1)&3) | (patternDatas[i]&1));
        }
        return patternColorData;
    }

    private void print8(byte data){
        String s = Integer.toBinaryString(data & 0xFF);
        while (s.length()<8) {
            s="0"+s;
        }
        System.out.print(s);
    }
}
