package com.wxclog.core.ppu;

import com.wxclog.core.DataBus;

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
public class Ppu {

    private PpuMemory ppuMemory;

    /**
     * 初始化PPU  从nes文件读取到的CHR数据
     * @param patternData 图案表数据
     */
    public Ppu(byte[] patternData,byte mirroringType){
        ppuMemory = new PpuMemory(patternData,mirroringType);
    }

    public PpuMemory getPpuMemory() {
        return ppuMemory;
    }

    /**
     * 开始绘制扫描线
     */
    public void preRender(int scanLineIndex,short[][] frameData,byte spriteScanData[][]) {
        //渲染背景
        if(DataBus.showBg()) {
            this.renderNameTable(scanLineIndex,frameData);
        }
        //渲染精灵
        if(DataBus.showSpr()) {
            this.renderSpriteData(scanLineIndex,spriteScanData);
        }
    }

    /**
     * 保存命名表
     * 内存空间压缩
     * @see <a href="https://wiki.nesdev.org/w/index.php?title=PPU_scrolling">PPU_scrolling</a>
     * @return
     */
    private short[][] renderNameTable(int scanLineIndex,short scanData[][]) {
        scanData[scanLineIndex][0] = DataBus.p_vram_addr;
        scanData[scanLineIndex][1] = DataBus.p_scroll_x;
        scanData[scanLineIndex][2] = (short) (DataBus.p_2000[4] == 0 ?0x0000:0x1000);
        return scanData;
    }

    public void renderNameTable(short scanData[][],int[] render) {
        byte bgColorIndex = ppuMemory.read(0x3F00);
        for (int line = 0;line < 240;line++){
            byte fine_x = (byte) scanData[line][1];
            byte fine_y = (byte) ((scanData[line][0] >> 12) & 7);
            short nameTableAddress  = (short) (0x2000 | (scanData[line][0] & 0xFFF));
            short patternStartAddr = (short) (scanData[line][2] + fine_y);
            for (int i=0;i<33;i++) {
                //指示哪个tile
                byte coarse_x = (byte) (nameTableAddress&0x1F);
                byte coarse_y = (byte) ((nameTableAddress>>5)&0x1F);
                //1 读取name table数据,其实就是Tile图案表索引  (图案+颜色 = 8字节+8字节=16字节)
                byte nameTableData = ppuMemory.read(nameTableAddress);
                //2 读取图案,图案表起始地址+索引+具体渲染的8字节中的第几字节
                int patternAddress = patternStartAddr + (nameTableData&0xff) * 16;
                //图案表数据
                byte patternData = ppuMemory.read(patternAddress);
                //图案表颜色数据
                byte colorData = ppuMemory.read(patternAddress + 8);
                //取颜色低两位
                if(patternData == 0 && colorData==0) {
                    for (int j=0; j<8; j++) {
                        int index = line * 256 + i * 8 + j - fine_x;
                        if(index<0){
                            index = 0;
                        }
                        render[index] = ppuMemory.palettes[bgColorIndex&0xff];
                    }
                }else{
                    int attributeOffset = 0;
                    int attributeAddress = 0x23C0 | (nameTableAddress & 0x0C00);
                    if(coarse_x != 0 || coarse_y != 0){
                        //取颜色高两位,属性表数据64byte,每32*32像素一个字节,每32条扫描线占用8字节
                        attributeOffset = ((coarse_y & 2) == 0 ? 0 : 4) + ((coarse_x & 2) == 0 ? 0 : 2);
                        attributeAddress = attributeAddress | ((coarse_y>>2)<<3) | (coarse_x >> 2);
                    }
                    byte pchb = (byte) ((ppuMemory.read(attributeAddress)>>attributeOffset)&3);
                    //合并 取最终4位颜色
                    for (int j=0; j<8; j++){
                        int pclb = ((patternData & 0x80)>>7) | (((colorData & 0x80)>>7) << 1);
                        int index = line * 256 + i * 8 + j - fine_x;
                        if(index<0){
                            index = 0;
                        }
                        if(pclb!=0) {
                            int colorAddr = 0x3f00 + (pchb<<2|(pclb&0x3));
                            int paletteIndex = ppuMemory.read(colorAddr);
                            render[index] = ppuMemory.palettes[paletteIndex];
                        }else{
                            render[index] = ppuMemory.palettes[bgColorIndex&0xff];
                        }
                        patternData <<= 1;
                        colorData <<= 1;
                    }
                }
                // if coarse X == 31 (coarseX的最大值就是31即11111B,所以到最大值了要切换到下一个nametable)
                if ((nameTableAddress & 0x1F) == 0x1F) {
                    nameTableAddress = (short) ((nameTableAddress & ~0x1f) ^ 0x400);
                } else {
                    nameTableAddress++;
                }
            }
        }
    }

