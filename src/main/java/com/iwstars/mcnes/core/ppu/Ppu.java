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
        short[][] render = new short[256][3];
        byte[] b2000 = DataBus.p_2000;
        byte[] b2001 = DataBus.p_2001;
        //渲染背景
        if(b2001[3] == 1) {
            byte ntAddr = b2000[0];
            byte bgAddr = b2000[4];
            short nameTableAddr = 0;
            short patternAddr = 0;
            if(ntAddr == 0x00) {
                nameTableAddr = 0x2000;
            }else if (ntAddr == 0x01){
                nameTableAddr = 0x2400;
            }else if (ntAddr == 0x10){
                nameTableAddr = 0x2800;
            }else if (ntAddr == 0x11){
                nameTableAddr = 0x2C00;
            }
            if(bgAddr == 1) {
                patternAddr = 0x1000;
            }
            this.renderNameTable(scanLineIndex,nameTableAddr,patternAddr,render);
        }
        //渲染精灵
        if(b2001[4] == 1) {
            short spritePatternAddr = (short) (b2000[3]==0 ? 0:0x1000);
            byte spriteSize = b2000[5];
            this.renderSprite(scanLineIndex,spritePatternAddr,spriteSize,render);
        }else if(b2001[3] == 0 && b2001[4] == 0) {
            //无背景和无精灵 渲染背景色
            short[] palette = ppuMemory.palettes[ppuMemory.read(0x3F00)];
            Arrays.fill(render,palette);
        }
        return render;
    }

    /**
     * 绘制命名表
     * @param scanLineIndex 扫描线
     * @param nametableStartAddr 命名表起始地址
     * @param patternStartAddr 图案表起始地址
     * @param render
     */
    private void renderNameTable(int scanLineIndex, short nametableStartAddr, short patternStartAddr, short[][] render) {
        //32*30个Tile = (256*240 像素)
        for (int i=0;i<32;i++) {
            //1 读取name table数据,其实就是Tile图案表索引  (图案+颜色 = 8字节+8字节=16字节)
            int nameTableData = (ppuMemory.read((nametableStartAddr + (scanLineIndex/8) * 32) + i)&0xFF) * 16;
            //2 读取图案,图案表起始地址+索引+具体渲染的8字节中的第几字节
            int patternAddr = patternStartAddr + nameTableData + (scanLineIndex % 8);
            int patternColor = patternAddr + 8;
            //图案表数据
            byte patternData = ppuMemory.read(patternAddr);
            //图案表颜色数据
            byte colorData = ppuMemory.read(patternColor);
            //取每像素的低两位颜色
            byte[] patternColorLowData = getPatternColorLowData(patternData,colorData);
            //取颜色高两位,属性表数据64byte,每32*32像素一个字节,每32条扫描线占用8字节
            byte attributeData = ppuMemory.read(nametableStartAddr + 0x3C0 + (scanLineIndex/32*8)+i/4);
            byte patternColorHighData = getPatternColorHighData(attributeData,i,scanLineIndex);
            byte p0 = ppuMemory.read(0x3F00);
            //合并 取最终4位颜色
            for (int i1 = 0; i1 <8; i1++) {
                int patternColorLowBit = patternColorLowData[7 - i1];
                //透明色 显示背景色
                if(patternColorLowBit == 0) {
                    render[i*8+i1] = ppuMemory.palettes[p0];
                }else {
                    int colorAddr = 0x3f00 + (((patternColorHighData << 2) & 0xF) | (patternColorLowBit & 0x3));
                    int paletteIndex = ppuMemory.read(colorAddr);
                    render[i*8+i1] = ppuMemory.palettes[paletteIndex];
                }
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
        int spriteHeight = spriteSize == 0?8:16;
        byte[] sprRam = ppuMemory.getSprRam();
        for (int i = 0; i < sprRam.length; i += 4) {
            short y = (short) ((sprRam[i]&0xff)+1);
            short patternIndex = (short) (sprRam[i+1]&0xff);
            byte attributeData = sprRam[i+2];
            byte backgroundPriority = (byte) ((attributeData>>5)&1);
            short x = (short) (sprRam[i+3]&0xff);
            if(sl >= y && sl <= y + spriteHeight) {
                //获取图案地址
                int spritePatternAddr = spritePatternStartAddr + patternIndex * 16 + (sl - y);
                //获取图案颜色数据
                byte patternData = ppuMemory.read(spritePatternAddr);
                byte colorData = ppuMemory.read(spritePatternAddr + 8);
                byte[] patternColorLowData = getPatternColorLowData(patternData,colorData);
                byte patternColorHighData = (byte) (attributeData & 0x03);
                //命中非透明背景
                if(patternData + colorData != 0) {
                    DataBus.p_2002[6] = 1;
                }
                for (int i1 = 0; i1 <spriteHeight; i1++) {
                    if(backgroundPriority == 0) {
                        //获取4位颜色
                        int colorAddr = 0x3f10 + (((patternColorHighData << 2) & 0xF) | ((patternColorLowData[7 - i1]) & 0x3));
                        if(patternData+colorData+patternColorHighData!=0 && colorAddr!=0x3f10) {
                            render[x + i1] = ppuMemory.palettes[ppuMemory.read(colorAddr)];;
                        }
                    }
                }
            }
        }

    }
    /**
     * 从属性表获取图案的高两位颜色
     * @param attributeData
     * @param i
     * @param line
     * @return
     */
    private byte getPatternColorHighData(byte attributeData,int i,int line) {
        byte[] attributeDatas = MemUtil.toBits(attributeData);
        int x = i % 4;
        int y = line % 32 / 8;
        byte high2 = 0;
        if(y<2) {
            if(x<2) {
                high2 = (byte) ((attributeDatas[0]) + (attributeDatas[1]<<1));
            }else {
                high2 = (byte) ((attributeDatas[2]) + (attributeDatas[3]<<1));
            }
        }else {
            if(x<2) {
                high2 = (byte) ((attributeDatas[4]) + (attributeDatas[5]<<1));
            }else {
                high2 = (byte) ((attributeDatas[6]) + (attributeDatas[7]<<1));
            }
        }
        return high2;
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
}
