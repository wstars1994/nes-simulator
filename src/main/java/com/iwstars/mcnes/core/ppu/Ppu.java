package com.iwstars.mcnes.core.ppu;

import com.iwstars.mcnes.core.DataBus;
import com.iwstars.mcnes.util.MemUtil;
import lombok.Getter;

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
     * 初始化PPU 必须设置,从nes文件读取到的CHR数据
     * @param patternData 图案表数据
     */
    public Ppu(byte[] patternData){
        ppuMemory = new PpuMemory();
        ppuMemory.writePattern(patternData);
    }

    /**
     * 开始绘制扫描线
     */
    public short[][] preRender(int line) {
        short[][] render = new short[256][3];

        byte[] b2000 = DataBus.p_2000;
        byte[] b2001 = DataBus.p_2001;
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
            this.renderNameTable(line,nameTableAddr,patternAddr,render);
        }
        if(b2001[3] == 0 && b2001[4] == 0) {
            render = new short[256][3];
            byte read = ppuMemory.read(0x3F10);
            short[] palette = ppuMemory.palettes[read==0?0:read-1];
            for(int i=0;i<256;i++) {
                render[i] = palette;
            }
        }
        return render;
    }

    /**
     * 绘制命名表
     * @param line 扫描线
     * @param nametableStartAddr 命名表起始地址
     * @param patternStartAddr 图案表起始地址
     * @param render
     */
    private void renderNameTable(int line, short nametableStartAddr, short patternStartAddr, short[][] render) {
        //计算每条扫描线 起始的命名表地址
        //line/8 每8条扫描线命名表地址 ++32(每8条扫描线后才读取完32个8*8tile)
        nametableStartAddr = (short) (nametableStartAddr + (line/8) * 32);
        //32*30个Tile = (256*240 像素)
        for (int i=0;i<32;i++) {
            //1 读取name table数据,其实就是Tile图案表索引  (图案+颜色 = 8字节+8字节=16字节)
            int nameTableData = (ppuMemory.read(nametableStartAddr + i)&0xFF) * 16;
            //2 读取图案,图案表起始地址+索引+具体渲染的8字节中的第几字节
            int patternAddr = patternStartAddr + nameTableData + (line%8);
            int patternColor = patternAddr + 8;
            //图案表数据
            byte patternData = ppuMemory.read(patternAddr);

            char[] bytes = print8(patternData).toCharArray();

            //图案表颜色数据
            byte colorData = ppuMemory.read(patternColor);
            //取每像素的低两位颜色
            byte[] patternColorLowData = getPatternColorLowData(patternData,colorData);
            //属性表数据 (取颜色高两位)
            byte attributeData = (byte) (ppuMemory.read(nametableStartAddr + 0x3C0 + (i/4))&0xFF);
            byte patternColorHighData = getPatternColorHighData(attributeData,i,line);
            //合并 取最终4位颜色
            for (int i1 = 0; i1 <8; i1++) {
//                byte color = (byte) (((patternColorHighData << 2) & 0xF) | (patternColorLowData[i1] & 0x3));
                byte color = (byte) Integer.parseInt(bytes[i1]+"");
                short[] paletteColor = {0,0,0};
                if(color!=0){
                    paletteColor[0] = 255;
                    paletteColor[1] = 255;
                    paletteColor[2] = 255;
                }
                render[i*8+i1] = paletteColor;
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
        int ii = i % 4;
        int ll = line % 4;
        byte high2 = 0;
        if(ll<2) {
            if(ii<2) {
                high2 = (byte) ((attributeDatas[0]) + (attributeDatas[1] & 2));
            }else {
                high2 = (byte) ((attributeDatas[2]) + (attributeDatas[3] & 2));
            }
        }else {
            if(ii<2) {
                high2 = (byte) ((attributeDatas[4]) + (attributeDatas[5] & 2));
            }else {
                high2 = (byte) ((attributeDatas[6]) + (attributeDatas[7] & 2));
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
        byte[] patternDatas = MemUtil.toBits((byte) (patternData&0xFF));
        byte[] colorDatas = MemUtil.toBits((byte) (colorData&0xFF));

        byte patternColorData[] = new byte[8];
        for(int i=7;i>0;i--) {
            patternColorData[i] = (byte) ((colorDatas[i]<<1) | (patternDatas[i]&1));
        }
        return patternColorData;
    }

    private String print8(byte data){
        String s = Integer.toBinaryString(data & 0xFF);
        while ( s.length() < 8 ) {
            s = "0" + s;
        }
//        System.out.print(s);
        return s;
    }
}
