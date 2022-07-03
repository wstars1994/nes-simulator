//
// Created by WStars on 2022/6/18.
//

#ifndef NES_C_MEM_H
#define NES_C_MEM_H

#endif //NES_C_MEM_H

#include <stdio.h>

/**
 * 读取程序执行数据
 * @param addr 读取地址
 */
char read_program(int addr);

char get_bit(char data, char index);

void set_bit(char *data, char set_data, char index);

void write(int addr, char data);

char read(int addr);

char p_read(int addr);
