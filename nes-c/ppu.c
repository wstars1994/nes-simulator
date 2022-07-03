//
// Created by WStars on 2022/6/18.
//
#include "include/ppu.h"
#include "include/mem.h"

short palettes[65] = {29614, 8401, 21, 16403, -30706, -22526, -24576, 30784, 16736, 544, 640, 482, 6635, 0, 32, 32,
                      -16905, 925, 8669, -32738, -18409, -8181, -9920, -13727, -29824, 1184, 1344, 1159, 1041, 4226,
                      2113, 2113, -1, 15871, 23743, -23457, -3105, -1098, -1108, -825, -2585, -31102, 20201, 24531,
                      1883, 25388, 2145, 2145, -1, -20673, -14657, -10657, -449, -453, -522, -299, -204, -6156, -20585,
                      -18439, -24578, -8453, 4226, 4226, 0,};
//渲染数据
extern int render[(256+16)*240];
//PPU 精灵数据
extern char p_spr_ram[256];

void render_nametable(short line) {
    short p_vram_addr = ppu.vram_addr;
    char fine_y = (char) ((p_vram_addr >> 12) & 7);
    char fine_x = ppu.scroll_x;
    short patternStartAddr = (short) ((get_bit(ppu.reg_2000,4) == 0 ? 0x0000 : 0x1000)+fine_y);
    char bgColorIndex = p_read(0x3F00);
    short nameTableAddress  = (short) (0x2000 | (p_vram_addr & 0xFFF));
    for (int i=0;i<32;i++) {
        //指示哪个tile
        char coarse_x = (char) (nameTableAddress&0x1F);
        char coarse_y = (char) ((nameTableAddress>>5)&0x1F);
        //1 读取name table数据,其实就是Tile图案表索引  (图案+颜色 = 8字节+8字节=16字节)
        char nameTableData = p_read(nameTableAddress);
        //2 读取图案,图案表起始地址+索引+具体渲染的8字节中的第几字节
        int patternAddress = patternStartAddr + (nameTableData&0xff) * 16;
        //图案表数据
        char patternData = p_read(patternAddress);
        //图案表颜色数据
        char patternColorData = p_read(patternAddress + 8);
        //取颜色低两位
        int attributeOffset = 0;
        int attributeAddress = 0x23C0 | (nameTableAddress & 0x0C00);
        if(coarse_x != 0 || coarse_y != 0){
            attributeOffset = ((coarse_y & 2) == 0 ? 0 : 4) + ((coarse_x & 2) == 0 ? 0 : 2);
            attributeAddress = attributeAddress | ((coarse_y>>2)<<3) | (coarse_x >> 2);
        }
        //取颜色高两位,属性表数据64char,每32*32像素一个字节,每32条扫描线占用8字节
        char pchb = (char) ((p_read(attributeAddress)>>attributeOffset)&3);
        //合并 取最终4位颜色
        for (int j=0; j<8; j++){
            int pclb = ((patternData & 0x80)>>7) | (((patternColorData & 0x80)>>7) << 1);
            int index = line * 256 + i * 8 + j - fine_x;
            if(pclb!=0) {
                int colorAddr = 0x3f00 + (pchb<<2|(pclb&0x3));
                int paletteIndex = p_read(colorAddr);
                render[index] = palettes[paletteIndex];
            }else{
                render[index] = palettes[bgColorIndex&0xff];
            }
            patternData <<= 1;
            patternColorData <<= 1;
        }
        // if coarse X == 31 (coarseX的最大值就是31即11111B,所以到最大值了要切换到下一个nametable)
        if ((nameTableAddress & 0x1F) == 0x1F) {
            nameTableAddress = (short) ((nameTableAddress & ~0x1f) ^ 0x400);
        } else {
            nameTableAddress++;
        }
    }
}

void render_sprite(short line){
    char bgColorIndex = p_read(0x3F00);
    int palette = palettes[bgColorIndex & 0xff];
    char spriteHeight = (char) (get_bit(ppu.reg_2000,5) == 0?8:16);
    short spritePatternStartAddr = (short) (get_bit(ppu.reg_2000,3)==0 ? 0:0x1000);
    if(spriteHeight!=0){
        //获取内存中的精灵数据
        for (int i = 0; i < 256; i += 4) {
            short y = (short) ((p_spr_ram[i]&0xff)+1);
            short patternIndex = (short) (p_spr_ram[i+1]&0xff);
            if(line < y || line >= y + spriteHeight) {
                continue;
            }
            //子图形数据
            char attributeData = p_spr_ram[i+2];
            //背景层级
            char backgroundPriority = (char) ((attributeData>>5)&1);
            //图案垂直翻转
            char vFlip = (char) ((attributeData>>7)&1);
            //图案水平翻转
            char hFlip = (char) ((attributeData>>6)&1);
            short sprX = (short) (p_spr_ram[i+3]&0xff);
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
            char spritePatternData = p_read(spritePatternAddr);
            //获取图案颜色数据
            char colorData = p_read(spritePatternAddr + 8);
            //命中非透明背景 sprite#0 hit
            if(i==0&&(spritePatternData&colorData)!=0) {
                set_bit(&ppu.reg_2002,1,6);
            }
            char colorHigh = (char) ((attributeData & 0x03)<<2);
            //水平翻转 01234567 -> 76543210
            int x = 0,x2 = 8,x3=1;
            if(hFlip == 1) {
                x = 7; x2 = -1; x3 = -1;
            }
            for (;x!=x2; x+=x3) {
                int colorLow = ((spritePatternData & 0x80)>>7) | (((colorData & 0x80)>>7) << 1);
                int shorts = render[line * 256 + sprX + x];
                if(colorLow != 0 && (backgroundPriority == 0||shorts==palette)){
                    //获取4位颜色
                    int colorAddr = 0x3f10 | colorHigh | colorLow;
                    if(colorAddr != 0x3f10){
                        render[line*256+ sprX + x] = palettes[p_read(colorAddr)];
                    }
                }
                spritePatternData<<=1;
                colorData<<=1;
            }
        }
    }
}