//
// Created by WStars on 2022/6/18.
//

#ifndef NES_C_CPU6502_H
#define NES_C_CPU6502_H

//寄存器
struct Reg {
    char a;
    char x;
    char y;
    char s;
};
/**
 * 标志位
 */
struct Flag {
    char n;
    char v;
    char b;
    char d;
    char i;
    char z;
    char c;
};

struct Cpu {
    //CPU内存
    char mem[0x800];
    struct Reg reg;
    struct Flag flag;
    char cycle;
};

#endif //NES_C_CPU6502_H

void cpu_init();
/**
 * 开始执行指令
 */
void cpu_go();

void interrupt_nmi(char reg_2000);