    /**
     * 渲染精灵
     */
    private void renderSpriteData(int sl,byte spriteScanData[][]) {
        //获取精灵高度
        byte spriteHeight = (byte) (DataBus.p_2000[5] == 0?8:16);
        short spritePatternStartAddr = (short) (DataBus.p_2000[3]==0 ? 0:0x1000);
        //获取内存中的精灵数据
        byte[] sprRam = ppuMemory.getSprRam();
        for (int i = 0; i < sprRam.length; i += 4) {
            short y = (short) ((sprRam[i]&0xff)+1);
            short patternIndex = (short) (sprRam[i+1]&0xff);
            //子图形数据
            byte attributeData = sprRam[i+2];
            //图案垂直翻转
            byte vFlip = (byte) ((attributeData>>7)&1);
            if(sl >= y && sl < y + spriteHeight) {
                spriteScanData[sl][0] = spriteHeight;
                spriteScanData[sl][1] = DataBus.p_2000[3];
                int offset = sl - y;
                //垂直翻转
                if(vFlip == 1){
                    offset = spriteHeight-offset-1;
                }
                //获取图案地址
                int spritePatternAddr = spritePatternStartAddr + patternIndex * 16 + offset;
                if(spriteHeight == 16) {
                    spritePatternAddr = (patternIndex & ~1) * 16 + ((patternIndex & 1) * 0x1000) + (offset >= 8 ? 16 : 0) + (offset & 7);
                }
                byte spritePatternData = ppuMemory.read(spritePatternAddr);
                //获取图案颜色数据
                byte colorData = ppuMemory.read(spritePatternAddr + 8);
                //命中非透明背景 sprite0 hit
                if(i==0&&spritePatternData+colorData!=0) {
                    DataBus.p_2002[6] = 1;
                }
            }
        }
    }

    public void renderSprite(int[] render,byte spriteScanData[][]) {
        byte bgColorIndex = ppuMemory.read(0x3F00);
        int palette = ppuMemory.palettes[bgColorIndex & 0xff];
        for (int line=0;line<240;line++){
            byte spriteHeight = spriteScanData[line][0];
            short spritePatternStartAddr = (short) (spriteScanData[line][1]==0 ? 0:0x1000);
            if(spriteHeight!=0){
                //获取内存中的精灵数据
                byte[] sprRam = ppuMemory.getSprRam();
                for (int i = 0; i < sprRam.length; i += 4) {
                    short y = (short) ((sprRam[i]&0xff)+1);
                    short patternIndex = (short) (sprRam[i+1]&0xff);
                    if(line < y || line >= y + spriteHeight) {
                        continue;
                    }
                    //子图形数据
                    byte attributeData = sprRam[i+2];
                    //背景层级
                    byte backgroundPriority = (byte) ((attributeData>>5)&1);
                    //图案垂直翻转
                    byte vFlip = (byte) ((attributeData>>7)&1);
                    //图案水平翻转
                    byte hFlip = (byte) ((attributeData>>6)&1);
                    short sprX = (short) (sprRam[i+3]&0xff);
                    int offset = line - y;
                    //垂直翻转
                    if(vFlip == 1){
                        offset = spriteHeight-offset-1;
                    }
                    //获取图案地址
                    int spritePatternAddr = spritePatternStartAddr + patternIndex * 16 + offset;
                    if(spriteHeight == 16) {
                        spritePatternAddr = (patternIndex & ~1) * 16 + ((patternIndex & 1) * 0x1000) + (offset >= 8 ? 16 : 0) + (offset & 7);
                    }
                    byte spritePatternData = ppuMemory.read(spritePatternAddr);
                    //获取图案颜色数据
                    byte colorData = ppuMemory.read(spritePatternAddr + 8);
                    if(spritePatternData == 0 && colorData==0){
                        continue;
                    }
                    byte colorHigh = (byte) ((attributeData & 0x03)<<2);
                    //水平翻转 01234567 -> 76543210
                    int x = 0,x2 = 8,x3=1;
                    if(hFlip == 1) {
                        x = 7; x2 = -1; x3 = -1;
                    }
                    for (;x!=x2; x+=x3) {
                        int colorLow = ((spritePatternData & 0x80)>>7) | (((colorData & 0x80)>>7) << 1);
                        int shorts = render[line * 256 + sprX + x];
                        boolean i1 = shorts==palette;
                        if(colorLow != 0 && (backgroundPriority == 0||i1)){
                            //获取4位颜色
                            int colorAddr = 0x3f10 | colorHigh | colorLow;
                            if(colorAddr != 0x3f10){
                                render[line*256+ sprX + x] = ppuMemory.palettes[ppuMemory.read(colorAddr)];
                            }
                        }
                        spritePatternData<<=1;
                        colorData<<=1;
                    }
                }
            }
        }

    }
}
