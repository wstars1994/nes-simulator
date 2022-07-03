//
// Created by WStars on 2022/6/18.
//
#include "stdio.h"

#include "include/cpu6502.h"
#include "include/mem.h"

struct Cpu cpu;
int prgPc = 0x8000;

extern FILE *out;

void cpu_init() {
    out = fopen( "../output.txt", "w" );
    cpu.reg.s = 0xff;
    cpu.flag.b = 1;
    cpu.flag.i = 1;
}
void set_i(char flag) {
    cpu.flag.i = flag;
}
void set_n(char flag) {
    cpu.flag.n = get_bit(flag, 7);
}

void set_z(char flag) {
    cpu.flag.z = flag == 0;
}
void set_v(char flag) {
    cpu.flag.v = flag;
}
void set_nz(char flag) {
    set_n(flag);
    set_z(flag);
}

int concat16(char low, char high) {
    return (low & 0xFF) | ((high & 0xff) << 8);
}

//栈操作----------------------------------
void push_stack(char data) {
    write(0x0100 | (cpu.reg.s & 0xFF), data);
    cpu.reg.s -= 1;
}

void push16_stack(short data) {
    push_stack((data >> 8) & 0xFF);
    push_stack(data & 0xFF);
}

char pop8_stack() {
    cpu.reg.s++;
    return read(0x0100 | (cpu.reg.s & 0xFF));
}

int pop16_stack() {
    short pcLow8 = (short) (pop8_stack() & 0xFF);
    short pcLow16 = (short) (pop8_stack() & 0xFF);
    return (pcLow16 << 8) | pcLow8;
}

char flag_merge() {
    return (cpu.flag.n << 7) | (cpu.flag.v << 6) | 0x20 | (cpu.flag.b << 4) | (cpu.flag.d << 3) | (cpu.flag.i << 2) | (cpu.flag.z << 1) |
           cpu.flag.c;
}

void flag_set(char data) {
    cpu.flag.n = (data >> 7) & 0x01;
    cpu.flag.v = (data >> 6) & 0x01;
    cpu.flag.b = (data >> 4) & 0x01;
    cpu.flag.d = (data >> 3) & 0x01;
    cpu.flag.i = (data >> 2) & 0x01;
    cpu.flag.z = (data >> 1) & 0x01;
    cpu.flag.c = data & 0x01;
}

char st_x(int addr, char reg) {
    write(addr, reg);
    return 3;
}
void ams_immed(char *reg) {
    *reg = read_program(prgPc++);
}

int ams_abs() {
    char low = read_program(prgPc++);
    char high = read_program(prgPc++);
    return concat16(low, high);
}

int ams_branch(char data, char branch) {
    char cpu_cycle = 2;
    if (branch) {
        cpu_cycle += (prgPc & 0xff00) == ((prgPc + data) & 0xff00) ? 1 : 2;
        prgPc += data;
    }
    return cpu_cycle;
}

int ams_abs_x(char reg) {
    return ams_abs() + (reg & 0xff);
}

void ams_cmp(char data) {
    short cmpData = (short) ((cpu.reg.a & 0xff) - (data & 0xff));
    cpu.flag.n = (cmpData >> 7) & 1;
    set_z(cmpData & 0xFF);
    cpu.flag.c = (cmpData & 0xff00) == 0 ? 1 : 0;
}

void ams_cpx(char data) {
    //TODO 可能有问题
    char cmpData = cpu.reg.x - data;
    set_nz(cmpData);
    cpu.flag.c = (cmpData & 0xff00) == 0 ? 1 : 0;
}

int indirect() {
    char data = read_program(prgPc++);
    char low = read(data & 0xFF);
    char high = read((data & 0xFF) + 1);
    return concat16(low, high);
}

char indirect_y() {
    return read(indirect() + (cpu.reg.y & 0xFF));
}

void ams_cpy(char data) {
    short cmpData = (short) ((cpu.reg.y & 0xFF) - (data & 0xFF));
    set_nz(cmpData);
    cpu.flag.c = (cmpData & 0xff00) == 0;
}

void ams_ora(char data) {
    cpu.reg.a |= data;
    set_nz(cpu.reg.a);
}

