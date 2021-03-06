package com.wxclog.core.ppu;

import com.wxclog.core.Const;
import com.wxclog.util.LogUtil;

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

    private byte[] ppuData = new byte[65536];
    /**
     * 精灵数据
     */
    private byte[] sprRam = new byte[256];

    private byte mirroringType;

    public PpuMemory(byte[] patternData,byte mirroringType){
        if(patternData.length>0x2000){
            System.arraycopy(patternData,0,ppuData,0,0x2000);
        }else{
            System.arraycopy(patternData,0,ppuData,0,patternData.length);
        }
        this.mirroringType = mirroringType;
    }

    /**
     * 调色板 http://nesdev.com/NESDoc.pdf (Appendix F)
     */
//    public short[][] palettes = {
//            { 0x75, 0x75, 0x75 },{ 0x27, 0x1B, 0x8F },{ 0x00, 0x00, 0xAB },{ 0x47, 0x00, 0x9F },{ 0x8F, 0x00, 0x77 },{ 0xAB, 0x00, 0x13 },{ 0xA7, 0x00, 0x00 },{ 0x7F, 0x0B, 0x00 },
//            { 0x43, 0x2F, 0x00 },{ 0x00, 0x47, 0x00 },{ 0x00, 0x51, 0x00 },{ 0x00, 0x3F, 0x17 },{ 0x1B, 0x3F, 0x5F },{ 0x00, 0x00, 0x00 },{ 0x05, 0x05, 0x05 },{ 0x05, 0x05, 0x05 },
//            { 0xBC, 0xBC, 0xBC },{ 0x00, 0x73, 0xEF },{ 0x23, 0x3B, 0xEF },{ 0x83, 0x00, 0xF3 },{ 0xBF, 0x00, 0xBF },{ 0xE7, 0x00, 0x5B },{ 0xDB, 0x2B, 0x00 },{ 0xCB, 0x4F, 0x0F },
//            { 0x8B, 0x73, 0x00 },{ 0x00, 0x97, 0x00 },{ 0x00, 0xAB, 0x00 },{ 0x00, 0x93, 0x3B },{ 0x00, 0x83, 0x8B },{ 0x11, 0x11, 0x11 },{ 0x09, 0x09, 0x09 },{ 0x09, 0x09, 0x09 },
//            { 0xFF, 0xFF, 0xFF },{ 0x3F, 0xBF, 0xFF },{ 0x5F, 0x97, 0xFF },{ 0xA7, 0x8B, 0xFD },{ 0xF7, 0x7B, 0xFF },{ 0xFF, 0x77, 0xB7 },{ 0xFF, 0x77, 0x63 },{ 0xFF, 0x9B, 0x3B },
//            { 0xF3, 0xBF, 0x3F },{ 0x83, 0xD3, 0x13 },{ 0x4F, 0xDF, 0x4B },{ 0x58, 0xF8, 0x98 },{ 0x00, 0xEB, 0xDB },{ 0x66, 0x66, 0x66 },{ 0x0D, 0x0D, 0x0D },{ 0x0D, 0x0D, 0x0D },
//            { 0xFF, 0xFF, 0xFF },{ 0xAB, 0xE7, 0xFF },{ 0xC7, 0xD7, 0xFF },{ 0xD7, 0xCB, 0xFF },{ 0xFF, 0xC7, 0xFF },{ 0xFF, 0xC7, 0xDB },{ 0xFF, 0xBF, 0xB3 },{ 0xFF, 0xDB, 0xAB },
//            { 0xFF, 0xE7, 0xA3 },{ 0xE3, 0xFF, 0xA3 },{ 0xAB, 0xF3, 0xBF },{ 0xB3, 0xFF, 0xCF },{ 0x9F, 0xFF, 0xF3 },{ 0xDD, 0xDD, 0xDD },{ 0x11, 0x11, 0x11 },{ 0x11, 0x11, 0x11 }
//            ,{ 0x00, 0x00, 0x00 }
//    };
    public int palettes[] = {-9079435,- 14214257,-16777045,-12124001,-7405449,-5570541,-5832704,-8451328,-12374272,
            -16759040,-16756480,-16761065,-14991521,0,-16448251,-16448251,-4408132,-16747537,-14468113,-8191757,
            -4259649,-1638309,-2413824,-3453169,-7638272,-16738560,-16733440,-16739525,-16743541,-15658735,-16185079,
            -16185079,
            -1,
            -12599297,
            -10512385,
            -5796867,
            -558081,
            -34889,
            -34973,
            -25797,
            -803009,
            -8137965,
            -11542709,
            -10946408,
            -16716837,
            -10066330,
            -15921907,
            -15921907,
            -1,
            -5511169,
            -3680257,
            -2634753,
            -14337,
            -14373,
            -16461,
            -9301,
            -6237,
            -1835101,
            -5508161,
            -4980785,
            -6291469,
            -2236963,
            -15658735,
            -15658735,
            0};

    public void write(int addr, byte data) {
        LogUtil.logf(" | PWR:[addr:%d INDEX:%d DATA:%d]",addr&0xFFFF,addr&0xFFFF,data);
        //写入 $3F00-3FFF 的 D7-D6 字节被忽略.
        if(addr >= 0x3F00 && addr <= 0x3FFF) {
            data = (byte) (data & 0x3f);
            if(addr==0x3F00){
                ppuData[0x3F10] = data;
            }else if(addr==0x3F10){
                ppuData[0x3F00] = data;
            }
        }
        ppuData[addr] = data;
    }

    public byte read(int addr) {
        switch (this.mirroringType){
            //H
            case 0:
                if(addr>=0x2400&&addr<0x2800 || addr>=0x2C00&&addr<0x3000){
                    addr-=0x400;
                }
                break;
            //V
            case 1:
                if(addr>=0x2800&&addr<0x3000){
                    addr-=0x800;
                }
                break;
        }
        return Const.mapper.readPpu(addr);
    }

    /**
     * 写入精灵数据
     * @param addr
     * @param data
     */
    public void writeSprRam(byte addr, byte data) {
        this.sprRam[addr&0xFF] = data;
    }

    /**
     * 获取精灵数据
     * @return
     */
    public byte[] getSprRam() {
        return sprRam;
    }

    public byte readMem(int addr) {
        return this.ppuData[addr];
    }
}
