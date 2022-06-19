//
// Created by WStars on 2022/6/18.
//

#ifndef NES_C_PPU_H
#define NES_C_PPU_H

struct Ppu {
    char reg_2000;
    char reg_2001;
    char reg_2002;
    char reg_2003;
    char write_toggle;
    char reg_2007;
    char scroll_x;
    short vram_addr;
    short vram_temp_addr;
}static ppu;

#endif //NES_C_PPU_H

void render_nametable(short scanLineIndex);

void render_sprite(short scanLineIndex);

char p_read(short addr);
char p_read(short addr);