void ams_sbc(char data) {
    int abs_data = (cpu.reg.a & 0xff) - (data & 0xff) - (cpu.flag.c == 0);
    cpu.flag.c = (abs_data & 0xff00) == 0;
    set_nz(abs_data & 0xff);
    set_v((((cpu.reg.a ^ data) & 0x80) != 0) && ((cpu.reg.a ^ abs_data) & 0x80) != 0);
    cpu.reg.a = abs_data & 0xff;
}

void ams_eor_a(char data) {
    cpu.reg.a ^= data;
    set_nz(cpu.reg.a);
}

void ams_adc(char data) {
    int abs_data = (cpu.reg.a & 0xff) + (data & 0xff) + (cpu.flag.c & 0xff);
    cpu.flag.c = abs_data >> 8;
    set_nz(abs_data & 0xff);
    cpu.flag.v = ((cpu.reg.a ^ data) & 0x80) == 0 && ((cpu.reg.a ^ abs_data) & 0x80) != 0;
    cpu.reg.a = abs_data & 0xff;
}

void ams_lsr(int addr) {
    char data = read(addr);
    cpu.flag.c = data & 1;
    char data2 = (data & 0xff) >> 1;
    set_nz(data2);
    write(addr, data2);
}

int ams_zero(char addr, char reg) {
    return (addr & 0XFF) + (reg & 0xff);
}

void ams_lda(char data) {
    set_nz(data);
    cpu.reg.a = data;
}

void ams_and(char data) {
    cpu.reg.a &= data;
    set_nz(cpu.reg.a);
}

void ams_asl(int addr) {
    char data = read(addr);
    cpu.flag.c = (data >> 7) & 1;
    char data2 = data << 1;
    set_nz(data2);
    write(addr, data2);
}

char indirect_x() {
    char data = read_program(prgPc++);
    char addr = (data & 0xFF) + (cpu.reg.x & 0xFF);
    return read(concat16(read(addr), read(addr + 1)));
}

void ams_ror(int addr) {
    char data = read(addr);
    char read2 = ((data & 0xff) >> 1) | (cpu.flag.c << 7);
    cpu.flag.c = data & 1;
    set_nz(read2);
    write(addr, read2);
}

void ams_rol(int addr) {
    char data = read(addr);
    char read2 = ((data & 0xff) << 1) | cpu.flag.c;
    cpu.flag.c = (data >> 7) & 1;
    set_nz(read2);
    write(addr, read2);
}

