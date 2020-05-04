package com.iwstars.mcnes.core.ppu;

import com.iwstars.mcnes.core.DataBus;
import com.iwstars.mcnes.util.LogUtil;
import com.iwstars.mcnes.util.MemUtil;
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
    public void startRender(int line) {
        LogUtil.logLn("--------------------Render begin--------------------");
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
            this.renderNameTable(line,nameTableAddr,patternAddr);
        }
        if(b2001[4] == 1) {

        }
        LogUtil.logLn("--------------------Render end--------------------");
    }

    /**
     * 绘制命名表
     * @param line 扫描线
     * @param nametableStartAddr 命名表起始地址
     * @param patternStartAddr 图案表起始地址
     */
    private void renderNameTable(int line,short nametableStartAddr,short patternStartAddr) {
        //32*30个Tile = (256*240 像素)
        for (int i=0;i<32;i++) {
            int patternAddr = patternStartAddr + (PpuMemory.read(nametableStartAddr + i + (line*32)) * 16);
            int patternColor = patternAddr + 8;
            //图案表数据
            byte patternData = PpuMemory.read(patternAddr);
            //图案表颜色数据
            byte colorData = PpuMemory.read(patternColor);
            //合并取低两位颜色
            byte[] patternColorData = getPatternColorData(patternData,colorData);
            //取属性表数据 (颜色高两位)
            byte attributeData = PpuMemory.read(nametableStartAddr + 0x3C0 + (i/4));
            byte[] attributeDatas = MemUtil.toBits(attributeData);
            int ii = i % 4;
            int ll = line % 4;
            int high2 = 0;
            if(ll<2) {
                if(ii<2) {
                    high2 = (attributeDatas[0]) + (attributeDatas[1] & 2);
                }else {
                    high2 = (attributeDatas[2]) + (attributeDatas[3] & 2);
                }
            }else {
                if(ii<2) {
                    high2 = (attributeDatas[4]) + (attributeDatas[5] & 2);
                }else {
                    high2 = (attributeDatas[6]) + (attributeDatas[7] & 2);
                }
            }

//            print8(MemUtil.bitsToByte(patternColorData));
        }
//        System.out.println("");

    }

    private byte[] getPatternColorData(byte patternData, byte colorData) {
        byte[] patternDatas = MemUtil.toBits((byte) (patternData&0xFF));
        byte[] colorDatas = MemUtil.toBits((byte) (colorData&0xFF));

        byte patternColorData[] = new byte[8];
        for(int i=7;i>0;i--) {
            patternColorData[i] = (byte) ((colorDatas[i]<<1) | (patternDatas[i]));
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
