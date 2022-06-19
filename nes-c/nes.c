//
// Created by WStars on 2022/6/18.
//
#include <malloc.h>
#include "stdio.h"

#include "include/nes.h"
#include "include/cpu6502.h"
#include "include/ppu.h"
#include "include/mem.h"
#include "include/render.h"

//程序数据
extern char *prg_data;
//图案数据
extern char *chr_data;


struct Card{
    char magic[3];
    char magicEof;
    char prgCount;
    char chrCount;
    char control1;
    char control2;
    char zero[2];
}cardData;

void load_card(){
    FILE *fp;
    if((fp = fopen("../mario.nes", "rb")) == NULL){
        printf("can not open file");
        return;
    }
    fread(cardData.magic, 1, 3, fp);
    printf("Magic: %c%c%c\n",cardData.magic[0],cardData.magic[1],cardData.magic[2]);
    fread(&cardData.magicEof, 1, 1, fp);
    fread(&cardData.prgCount, 1, 1, fp);
    fread(&cardData.chrCount, 1, 1, fp);
    printf("prgCount: %d,chrCount: %d\n",cardData.prgCount,cardData.chrCount);
    fread(&cardData.control1, 1, 1, fp);
    fread(&cardData.control2, 1, 1, fp);
    fread(&cardData.zero, 8, 1, fp);
    //读取程序
    prg_data = (char *)malloc(cardData.prgCount * 1024 * 16);
    chr_data = (char *)malloc(cardData.chrCount * 1024 * 8);
    fread(prg_data, cardData.prgCount * 1024 * 16, 1, fp);
    fread(chr_data, cardData.chrCount * 1024 * 8, 1, fp);
}

int showBg() {
    return get_bit(ppu.reg_2001, 3);
}

int showSpr() {
    return get_bit(ppu.reg_2001, 4);
}

void coarseY() {
    if (showBg() || showSpr()) {
        // if fine Y < 7
        if ((ppu.vram_addr & 0x7000) != 0x7000) {
            // increment fine Y
            ppu.vram_addr += 0x1000;
        } else {
            // fine Y = 0
            ppu.vram_addr &= ~0x7000;
            // let y = coarse Y
            int y = (ppu.vram_addr & 0x03E0) >> 5;
            if (y == 29) {
                // coarse Y = 0
                y = 0;
                // switch vertical nametableelse if (y == 31)
                ppu.vram_addr ^= 0x0800;
            } else if (y == 31) {
                // coarse Y = 0, nametable not switched
                y = 0;
            } else {
                // increment coarse Y
                y += 1;
            }
            // put coarse Y back into v
            ppu.vram_addr = (ppu.vram_addr & ~0x03E0) | (y << 5);
        }
    }
}

/**
 * 开始绘制扫描线
 */
void ppu_render(short scanLineIndex) {
    //渲染背景
    if(showBg()) {
        render_nametable(scanLineIndex);
    }
    //渲染精灵
    if(showSpr()) {
        render_sprite(scanLineIndex);
    }
}

void nes_start(){
    load_card();
    cpu_init();

    while (1) {
        for (int l = 0; l < 240; l++) {
            if (showBg() || showSpr()) {
                ppu.vram_addr = (ppu.vram_addr & 0xfbe0) | (ppu.vram_temp_addr & 0x041f);
            }
            ppu_render(l);
            cpu_go();
            coarseY();
        }
        if ((showBg() || showSpr())) {
            render_windows();
        }
        //设置vblank true
        set_bit(&ppu.reg_2002, 1, 7);
        set_bit(&ppu.reg_2002, 0, 6);
        cpu_go();
        //NMI中断
        interrupt_nmi(ppu.reg_2000);
        //242-260
        for (int i = 242; i < 262; i++) {
            cpu_go();
        }
        set_bit(&ppu.reg_2002, 0, 7);
        //vblank结束后 如果有渲染 将t复制到v
        if (showBg() || showSpr()) {
            ppu.vram_addr = ppu.vram_temp_addr;
        }
    }
}