//中断
void interrupt_nmi(char reg_2000) {
    if (get_bit(reg_2000, 7)) {
        fprintf(out,"NMI");
        push16_stack(prgPc);
        push_stack(flag_merge());
        set_i(1);
        prgPc = (read(0xFFFA) & 0xff) | ((read(0xFFFB) & 0xff) << 8);
        fprintf(out,"\n");
    }
}
long count = 0;
void cpu_go() {
    cpu.cycle = 113;
    int abs_data;
    int data,data2,data3;
    while (cpu.cycle > 0) {
        char opc = read_program(prgPc++);

        fprintf(out,"PC:[%06d] | CYC:[%03d] | PC:[%X] | OPC:[%02X] | R:[A:%02X X:%02X Y:%02X S:%02X] | F:[N:%d V:%d B:%d D:%d I:%d Z:%d C:%d]",
                ++count,
               cpu.cycle,
                (prgPc-1)&0xFFFF,
                 opc&0xFF,
                cpu.reg.a&0xFF,cpu.reg.x&0xFF,cpu.reg.y&0xFF,cpu.reg.s&0xFF,
                cpu.flag.n,cpu.flag.v,cpu.flag.b,cpu.flag.d,cpu.flag.i,cpu.flag.z,cpu.flag.c);

        switch (opc & 0xFF) {
            case 0x78:
                cpu.flag.i = 1;
                cpu.cycle -= 2;
                break;
            case 0xD8:
                cpu.flag.d = 0;
                cpu.cycle -= 2;
                break;
            case 0xA9:
                ams_immed(&cpu.reg.a);
                set_nz(cpu.reg.a);
                cpu.cycle -= 2;
                break;
            case 0x8D:
                write(ams_abs(), cpu.reg.a);
                cpu.cycle -= 4;
                break;
            case 0xA2:
                ams_immed(&cpu.reg.x);
                set_nz(cpu.reg.x);
                cpu.cycle -= 2;
                break;
            case 0x9A:
                cpu.reg.s = cpu.reg.x;
                cpu.cycle -= 2;
                break;
            case 0xAD:
                cpu.reg.a = read(ams_abs());
                set_nz(cpu.reg.a);
                cpu.cycle -= 4;
                break;
            case 0x10:
                cpu.cycle -= ams_branch(read_program(prgPc++), cpu.flag.n == 0);
                break;
            case 0xA0:
                ams_immed(&cpu.reg.y);
                set_nz(cpu.reg.y);
                cpu.cycle -= 2;
                break;
            case 0xBD:
                cpu.reg.a = read(ams_abs_x(cpu.reg.x));
                set_nz(cpu.reg.a);
                cpu.cycle -= 4;
                break;
            case 0xC9:
                ams_cmp(read_program(prgPc++));
                cpu.cycle -= 2;
                break;
            case 0xB0:
                cpu.cycle -= ams_branch(read_program(prgPc++), cpu.flag.c);
                break;
            case 0xCA:
                cpu.reg.x -= 1;
                set_nz(cpu.reg.x);
                cpu.cycle -= 2;
                break;
            case 0xD0:
                cpu.cycle -= ams_branch(read_program(prgPc++), cpu.flag.z == 0);
                break;
                //JSR
            case 0x20:
                abs_data = ams_abs();
                push16_stack(prgPc - 1);
                prgPc = abs_data;
                cpu.cycle -= 6;
                break;
            case 0x85:
                cpu.cycle -= st_x(read_program(prgPc++) & 0xff, cpu.reg.a);
                break;
            case 0x86:
                cpu.cycle -= st_x(read_program(prgPc++) & 0xff, cpu.reg.x);
                break;
                //CPX
            case 0xE0:
                ams_cpx(read_program(prgPc++));
                cpu.cycle -= 2;
                break;
                //STA_INDIRECT_Y
            case 0x91:
                write(indirect() + (cpu.reg.y & 0xff), cpu.reg.a);
                cpu.cycle -= 6;
                break;
            case 0x88:
                cpu.reg.y--;
                set_nz(cpu.reg.y);
                cpu.cycle -= 2;
                break;
                //CPY
            case 0xC0:
                ams_cpy(read_program(prgPc++));
                cpu.cycle -= 2;
                break;
                //RTS
            case 0x60:
                prgPc = pop16_stack();
                prgPc += 1;
                cpu.cycle -= 6;
                break;
                //BRK
            case 0x00:
                cpu.flag.b = 1;
                cpu.flag.i = 1;
                push16_stack(prgPc + 2);
                push_stack(flag_merge());
                prgPc = concat16(read(0xFFFE), read(0xFFFF));
                cpu.cycle -= 7;
                break;
                //SED
            case 0xF8:
                cpu.flag.d = 1;
                cpu.cycle -= 2;
                break;
                //BIT_ABS
            case 0x2C:
                data = read(ams_abs());
                set_n(data);
                set_v((data >> 6) & 1);
                set_z(cpu.reg.a & data);
                cpu.cycle -= 4;
                break;
                //STA_ABS_Y
            case 0x99:
                st_x(ams_abs_x(cpu.reg.y), cpu.reg.a);
                cpu.cycle -= 5;
                break;
                //INY
            case 0xC8:
                cpu.reg.y += 1;
                set_nz(cpu.reg.y);
                cpu.cycle -= 2;
                break;
                //ORA
            case 0x09:
                data = read_program(prgPc++);
                ams_ora(data);
                cpu.cycle -= 2;
                break;
                //AND
            case 0x29:
                data = read_program(prgPc++);
                cpu.reg.a &= data;
                set_nz(cpu.reg.a);
                cpu.cycle -= 2;
                break;
                //TXA
            case 0x8A:
                cpu.reg.a = cpu.reg.x;
                set_nz(cpu.reg.a);
                cpu.cycle -= 2;
                break;
                //JMP_ABS
            case 0x4C:
                prgPc = ams_abs();
                cpu.cycle -= 3;
                break;
                //INC_ABS
            case 0xEE:
                abs_data = ams_abs();
                data = (read(abs_data) + 1) & 0xff;
                write(abs_data, data);
                set_nz(data);
                cpu.cycle -= 6;
                break;
                //执行到AC前会触发NMI
                //LDY_ABS
            case 0xAC:
                cpu.reg.y = read(ams_abs());
                set_nz(cpu.reg.y);
                cpu.cycle -= 4;
                break;
                //LDX_ABS
            case 0xAE:
                cpu.reg.x = read(ams_abs());
                set_nz(cpu.reg.x);
                cpu.cycle -= 4;
                break;
                //LDA_INDIRECT_Y
            case 0xB1:
                cpu.reg.a = indirect_y();
                set_nz(cpu.reg.a);
                cpu.cycle -= 5;
                break;
                //LDX_ABS_Y
            case 0xBE:
                cpu.reg.x = read(ams_abs_x(cpu.reg.y));
                set_nz(cpu.reg.x);
                cpu.cycle -= 4;
                break;
                //STA_ABS_X
            case 0x9D:
                abs_data = ams_abs_x(cpu.reg.x);
                write(abs_data, cpu.reg.a);
                cpu.cycle -= 5;
                break;
                //LSR
            case 0x4A:
                cpu.flag.c = cpu.reg.a & 1;
                cpu.reg.a = (cpu.reg.a & 0xff) >> 1;
                set_nz(cpu.reg.a);
                cpu.cycle -= 2;
                break;
                //TAX
            case 0xAA:
                cpu.reg.x = cpu.reg.a;
                set_nz(cpu.reg.x);
                cpu.cycle -= 2;
                break;
                //PHA
            case 0x48:
                push_stack(cpu.reg.a);
                cpu.cycle -= 3;
                break;
                //ORA_ZERO
            case 0x05:
                data = read_program(prgPc++);
                cpu.reg.a |= read(data & 0xFF);
                set_nz(cpu.reg.a);
                cpu.cycle -= 3;
                break;
                //PLA
            case 0x68:
                cpu.reg.a = pop8_stack();
                set_nz(cpu.reg.a);
                cpu.cycle -= 4;
                break;
                //ROL
            case 0x2A:
                data = cpu.reg.a;
                cpu.reg.a = (data << 1) | cpu.flag.c;
                cpu.flag.c = (data >> 7) & 1;
                set_nz(cpu.reg.a);
                cpu.cycle -= 2;
                break;
                //AND_ABS_X
            case 0x3D:
                abs_data = ams_abs_x(cpu.reg.x);
                cpu.reg.a &= read(abs_data);
                set_nz(cpu.reg.a);
                cpu.cycle -= 4;
                break;
                //BEQ
            case 0xF0:
                data = read_program(prgPc++);
                cpu.cycle -= ams_branch(data, cpu.flag.z);
                break;
                //INX
            case 0xE8:
                cpu.reg.x += 1;
                set_nz(cpu.reg.x);
                cpu.cycle -= 2;
                break;
                //SEC
            case 0x38:
                cpu.flag.c = 1;
                cpu.cycle -= 2;
                break;
                //SBC_ABS_Y
            case 0xF9:
                ams_sbc(read(ams_abs_x(cpu.reg.y)));
                cpu.cycle -= 4;
                break;
                //BCC
            case 0x90:
                data = read_program(prgPc++);
                cpu.cycle -= ams_branch(data, cpu.flag.c == 0);
                break;
                //EOR_ZERO
            case 0x45:
                ams_eor_a(read(read_program(prgPc++) & 0xFF));
                cpu.cycle -= 3;
                break;
                //CLC
            case 0x18:
                cpu.flag.c = 0;
                cpu.cycle -= 2;
                break;
                //ROR_ABS_X
            case 0x7E:
                abs_data = ams_abs_x(cpu.reg.x);
                data = read(abs_data);
                data2 = ((data & 0xff) >> 1) | (cpu.flag.c << 7);
                cpu.flag.c = data & 1;
                set_nz(data2);
                write(abs_data, data2);
                cpu.cycle -= 7;
                break;
                //DEC_ABS
            case 0xCE:
                abs_data = ams_abs();
                data = read(abs_data) - 1;
                write(abs_data, data);
                set_nz(data);
                cpu.cycle -= 6;
                break;
                //INC_ZERO
            case 0xE6:
                data = read_program(prgPc++);
                data2 = read(data & 0xFF) + 1;
                write(data & 0xFF, data2);
                set_nz(data2);
                cpu.cycle -= 5;
                break;
                //ASL_A
            case 0x0A:
                cpu.flag.c = (cpu.reg.a >> 7) & 1;
                cpu.reg.a <<= 1;
                set_nz(cpu.reg.a);
                cpu.cycle -= 2;
                break;
                //TAY
            case 0xA8:
                cpu.reg.y = cpu.reg.a;
                set_nz(cpu.reg.y);
                cpu.cycle -= 2;
                break;
                //JMP_INDIRECT
            case 0x6C:
                abs_data = ams_abs();
                data = read(abs_data);
                prgPc = concat16(data, read(abs_data + 1));
                cpu.cycle -= 5;
                break;
                //LDA_ABS_Y
            case 0xB9:
                cpu.reg.a = read(ams_abs_x(cpu.reg.y));
                set_nz(cpu.reg.a);
                cpu.cycle -= 4;
                break;
                //ADC_ABS
            case 0x6D:
                ams_adc(read(ams_abs()));
                cpu.cycle -= 4;
                break;
                //ADC
            case 0x69:
                ams_adc(read_program(prgPc++));
                cpu.cycle -= 2;
                break;
                //STY_ABS
            case 0x8C:
                write(ams_abs(), cpu.reg.y);
                cpu.cycle -= 4;
                break;
                //LDA_ZERO
            case 0xA5:
                cpu.reg.a = read(read_program(prgPc++) & 0xFF);
                set_nz(cpu.reg.a);
                cpu.cycle -= 3;
                break;
                //RTI
            case 0x40:
                flag_set(pop8_stack());
                prgPc = pop16_stack();
                cpu.cycle -= 6;
                break;
                //DEC_ZERO
            case 0xC6:
                data = read_program(prgPc++) & 0xFF;
                data2 = read(data) - 1;
                write(data, data2);
                set_nz(data2);
                cpu.cycle -= 5;
                break;
                //TYA
            case 0x98:
                cpu.reg.a = cpu.reg.y;
                set_nz(cpu.reg.a);
                cpu.cycle -= 2;
                break;
                //ADC_ZERO
            case 0x65:
                ams_adc(read(read_program(prgPc++) & 0xFF));
                cpu.cycle -= 3;
                break;
                //LDX_ZERO
            case 0xA6:
                cpu.reg.x = read(read_program(prgPc++) & 0xFF);
                set_nz(cpu.reg.x);
                cpu.cycle -= 3;
                break;
                //STX_ABS
            case 0x8E:
                write(ams_abs(), cpu.reg.x);
                cpu.cycle -= 4;
                break;
                //BMI
            case 0x30:
                cpu.cycle -= ams_branch(read_program(prgPc++), cpu.flag.n);
                break;
                //ADC_ABS_Y
            case 0x79:
                ams_adc(read(ams_abs_x(cpu.reg.y)));
                cpu.cycle -= 4;
                break;
                //SBC
            case 0xE9:
                ams_sbc(read_program(prgPc++));
                cpu.cycle -= 2;
                break;
                //STY_ZERO
            case 0x84:
                write(read_program(prgPc++) & 0xFF, cpu.reg.y);
                cpu.cycle -= 3;
                break;
                //BIT_ZERO
            case 0x24:
                data = read(read_program(prgPc++) & 0xFF);
                set_n(data);
                set_v((data >> 6) & 1);
                set_z(cpu.reg.a & data);
                cpu.cycle -= 3;
                break;
                //LDY_ZERO
            case 0xA4:
                data = read(read_program(prgPc++) & 0xFF);
                cpu.reg.y = data;
                set_nz(cpu.reg.y);
                cpu.cycle -= 3;
                break;
                //CMP_ABS
            case 0xCD:
                ams_cmp(read(ams_abs()));
                cpu.cycle -= 4;
                break;
                //CMP_ABS_Y
            case 0xD9:
                ams_cmp(read(ams_abs_x(cpu.reg.y)));
                cpu.cycle -= 4;
                break;
                //EOR
            case 0x49:
                cpu.reg.a ^= read_program(prgPc++);
                set_nz(cpu.reg.a);
                cpu.cycle -= 2;
                break;
                //ROL_ZERO
            case 0x26:
                data = read(read_program(prgPc++) & 0xFF);
                data2 = (data << 1) | cpu.flag.c;
                cpu.flag.c = (data >> 7) & 1;
                set_nz(data2);
                write(read_program(prgPc - 1) & 0xFF, data2);
                cpu.cycle -= 5;
                break;
                //LSR_ZERO
            case 0x46:
                data = read_program(prgPc++) & 0xFF;
                data3 = read(data & 0xff);
                data2 = (data3 & 0xff) >> 1;
                cpu.flag.c = data3 & 1;
                set_nz(data2);
                write(data, data2);
                cpu.cycle -= 5;
                break;
                //LDY_ABS_X
            case 0xBC:
                cpu.reg.y = read(ams_abs_x(cpu.reg.x));
                set_nz(cpu.reg.y);
                cpu.cycle -= 4;
                break;
                //DEC_ABS_X
            case 0xDE:
                abs_data = ams_abs_x(cpu.reg.x);
                data = read(abs_data) - 1;
                write(abs_data, data);
                set_nz(data);
                cpu.cycle -= 7;
                break;
                //LSR_ABS
            case 0x4E:
                ams_lsr(ams_abs());
                cpu.cycle -= 6;
                break;
                //ROR_A
            case 0x6A:
                data = ((cpu.reg.a & 0xff) >> 1) | (cpu.flag.c << 7);
                cpu.flag.c = cpu.reg.a & 1;
                set_nz(data);
                cpu.reg.a = data;
                cpu.cycle -= 2;
                break;
                //ROL_ABS
            case 0x2E:
                abs_data = ams_abs();
                data = read(abs_data);
                data2 = (data << 1) | cpu.flag.c;
                cpu.flag.c = (data >> 7) & 1;
                set_nz(data2);
                write(abs_data, data2);
                cpu.cycle -= 6;
                break;
                //CMP_ZERO
            case 0xC5:
                ams_cmp(read(read_program(prgPc++) & 0xFF));
                cpu.cycle -= 3;
                break;
                //ORA_ABS
            case 0x0D:
                cpu.reg.a |= read(ams_abs());
                set_nz(cpu.reg.a);
                cpu.cycle -= 4;
                break;
                //NOP
            case 0xEA:
                cpu.cycle -= 2;
                break;
                //LDA_ZERO_X
            case 0xB5:
                ams_lda(read(ams_zero(read_program(prgPc++), cpu.reg.x)));
                cpu.cycle -= 4;
                break;
                //STA_ZERO_X
            case 0x95:
                write(ams_zero(read_program(prgPc++), cpu.reg.x), cpu.reg.a);
                cpu.cycle -= 4;
                break;
                //SBC_ABS
            case 0xED:
                ams_sbc(read(ams_abs()));
                cpu.cycle -= 4;
                break;
                //CMP_ZERO_X
            case 0xD5:
                ams_cmp(read(ams_zero(read_program(prgPc++), cpu.reg.x)));
                cpu.cycle -= 4;
                break;
                //SBC_ZERO_X
            case 0xF5:
                ams_sbc(read(ams_zero(read_program(prgPc++), cpu.reg.x)));
                cpu.cycle -= 4;
                break;
                //AND_ABS_Y
            case 0x39:
                cpu.reg.a &= read(ams_abs_x(cpu.reg.y));
                set_nz(cpu.reg.a);
                cpu.cycle -= 4;
                break;
                //ASL_ABS
            case 0x0E:
                abs_data = ams_abs();
                data = read(abs_data);
                cpu.flag.c = (data >> 7) & 1;
                data2 = data << 1;
                set_nz(data2);
                write(abs_data, data2);
                cpu.cycle -= 6;
                break;
                //AND_ABS
            case 0x2D:
                cpu.reg.a &= read(ams_abs());
                set_nz(cpu.reg.a);
                cpu.cycle -= 4;
                break;
                //ADC_ABS_X
            case 0x7D:
                ams_adc(read(ams_abs_x(cpu.reg.x)));
                cpu.cycle -= 4;
                break;
                //CMP_ABS_X
            case 0xDD:
                ams_cmp(read(ams_abs_x(cpu.reg.x)));
                cpu.cycle -= 4;
                break;
                //ADC_ZERO_X
            case 0x75:
                ams_adc(read(ams_zero(read_program(prgPc++), cpu.reg.x)));
                cpu.cycle -= 4;
                break;
                //PHP
            case 0x08:
                push_stack(flag_merge());
                cpu.cycle -= 3;
                break;
                //CPX_ZERO
            case 0xE4:
                ams_cpx(read(read_program(prgPc++) & 0xFF));
                cpu.cycle -= 3;
                break;
                //SBC_ZERO
            case 0xE5:
                ams_sbc(read(read_program(prgPc++) & 0xff));
                cpu.cycle -= 3;
                break;
                //AND_ZERO
            case 0x25:
                data = read(read_program(prgPc++) & 0xff);
                ams_and(data);
                cpu.cycle -= 3;
                break;
                //PLP
            case 0x28:
                flag_set(pop8_stack());
                cpu.cycle -= 4;
                break;
                //ASL_ZERO
            case 0x06:
                ams_asl(read_program(prgPc++));
                cpu.cycle -= 5;
                break;
                //AND_INDIRECT_Y
            case 0x31:
                ams_and(indirect_y());
                cpu.cycle -= 5;
                break;
                //LDY_ZERO_X
            case 0xB4:
                cpu.reg.y = read(ams_zero(read_program(prgPc++), cpu.reg.x));
                set_nz(cpu.reg.y);
                cpu.cycle -= 4;
                break;
                //EOR_ABS
            case 0x4D:
                ams_eor_a(read(ams_abs()));
                cpu.cycle -= 4;
                break;
                //TSX
            case 0xBA:
                cpu.reg.x = cpu.reg.s;
                set_nz(cpu.reg.x);
                cpu.cycle -= 2;
                break;
                //DEC_ZERO_X
            case 0xD6:
                abs_data = ams_zero(read_program(prgPc++), cpu.reg.x);
                data2 = read(abs_data) - 1;
                write(abs_data, data2);
                set_nz(data2);
                cpu.cycle -= 6;
                break;
                //EOR_ZERO_X
            case 0x55:
                ams_eor_a(read(ams_zero(read_program(prgPc++), cpu.reg.x)));
                cpu.cycle -= 4;
                break;
                //AND_ZERO_X
            case 0x35:
                ams_and(read(ams_zero(read_program(prgPc++), cpu.reg.x)));
                cpu.cycle -= 4;
                break;
                //STY_ZERO_X
            case 0x94:
                write(ams_zero(read_program(prgPc++), cpu.reg.x), cpu.reg.y);
                cpu.cycle -= 4;
                break;
                //INC_ZERO_X
            case 0xF6:
                abs_data = ams_zero(read_program(prgPc++), cpu.reg.x);
                data = read(abs_data) + 1;
                write(abs_data, data);
                set_nz(data);
                cpu.cycle -= 6;
                break;
                //ORA_ZERO_X
            case 0x15:
                ams_ora(read(read_program(prgPc++)));
                cpu.cycle -= 4;
                break;
                //BVC
            case 0x50:
                cpu.cycle -= ams_branch(read_program(prgPc++), !cpu.flag.v);
                break;
                //SBC_ABS_X
            case 0xFD:
                ams_sbc(read(ams_abs_x(cpu.reg.x)));
                cpu.cycle -= 4;
                break;
                //CPY_ZERO
            case 0xC4:
                ams_cpy(read(read_program(prgPc++) & 0xFF));
                cpu.cycle -= 3;
                break;
                //ORA_INDIRECT_Y
            case 0x11:
                ams_ora(indirect_y());
                cpu.cycle -= 5;
                break;
                //ORA_ABS_Y
            case 0x19:
                ams_ora(read(ams_abs_x(cpu.reg.y)));
                cpu.cycle -= 4;
                break;
                //ADC_INDIRECT_Y
            case 0x71:
                ams_adc(indirect_y());
                cpu.cycle -= 5;
                break;
                //ORA_ABS_X
            case 0x1D:
                ams_ora(read(ams_abs_x(cpu.reg.x)));
                cpu.cycle -= 4;
                break;
                //CMP_INDIRECT_Y
            case 0xD1:
                ams_cmp(indirect_y());
                cpu.cycle -= 5;
                break;
                //SBC_INDIRECT_Y
            case 0xF1:
                ams_sbc(indirect_y());
                cpu.cycle -= 5;
                break;
                //LDA_INDIRECT_X
            case 0xA1:
                ams_lda(indirect_x());
                cpu.cycle -= 6;
                break;
                //STA_INDIRECT_X
            case 0x81:
                st_x(indirect_x() & 0xff, cpu.reg.a);
                cpu.cycle -= 6;
                break;
                //INC_ABS_X
            case 0xFE:
                abs_data = ams_abs_x(cpu.reg.x);
                data = read(abs_data) + 1;
                write(abs_data, data);
                set_nz(data);
                cpu.cycle -= 7;
                break;
                //CPY_ABS
            case 0xCC:
                ams_cpy(read(ams_abs()));
                cpu.cycle -= 4;
                break;
                //ROR
            case 0x66:
                ams_ror(read_program(prgPc++) & 0xff);
                cpu.cycle -= 5;
                break;
                //ROR_ABS
            case 0x6E:
                ams_ror(ams_abs());
                cpu.cycle -= 6;
                break;
                //BVS
            case 0x70:
                cpu.cycle -= ams_branch(read_program(prgPc++), cpu.flag.v);
                break;
                //ROL_ABS_X
            case 0x3E:
                ams_rol(ams_abs_x(cpu.reg.x));
                cpu.cycle -= 7;
                break;
                //ASL_ABS_X
            case 0x1E:
                ams_asl(ams_abs_x(cpu.reg.x));
                cpu.cycle -= 7;
                break;
                //EOR_ABS_X
            case 0x5D:
                ams_eor_a(read(ams_abs_x(cpu.reg.x)));
                cpu.cycle -= 4;
                break;
                //CPX_ABS
            case 0xEC:
                ams_cpx(read(ams_abs()));
                cpu.cycle -= 4;
                break;
                //ROR_ZERO_X
            case 0x76:
                ams_ror(ams_zero(read_program(prgPc++), cpu.reg.x));
                cpu.cycle -= 6;
                break;
                //LSR_ZERO_X
            case 0x56:
                ams_lsr(ams_zero(read_program(prgPc++), cpu.reg.x));
                cpu.cycle -= 6;
                break;
                //ROL_ZERO_X
            case 0x36:
                ams_rol(ams_zero(read_program(prgPc++), cpu.reg.x));
                cpu.cycle -= 6;
                break;
                //LDX_ZERO_Y
            case 0xB6:
                cpu.reg.x = read(ams_zero(read_program(prgPc++), cpu.reg.y));
                set_nz(cpu.reg.x);
                cpu.cycle -= 4;
                break;
                //EOR_INDIRECT_Y
            case 0x51:
                ams_eor_a(indirect_y());
                cpu.cycle -= 5;
                break;
                //STX_ZERO_Y
            case 0x96:
                st_x(ams_zero(read_program(prgPc++), cpu.reg.y), cpu.reg.x);
                cpu.cycle -= 4;
                break;
                //CLI
            case 0x58:
                cpu.flag.i = 0;
                cpu.cycle -= 2;
                break;
                //LSR_ABS_X
            case 0x5E:
                ams_lsr(ams_abs_x(cpu.reg.x));
                cpu.cycle -= 7;
                break;
            default:
                cpu.cycle -= 0;
                //fprintf(out,"unknown ins %X\n", opc & 0xFF);
                break;
        }
        fprintf(out,"\n");
    }
}