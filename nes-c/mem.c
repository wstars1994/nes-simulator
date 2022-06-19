//
// Created by WStars on 2022/6/18.
//
#include "stdio.h"

#include "include/mem.h"
#include "include/ppu.h"
//程序数据
char *prg_data;
//图案数据
char *chr_data;
//PPU 精灵数据
char p_spr_ram[256];
//渲染数据
int render[(256+16)*240];

//PPU内存
char p_mem[0x2000];
//CPU内存
char c_mem[0x800];

char read_program(int addr) {
    if (addr < 0x8000) {
        return c_mem[addr];
    } else {
        return prg_data[addr - 0x8000];
    }
}

char get_bit(char data, char index) {
    return (data >> index) & 1;
}
void set_bit(char *data, char set_data, char index) {
    if (set_data) {
        *data |= 1 << index;
    } else {
        *data &= ~(1 << index);
    }
}

//PPU 内存操作----------------------------------
void p_write_spr(char addr, char data) {
    p_spr_ram[addr & 0xFF] = data;
}

char p_read(short addr) {
    if (addr < 0x2000) {
        return prg_data[addr];
    }
    return p_mem[addr - 0x2000];
}

void p_write(short addr, char data) {
    //写入 $3F00-3FFF 的 D7-D6 字节被忽略.
    if (addr >= 0x3F00 && addr <= 0x3FFF) {
        data = data & 0x3f;
        if (addr == 0x3F00) {
            p_mem[0x3F10 - 0x2000] = data;
        } else if (addr == 0x3F10) {
            p_mem[0x3F00 - 0x2000] = data;
        }
    }
    //printf(" | PWR:[addr:%02X INDEX:%d DATA:%d]",addr&0xFFFF,addr&0xFFFF,data);
    p_mem[addr - 0x2000] = data;
}


//内存操作----------------------------------
char read(int addr) {
    printf(" | RD:[addr:%02X INDEX:%d]",addr,addr);
    char ret_p_reg_2002;
    int temp_p_reg_2006;
    switch (addr) {
        //读PPUSTATUS状态寄存器
        case 0x2002:
            ret_p_reg_2002 = ppu.reg_2002;
            set_bit(&ppu.reg_2002, 0, 7);
            ppu.write_toggle = 0;
            return ret_p_reg_2002;
        case 0x2007:
            temp_p_reg_2006 = ppu.vram_addr & 0x3fff;
            ppu.vram_addr += get_bit(ppu.reg_2000, 2) ? 32 : 1;
            if (addr <= 0x3EFF) {
                //读取PPU
                char res = ppu.reg_2007;
                ppu.reg_2007 = p_read(temp_p_reg_2006);
                return res;
            }
            break;
        default:
            return read_program(addr);
    }
}

void write(int addr, char data) {
    printf(" | WR:[ADDR:%02X INDEX:%d DATA:%d]",addr&0xFFFF,addr&0xFFFF,data);
    short start;
    switch (addr) {
        case 0x2000:
            ppu.reg_2000 = data;
            ppu.vram_temp_addr = (ppu.vram_temp_addr & 0xf3ff) | ((data & 3) << 10);
            break;
        case 0x2001:
            ppu.reg_2001 = data;
            break;
        case 0x2003:
            ppu.reg_2003 = data;
            break;
        case 0x2004:
            ppu.reg_2003 += 1;
            p_write_spr(ppu.reg_2003, data);
            break;
        case 0x2005:
            if (!ppu.write_toggle) {
                //设置fine_x
                ppu.scroll_x = data & 0x7;
                //设置coarse_x
                ppu.vram_temp_addr &= ~0x1F;
                ppu.vram_temp_addr |= ((data & 0xff) >> 3) & 0x1F;
            } else {
                //设置coarse_y
                ppu.vram_temp_addr &= ~0x3E0;
                ppu.vram_temp_addr |= (((data & 0xff) >> 3) << 5);
                //设置fine_y
                ppu.vram_temp_addr &= ~0x7000;
                ppu.vram_temp_addr |= (data & 0x7) << 12;
            }
            ppu.write_toggle = !ppu.write_toggle;
            break;
        case 0x2006:
            if (!ppu.write_toggle) {
                //高6位置0 PPU高7/8位无效 置0
                ppu.vram_temp_addr &= ~(0xFF << 8);
                //第一次写将写入高6位;
                ppu.vram_temp_addr |= ((data & 0x3F) << 8);
            } else {
                //低8位置0
                ppu.vram_temp_addr &= ~0xFF;
                //第二次写将写入低8位
                ppu.vram_temp_addr |= data & 0xFF;
                ppu.vram_addr = ppu.vram_temp_addr;
            }
            ppu.write_toggle = !ppu.write_toggle;
            break;
        case 0x2007:
            p_write(ppu.vram_addr, data);
            ppu.vram_addr += get_bit(ppu.reg_2000, 2) ? 32 : 1;
            break;
            //OAM DMA register (high char)
        case 0x4014:
            start = data << 8;
            for (int i = 0; i < 256; i++) {
                char readData = read(start++);
                p_write_spr(i, readData);
            }
            break;
        default:
            c_mem[addr & 0xFFFF] = data;
    }
}