//
// Created by WStars on 2021/9/1.
//
#include <stdio.h>
#include <stm32f4xx_ll_dma.h>
#include "stm32f4xx_ll_usart.h"
#include "nes.h"
#include "ILI9341.h"
//#include "sdcard.h"
//RGB32位颜色
short palettes[65] = {29614,8401,21,16403,-30706,-22526,-24576,30784,16736,544,640,482,6635,0,32,32,-16905,925,8669,-32738,-18409,-8181,-9920,-13727,-29824,1184,1344,1159,1041,4226,2113,2113,-1,15871,23743,-23457,-3105,-1098,-1108,-825,-2585,-31102,20201,24531,1883,25388,2145,2145,-1,-20673,-14657,-10657,-449,-453,-522,-299,-204,-6156,-20585,-18439,-24578,-8453,4226,4226,0,};

int _write(int file, char* ptr, int len)
{
    for (int i = 0; i != len; ++i)
        LL_USART_TransmitData8(USART2,*ptr++);
    return len;
}

byte get_bit(byte data,byte index)
{
    return (data>>index)&1;
}

void set_bit(byte *data ,byte set_data,byte index)
{
    if(set_data) {
        *data |= 1<<index;
    }else{
        *data &= ~(1<<index);
    }
}

long count = 0;

int prgPc = 0x8000;
//状态寄存器
byte flag_n = 0,flag_v = 0,flag_b = 1,flag_d = 0,flag_i = 1,flag_z = 0,flag_c = 0;
//CPU寄存器
byte c_reg_a,c_reg_x,c_reg_y,c_reg_s=0xFF;
//CPU内存
byte c_mem[0x800];
//PPU寄存器
byte p_reg_2000,p_reg_2001,p_reg_2002,p_reg_2003,p_reg_2007,p_write_toggle=0,p_reg_2007,p_scroll_x;
//PPU内存
byte p_mem[0x2000];
//精灵数据
byte p_spr_ram[256];

short p_vram_addr,p_vram_temp_addr;
short frameData[240][3];
byte frameSpriteData[240][2];

uint16_t render[256*120];

int program_length;

//读取程序指令
byte read_program(int addr){
    if(addr < 0x8000){
        return c_mem[addr];
    }else{
        int start_addr = addr - 0x8000;
        return *(__IO uint8_t*)(0x08020010+start_addr);
    }
}
//标志位操作
void set_i(byte flag){
    flag_i = flag;
}
void set_d(byte flag){
    flag_d = flag;
}
void set_n(byte flag){
    flag_n = get_bit(flag,7);
}
void set_z(byte flag){
    flag_z = flag == 0;
}

void set_nz(byte flag){
    set_n(flag);
    set_z(flag);
}

void set_v(byte flag){
    flag_v = flag;
}
//PPU操作---------------------------
byte p_read(short addr){
    if(addr < 0x2000){
        return *(__IO uint8_t*)(0x08020010 + program_length + addr);
    }
    return p_mem[addr-0x2000];
}
void p_write(short addr,byte data){
    //写入 $3F00-3FFF 的 D7-D6 字节被忽略.
    if(addr >= 0x3F00 && addr <= 0x3FFF) {
        data = data & 0x3f;
        if(addr==0x3F00){
            p_mem[0x3F10-0x2000]=data;
        }else if(addr==0x3F10){
            p_mem[0x3F00-0x2000]=data;
        }
    }
    //printf(" | PWR:[addr:%02X INDEX:%d DATA:%d]",addr&0xFFFF,addr&0xFFFF,data);
    p_mem[addr-0x2000] = data;
}
void p_write_spr(byte addr, byte data){
    p_spr_ram[addr&0xFF] = data;
}

int showBg(){
    return get_bit(p_reg_2001,3);
}
int showSpr(){
    return get_bit(p_reg_2001,4);
}

//内存操作----------------------------------
byte read(int addr){
    //printf(" | RD:[addr:%02X INDEX:%d]",addr,addr);
    byte ret_p_reg_2002;
    int temp_p_reg_2006;
    switch (addr) {
        //读PPUSTATUS状态寄存器
        case 0x2002:
            ret_p_reg_2002 = p_reg_2002;
            set_bit(&p_reg_2002,0,7);
            p_write_toggle=0;
            return ret_p_reg_2002;
        case 0x2007:
            temp_p_reg_2006 = p_vram_addr&0x3fff;
            p_vram_addr += get_bit(p_reg_2000,2)?32:1;
            if(addr <= 0x3EFF) {
                //读取PPU
                byte res = p_reg_2007;
                p_reg_2007 = p_read(temp_p_reg_2006);
                return res;
            }
            break;
        default:
            return read_program(addr);
    }

}

void write(int addr,byte data){
    //printf(" | WR:[ADDR:%02X INDEX:%d DATA:%d]",addr&0xFFFF,addr&0xFFFF,data);
    short start;
    switch (addr) {
        case 0x2000:
            p_reg_2000 = data;
            p_vram_temp_addr = (p_vram_temp_addr & 0xf3ff) | ((data & 3) << 10);
            break;
        case 0x2001:
            p_reg_2001 = data;
            break;
        case 0x2003:
            p_reg_2003 = data;
            break;
        case 0x2004:
            p_reg_2003+=1;
            p_write_spr(p_reg_2003,data);
            break;
        case 0x2005:
            if(!p_write_toggle) {
                //设置fine_x
                p_scroll_x = data & 0x7;
                //设置coarse_x
                p_vram_temp_addr &= ~0x1F;
                p_vram_temp_addr |= ((data & 0xff) >> 3) & 0x1F;
            }else{
                //设置coarse_y
                p_vram_temp_addr &= ~0x3E0;
                p_vram_temp_addr |= (((data&0xff)>>3)<<5);
                //设置fine_y
                p_vram_temp_addr &= ~0x7000;
                p_vram_temp_addr |= (data&0x7)<<12;
            }
            p_write_toggle = !p_write_toggle;
            break;
        case 0x2006:
            if(!p_write_toggle) {
                //高6位置0 PPU高7/8位无效 置0
                p_vram_temp_addr &= ~(0xFF<<8);
                //第一次写将写入高6位;
                p_vram_temp_addr |= ((data&0x3F) << 8);
            }else {
                //低8位置0
                p_vram_temp_addr &= ~0xFF;
                //第二次写将写入低8位
                p_vram_temp_addr |= data&0xFF;
                p_vram_addr = p_vram_temp_addr;
            }
            p_write_toggle = !p_write_toggle;
            break;
        case 0x2007:
            p_write(p_vram_addr,data);
            p_vram_addr += get_bit(p_reg_2000,2)?32:1;
            break;
            //OAM DMA register (high byte)
        case 0x4014:
            start = data<<8;
            for(int i=0; i < 256; i++) {
                byte readData = read(start++);
                p_write_spr(i,readData);
            }
            break;
        default:
            c_mem[addr&0xFFFF] = data;
    }
}

//指令操作---------------------------
int concat16(byte low, byte high){
    return (low & 0xFF) | ((high&0xff)<<8);
}

int ams_branch(byte data, int branch) {
    byte cpu_cycle = 2;
    if(branch) {
        cpu_cycle+= (prgPc & 0xff00) == ((prgPc+ data) & 0xff00) ? 1 : 2;
        prgPc+=data;
    }
    return cpu_cycle;
}

void ams_immed(byte *reg){
    *reg = read_program(prgPc++);
}

int ams_abs() {
    byte low = read_program(prgPc++);
    byte high = read_program(prgPc++);
    return concat16(low,high);
}

int ams_abs_x(byte reg){
    return ams_abs() + (reg & 0xff);
}

int ams_zero(byte addr,byte reg) {
    return (addr&0XFF) + (reg & 0xff);
}

void ams_adc(byte data){
    int abs_data = (c_reg_a & 0xff) + (data & 0xff) + (flag_c & 0xff);
    flag_c = abs_data >> 8;
    set_nz(abs_data & 0xff);
    flag_v = ((c_reg_a ^ data) & 0x80) == 0 && ((c_reg_a ^ abs_data) & 0x80) != 0;
    c_reg_a = abs_data&0xff;
}

void ams_sbc(byte data){
    int abs_data = (c_reg_a & 0xff) - (data&0xff) - (flag_c == 0 );
    flag_c = (abs_data & 0xff00) == 0;
    set_nz(abs_data&0xff);
    set_v((((c_reg_a ^ data) & 0x80) != 0)&& ((c_reg_a ^ abs_data) & 0x80) != 0);
    c_reg_a = abs_data&0xff;
}

void ams_cmp(byte data){
    short cmpData = (short) ((c_reg_a & 0xff) - (data & 0xff));
    flag_n = (cmpData >> 7) & 1;
    set_z(cmpData&0xFF);
    flag_c = (cmpData & 0xff00) == 0 ? 1 : 0;
}

byte st_x(int addr,byte reg){
    write(addr, reg);
    return 3;
}


int indirect(){
    byte data = read_program(prgPc++);
    byte low = read(data & 0xFF);
    byte high = read((data & 0xFF) + 1);
    return concat16(low,high);
}

byte indirect_y() {
    return read(indirect() + (c_reg_y & 0xFF));
}

byte indirect_x(){
    byte data = read_program(prgPc++);
    byte addr = (data & 0xFF) + (c_reg_x & 0xFF);
    return read(concat16(read(addr), read(addr + 1)));
}

void ams_and(byte data){
    c_reg_a &= data;
    set_nz(c_reg_a);
}

void ams_eor_a(byte data){
    c_reg_a ^= data;
    set_nz(c_reg_a);
}

void ams_ora(byte data){
    c_reg_a |= data;
    set_nz(c_reg_a);
}

void ams_cpy(byte data){
    short cmpData = (short) ((c_reg_y&0xFF) - (data&0xFF));
    set_nz(cmpData);
    flag_c = (cmpData & 0xff00) == 0;
}

void ams_lda(byte data){
    set_nz(data);
    c_reg_a = data;
}

void ams_ror(int addr) {
    byte data = read(addr);
    byte read2 = ((data & 0xff) >> 1) | (flag_c << 7);
    flag_c = data&1;
    set_nz(read2);
    write(addr,read2);
}

void ams_rol(int addr) {
    byte data = read(addr);
    byte read2 = ((data & 0xff) << 1) | flag_c;
    flag_c = (data>>7)&1;
    set_nz(read2);
    write(addr,read2);
}

void ams_asl(int addr){
    byte data = read(addr);
    flag_c=(data >> 7) & 1;
    byte data2 = data << 1;
    set_nz(data2);
    write(addr, data2);
}

void ams_cpx(byte data){
    //TODO 可能有问题
    byte cmpData = c_reg_x - data;
    set_nz(cmpData);
    flag_c = (cmpData & 0xff00) == 0?1:0;
}

void ams_lsr(int addr){
    byte data = read(addr);
    flag_c = data&1;
    byte data2 = (data & 0xff) >> 1;
    set_nz(data2);
    write(addr,data2);
}

//栈操作----------------------------------
void push_stack(byte data){
    write(0x0100 | (c_reg_s&0xFF),data);
    c_reg_s-=1;
}

void push16_stack(short data){
    push_stack((data>>8)&0xFF);
    push_stack(data&0xFF);
}

byte pop8_stack(){
    c_reg_s++;
    return read(0x0100 | (c_reg_s & 0xFF));
}

int pop16_stack(){
    short pcLow8 = (short) (pop8_stack()&0xFF);
    short pcLow16 = (short) (pop8_stack()&0xFF);
    return  (pcLow16 << 8) | pcLow8;
}

byte flag_merge() {
    return (flag_n << 7) | (flag_v << 6) | 0x20 | (flag_b << 4)| (flag_d << 3) | (flag_i << 2) | (flag_z << 1) | flag_c;
}
void flag_set(byte data) {
    flag_n = (data >> 7) & 0x01;
    flag_v = (data >> 6) & 0x01;
    flag_b = (data >> 4) & 0x01;
    flag_d = (data >> 3) & 0x01;
    flag_i = (data >> 2) & 0x01;
    flag_z = (data >> 1) & 0x01;
    flag_c = data & 0x01;
}

//中断
void interrupt_nmi() {
    if(get_bit(p_reg_2000,7)){
        push16_stack(prgPc);
        push_stack(flag_merge());
        set_i(1);
        prgPc = (read(0xFFFA) & 0xff) | ((read(0xFFFB) & 0xff) << 8);
    }
}

//ppu操作
void render_sprite(int sl,int lineStartIndex) {
    byte spriteHeight = frameSpriteData[sl][0];
    short spritePatternStartAddr = frameSpriteData[sl][1] ? 0x1000:0;
    if(spriteHeight != 0){
        byte bgColorIndex = p_read(0x3F00);
        short palette = palettes[bgColorIndex & 0xff];
        //获取内存中的精灵数据
        for (int i = 0; i < 256; i += 4) {
            short y = (p_spr_ram[i]&0xff)+1;
            short patternIndex =  p_spr_ram[i+1]&0xff;
            //子图形数据
            byte attributeData = p_spr_ram[i+2];
            //背景层级
            byte backgroundPriority = (attributeData>>5)&1;
            //图案垂直翻转
            byte vFlip = (attributeData>>7)&1;
            //图案水平翻转
            byte hFlip = (attributeData>>6)&1;
            if(sl >= y && sl < y + spriteHeight) {
                byte sprX = p_spr_ram[i+3]&0xff;
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
                byte spritePatternData = p_read(spritePatternAddr);
                //获取图案颜色数据
                byte colorData = p_read(spritePatternAddr + 8);
                byte colorHigh = (attributeData & 0x03)<<2;
                //水平翻转 01234567 -> 76543210
                int x = 0,x2 = 8,x3=1;
                if(hFlip == 1) {
                    x = 7; x2 = -1; x3 = -1;
                }
                int index = sl*256 + sprX -lineStartIndex*256*120;

                for (;x!=x2; x+=x3) {
                    int colorLow = ((spritePatternData & 0x80)>>7) | (((colorData & 0x80)>>7) << 1);
                    uint16_t bg = render[index + x];
                    uint16_t sprg = palette;
                    if(colorLow && (backgroundPriority == 0||sprg==bg)){
//                            //获取4位颜色
                        uint16_t colorAddr = 0x3f10 | colorHigh | colorLow;
                        if(colorAddr != 0x3f10){
                            render[index + x] = palettes[p_read(colorAddr)];
                        }
                    }
                    spritePatternData<<=1;
                    colorData<<=1;
                }
            }
        }
    }
}

void render_name_table(int lineStartIndex){
    byte bgColorIndex = p_read(0x3F00)&0xff;
    for (int line=lineStartIndex*120;line<(lineStartIndex+1)*120;line++){
        byte fine_x = frameData[line][1]&0xff;
        byte fine_y = (frameData[line][0] >> 12) & 7;
        short nameTableAddress  = 0x2000 | (frameData[line][0] & 0xFFF);
        short patternStartAddr = frameData[line][2];
        for (int i=0;i<32;i++) {
            //指示哪个tile
            byte coarse_x = nameTableAddress&0x1F;
            byte coarse_y = (nameTableAddress>>5)&0x1F;
            //1 读取name table数据,其实就是Tile图案表索引  (图案+颜色 = 8字节+8字节=16字节)
            byte nameTableData = p_read(nameTableAddress);
            //2 读取图案,图案表起始地址+索引+具体渲染的8字节中的第几字节
            int patternAddress = patternStartAddr + (nameTableData&0xff) * 16 + fine_y;
            //图案表数据
            byte patternData = p_read(patternAddress);
            //图案表颜色数据
            byte colorData = p_read(patternAddress + 8);
            if(patternData==0&&colorData==0){
                for (int j=0; j<8; j++) {
                    int index = line * 256 + i * 8 + j;// - fine_x;
                    render[index-lineStartIndex*256*120] = palettes[bgColorIndex&0xff];
                }
            }else{
                int attributeOffset = 0;
                int attributeAddress = 0x23C0 | (nameTableAddress & 0x0C00);
                if(coarse_x != 0 || coarse_y != 0){
                    //取颜色高两位,属性表数据64byte,每32*32像素一个字节,每32条扫描线占用8字节
                    attributeOffset = ((coarse_y & 2) == 0 ? 0 : 4) + ((coarse_x & 2) == 0 ? 0 : 2);
                    attributeAddress = attributeAddress | ((coarse_y>>2)<<3) | (coarse_x >> 2);
                }
                byte pchb = (p_read(attributeAddress)>>attributeOffset)&3;
                //合并 取最终4位颜色
                for (int j=0; j<8; j++) {
                    int pclb = ((get_bit(colorData,7 - j)<<1)&3) | (get_bit(patternData,7 - j)&1);
                    int index = line * 256 + i * 8 + j;//-fine_x;
                    if(index<0){
                        index = 0;
                    }
                    int paletteIndex = bgColorIndex&0xff;
                    if(pclb!=0) {
                        int colorAddr = 0x3f00 + (pchb<<2|(pclb&0x3));
                        paletteIndex = p_read(colorAddr);
                    }
                    render[index-lineStartIndex*256*120] = palettes[paletteIndex];
                }
            }
            // if coarse X == 31 (coarseX的最大值就是31即11111B,所以到最大值了要切换到下一个nametable)
            if ((nameTableAddress & 0x1F) == 0x1F) {
                nameTableAddress = (nameTableAddress & ~0x1f) ^ 0x400;
            } else {
                nameTableAddress++;
            }
        }
        render_sprite(line,lineStartIndex);
    }
}

void renderSprite(int sl) {
    //获取精灵高度
    byte spriteHeight = get_bit(p_reg_2000,5) == 0?8:16;
    short spritePatternStartAddr = get_bit(p_reg_2000,3) ? 0x1000:0;
    //获取内存中的精灵数据
    for (int i = 0; i < 256; i += 4) {
        short y = (p_spr_ram[i]&0xff)+1;
        short patternIndex = p_spr_ram[i+1]&0xff;
        //子图形数据
        byte attributeData = p_spr_ram[i+2];
        //图案垂直翻转
        byte vFlip = (byte) ((attributeData>>7)&1);
        if(sl >= y && sl < y + spriteHeight) {
            //渲染
            frameSpriteData[sl][0] = spriteHeight;
            frameSpriteData[sl][1] = get_bit(p_reg_2000,3);
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
            byte spritePatternData = p_read(spritePatternAddr);
            //获取图案颜色数据
            byte colorData = p_read(spritePatternAddr + 8);
            //命中非透明背景 sprite0 hit
            if(i==0&&spritePatternData+colorData!=0) {
                set_bit(&p_reg_2002,1,6);
            }
        }
    }
}

void render_name_table2(int scanLineIndex){
    frameData[scanLineIndex][0] = p_vram_addr;
    frameData[scanLineIndex][1] = p_scroll_x&0xff;
    frameData[scanLineIndex][2] = get_bit(p_reg_2000,4)?0x1000:0;
}
void ppu_render(int line) {
    //渲染背景
    if(showBg()) {
        render_name_table2(line);
    }
    //渲染精灵
    if(showSpr()) {
        renderSprite(line);
    }
}
//执行指令
byte cpu_cycle = 113;
uint8_t stop = 1;

void exec_instruction() {
    cpu_cycle = 113;
    byte data,data2,data3;
    byte addr;
    int abs_data = 0;
    while (cpu_cycle >= 0) {
        byte opc = read_program(prgPc++);
//        printf("PC:[%06d] | CYC:[%03d] | PC:[%X] | OPC:[%02X] | R:[A:%02X X:%02X Y:%02X S:%02X] | F:[N:%d V:%d B:%d D:%d I:%d Z:%d C:%d]",
//                ++count,
//                cpu_cycle,
//                (prgPc-1)&0xFFFF,
//                 opc&0xFF,
//                c_reg_a&0xFF,c_reg_x&0xFF,c_reg_y&0xFF,c_reg_s&0xFF,
//                flag_n,flag_v,flag_b,flag_d,flag_i,flag_z,flag_c);
        switch (opc&0xFF) {
            case 0x78:
                set_i(1);
                cpu_cycle-=2;
                break;
            case 0xD8:
                set_d(0);
                cpu_cycle-=2;
                break;
            case 0xA9:
                ams_immed(&c_reg_a);
                set_nz(c_reg_a);
                cpu_cycle-=2;
                break;
            case 0x8D:
                abs_data = ams_abs();
                write(abs_data, c_reg_a);
                cpu_cycle-=4;
                break;
            case 0xA2:
                ams_immed(&c_reg_x);
                set_nz(c_reg_x);
                cpu_cycle-=2;
                break;
            case 0x9A:
                c_reg_s = c_reg_x;
                cpu_cycle-=2;
                break;
            case 0xAD:
                c_reg_a = read(ams_abs());
                set_nz(c_reg_a);
                cpu_cycle-=4;
                break;
            case 0x10:
                data = read_program(prgPc++);
                cpu_cycle -= ams_branch(data,flag_n == 0);
                break;
            case 0xA0:
                ams_immed(&c_reg_y);
                set_nz(c_reg_y);
                cpu_cycle -= 2;
                break;
            case 0xBD:
                abs_data = ams_abs_x(c_reg_x);;
                c_reg_a = read(abs_data);
                set_nz(c_reg_a);
                cpu_cycle -= 4;
                break;
            case 0xC9:
                data = read_program(prgPc++);
                ams_cmp(data);
                cpu_cycle -= 2;
                break;
            case 0xB0:
                data = read_program(prgPc++);
                cpu_cycle -= ams_branch(data,flag_c);
                break;
            case 0xCA:
                c_reg_x -= 1;
                set_nz(c_reg_x);
                cpu_cycle -= 2;
                break;
            case 0xD0:
                data = read_program(prgPc++);
                cpu_cycle -= ams_branch(data,flag_z == 0);
                break;
                //JSR
            case 0x20:
                abs_data = ams_abs();
                push16_stack(prgPc - 1);
                prgPc = abs_data;
                cpu_cycle -= 6;
                break;
            case 0x85:
                cpu_cycle -= st_x(read_program(prgPc++)&0xff,c_reg_a);
                break;
            case 0x86:
                cpu_cycle -= st_x(read_program(prgPc++)&0xff,c_reg_x);
                break;
                //CPX
            case 0xE0:
                ams_cpx(read_program(prgPc++));
                cpu_cycle -= 2;
                break;
                //STA_INDIRECT_Y
            case 0x91:
                write(indirect()+(c_reg_y&0xff),c_reg_a);
                cpu_cycle -= 6;
                break;
            case 0x88:
                c_reg_y--;
                set_nz(c_reg_y);
                cpu_cycle -= 2;
                break;
                //CPY
            case 0xC0:
                data = read_program(prgPc++);
                ams_cpy(data);
                cpu_cycle -= 2;
                break;
                //RTS
            case 0x60:
                prgPc = pop16_stack();
                prgPc+=1;
                cpu_cycle -= 6;
                break;
                //BRK
            case 0x00:
                flag_b = 1;
                flag_i = 1;
                short pc = prgPc + 2;
                push16_stack(pc);
                push_stack(flag_merge());
                prgPc = concat16(read(0xFFFE),read(0xFFFF));
                cpu_cycle -= 7;
                break;
                //SED
            case 0xF8:
                flag_d=1;
                cpu_cycle -= 2;
                break;
                //BIT_ABS
            case 0x2C:
                data = read(ams_abs());
                set_n(data);
                set_v((data>>6)&1);
                set_z(c_reg_a & data);
                cpu_cycle -= 4;
                break;
                //STA_ABS_Y
            case 0x99:
                st_x(ams_abs_x(c_reg_y),c_reg_a);
                cpu_cycle -= 5;
                break;
                //INY
            case 0xC8:
                c_reg_y+=1;
                set_nz(c_reg_y);
                cpu_cycle -= 2;
                break;
                //ORA
            case 0x09:
                data = read_program(prgPc++);
                ams_ora(data);
                cpu_cycle -= 2;
                break;
                //AND
            case 0x29:
                data = read_program(prgPc++);
                c_reg_a &= data;
                set_nz(c_reg_a);
                cpu_cycle -= 2;
                break;
                //TXA
            case 0x8A:
                c_reg_a = c_reg_x;
                set_nz(c_reg_a);
                cpu_cycle -= 2;
                break;
                //JMP_ABS
            case 0x4C:
                prgPc = ams_abs();
                cpu_cycle -= 3;
                break;
                //INC_ABS
            case 0xEE:
                abs_data = ams_abs();
                data = (read(abs_data) + 1)&0xff;
                write(abs_data, data);
                set_nz(data);
                cpu_cycle -= 6;
                break;
                //执行到AC前会触发NMI
                //LDY_ABS
            case 0xAC:
                c_reg_y = read(ams_abs());
                set_nz(c_reg_y);
                cpu_cycle-= 4;
                break;
                //LDX_ABS
            case 0xAE:
                c_reg_x = read(ams_abs());
                set_nz(c_reg_x);
                cpu_cycle-= 4;
                break;
                //LDA_INDIRECT_Y
            case 0xB1:
                c_reg_a = indirect_y();
                set_nz(c_reg_a);
                cpu_cycle-= 5;
                break;
                //LDX_ABS_Y
            case 0xBE:
                c_reg_x = read(ams_abs_x(c_reg_y));
                set_nz(c_reg_x);
                cpu_cycle-= 4;
                break;
                //STA_ABS_X
            case 0x9D:
                abs_data = ams_abs_x(c_reg_x);
                write(abs_data, c_reg_a);
                cpu_cycle-= 5;
                break;
                //LSR
            case 0x4A:
                flag_c = c_reg_a&1;
                c_reg_a = (c_reg_a&0xff) >> 1;
                set_nz(c_reg_a);
                cpu_cycle-= 2;
                break;
                //TAX
            case 0xAA:
                c_reg_x = c_reg_a;
                set_nz(c_reg_x);
                cpu_cycle-=2;
                break;
                //PHA
            case 0x48:
                push_stack(c_reg_a);
                cpu_cycle-=3;
                break;
                //ORA_ZERO
            case 0x05:
                data = read_program(prgPc++);
                c_reg_a |= read(data&0xFF);
                set_nz(c_reg_a);
                cpu_cycle-=3;
                break;
                //PLA
            case 0x68:
                c_reg_a = pop8_stack();
                set_nz(c_reg_a);
                cpu_cycle-=4;
                break;
                //ROL
            case 0x2A:
                data = c_reg_a;
                c_reg_a = (data << 1) | flag_c;
                flag_c = (data >> 7) & 1;
                set_nz(c_reg_a);
                cpu_cycle-=2;
                break;
                //AND_ABS_X
            case 0x3D:
                abs_data = ams_abs_x(c_reg_x);
                c_reg_a &= read(abs_data);
                set_nz(c_reg_a);
                cpu_cycle-=4;
                break;
                //BEQ
            case 0xF0:
                data = read_program(prgPc++);
                cpu_cycle -= ams_branch(data,flag_z);
                break;
                //INX
            case 0xE8:
                c_reg_x += 1;
                set_nz(c_reg_x);
                cpu_cycle-=2;
                break;
                //SEC
            case 0x38:
                flag_c = 1;
                cpu_cycle-=2;
                break;
                //SBC_ABS_Y
            case 0xF9:
                ams_sbc(read(ams_abs_x(c_reg_y)));
                cpu_cycle-=4;
                break;
                //BCC
            case 0x90:
                data = read_program(prgPc++);
                cpu_cycle-= ams_branch(data,flag_c==0);
                break;
                //EOR_ZERO
            case 0x45:
                ams_eor_a(read(read_program(prgPc++)&0xFF));
                cpu_cycle-=3;
                break;
                //CLC
            case 0x18:
                flag_c = 0;
                break;
                //ROR_ABS_X
            case 0x7E:
                abs_data = ams_abs_x(c_reg_x);
                data = read(abs_data);
                data2 = ((data & 0xff) >> 1) | (flag_c << 7);
                flag_c=data&1;
                set_nz(data2);
                write(abs_data,data2);
                cpu_cycle-=7;
                break;
                //DEC_ABS
            case 0xCE:
                abs_data = ams_abs();
                data = read(abs_data)-1;
                write(abs_data, data);
                set_nz(data);
                cpu_cycle-=6;
                break;
                //INC_ZERO
            case 0xE6:
                data = read_program(prgPc++);
                data2 = read(data&0xFF)+1;
                write(data&0xFF,data2);
                set_nz(data2);
                cpu_cycle-=5;
                break;
                //ASL_A
            case 0x0A:
                flag_c = (c_reg_a >> 7) & 1;
                c_reg_a <<= 1;
                set_nz(c_reg_a);
                cpu_cycle-= 2;
                break;
                //TAY
            case 0xA8:
                c_reg_y = c_reg_a;
                set_nz(c_reg_y);
                cpu_cycle-= 2;
                break;
                //JMP_INDIRECT
            case 0x6C:
                abs_data = ams_abs();
                byte low = read(abs_data);
                prgPc = concat16(low,read(abs_data+1));
                cpu_cycle-=5;
                break;
                //LDA_ABS_Y
            case 0xB9:
                abs_data = ams_abs_x(c_reg_y);
                c_reg_a = read(abs_data);
                set_nz(c_reg_a);
                cpu_cycle-=4;
                break;
                //ADC_ABS
            case 0x6D:
                data = read(ams_abs());
                ams_adc(data);
                cpu_cycle-=4;
                break;
                //ADC
            case 0x69:
                data = read_program(prgPc++);
                ams_adc(data);
                cpu_cycle-=2;
                break;
                //STY_ABS
            case 0x8C:
                write(ams_abs(),c_reg_y);
                cpu_cycle-=3;
                break;
                //LDA_ZERO
            case 0xA5:
                data = read_program(prgPc++);
                c_reg_a = read(data&0xFF);
                set_nz(c_reg_a);
                cpu_cycle-=3;
                break;
                //RTI
            case 0x40:
                flag_set(pop8_stack());
                prgPc = pop16_stack();
                cpu_cycle-=6;
                break;
                //DEC_ZERO
            case 0xC6:
                data = read_program(prgPc++) & 0xFF;
                data2 = read(data) - 1;
                write(data,data2);
                set_nz(data2);
                cpu_cycle-=5;
                break;
                //TYA
            case 0x98:
                c_reg_a = c_reg_y;
                set_nz(c_reg_a);
                cpu_cycle-=2;
                break;
                //ADC_ZERO
            case 0x65:
                data = read(read_program(prgPc++)&0xFF);
                ams_adc(data);
                cpu_cycle-=3;
                break;
                //LDX_ZERO
            case 0xA6:
                c_reg_x = read(read_program(prgPc++)&0xFF);
                set_nz(c_reg_x);
                cpu_cycle-=3;
                break;
                //STX_ABS
            case 0x8E:
                abs_data = ams_abs();
                write(abs_data,c_reg_x);
                cpu_cycle-=4;
                break;
                //BMI
            case 0x30:
                data = read_program(prgPc++);
                cpu_cycle-= ams_branch(data,flag_n);
                break;
                //ADC_ABS_Y
            case 0x79:
                abs_data = ams_abs_x(c_reg_y);
                ams_adc(read(abs_data));
                cpu_cycle-= 4;
                break;
                //SBC
            case 0xE9:
                data = read_program(prgPc++);
                ams_sbc(data);
                cpu_cycle-=2;
                break;
                //STY_ZERO
            case 0x84:
                write(read_program(prgPc++)&0xFF,c_reg_y);
                cpu_cycle-=3;
                break;
                //BIT_ZERO
            case 0x24:
                data = read(read_program(prgPc++)&0xFF);
                set_n(data);
                set_v((data>>6)&1);
                set_z(c_reg_a & data);
                cpu_cycle-=3;
                break;
                //LDY_ZERO
            case 0xA4:
                data = read(read_program(prgPc++)&0xFF);
                c_reg_y = data;
                set_nz(c_reg_y);
                cpu_cycle-=3;
                break;
                //CMP_ABS
            case 0xCD:
                ams_cmp(read(ams_abs()));
                cpu_cycle-=4;
                break;
                //CMP_ABS_Y
            case 0xD9:
                ams_cmp(read(ams_abs_x(c_reg_y)));
                cpu_cycle-=4;
                break;
                //EOR
            case 0x49:
                c_reg_a ^= read_program(prgPc++);
                set_nz(c_reg_a);
                cpu_cycle-= 2;
                break;
                //ROL_ZERO
            case 0x26:
                data = read(read_program(prgPc++)&0xFF);
                data2 = (data << 1) | flag_c;
                flag_c = (data >> 7) & 1;
                set_nz(data2);
                write(read_program(prgPc-1)&0xFF,data2);
                cpu_cycle-= 5;
                break;
                //LSR_ZERO
            case 0x46:
                data = read_program(prgPc++)&0xFF;
                data3 = read(data&0xff);
                data2 = (data3 & 0xff) >> 1;
                flag_c = data3&1;
                set_nz(data2);
                write(data,data2);
                cpu_cycle-= 5;
                break;
                //LDY_ABS_X
            case 0xBC:
                data = read( ams_abs_x(c_reg_x));
                c_reg_y = data;
                set_nz(c_reg_y);
                cpu_cycle-=4;
                break;
                //DEC_ABS_X
            case 0xDE:
                abs_data = ams_abs_x(c_reg_x);
                data = read( abs_data)-1;
                write(abs_data,data);
                set_nz(data);
                cpu_cycle-=7;
                break;
                //LSR_ABS
            case 0x4E:
                ams_lsr(ams_abs());
                cpu_cycle-=6;
                break;
                //ROR_A
            case 0x6A:
                data = ((c_reg_a & 0xff) >> 1) | (flag_c << 7);
                flag_c=c_reg_a&1;
                set_nz(data);
                c_reg_a = data;
                cpu_cycle-=2;
                break;
                //ROL_ABS
            case 0x2E:
                abs_data = ams_abs();
                data = read(abs_data);
                data2 = (data << 1) | flag_c;
                flag_c=(data >> 7) & 1;
                set_nz(data2);
                write(abs_data,data2);
                cpu_cycle-=6;
                break;
                //CMP_ZERO
            case 0xC5:
                data = read(read_program(prgPc++)&0xFF);
                ams_cmp(data);
                cpu_cycle-=3;
                break;
                //ORA_ABS
            case 0x0D:
                c_reg_a |= read(ams_abs());
                set_nz(c_reg_a);
                cpu_cycle-=2;
                break;
                //NOP
            case 0xEA:
                cpu_cycle-=2;
                break;
                //LDA_ZERO_X
            case 0xB5:
                data = read(ams_zero(read_program(prgPc++),c_reg_x));
                ams_lda(data);
                cpu_cycle-=4;
                break;
                //STA_ZERO_X
            case 0x95:
                write(ams_zero(read_program(prgPc++),c_reg_x), c_reg_a);
                cpu_cycle-=4;
                break;
                //SBC_ABS
            case 0xED:
                ams_sbc(read(ams_abs()));
                cpu_cycle-=4;
                break;
                //CMP_ZERO_X
            case 0xD5:
                ams_cmp(read(ams_zero(read_program(prgPc++),c_reg_x)));
                cpu_cycle-=4;
                break;
                //SBC_ZERO_X
            case 0xF5:
                ams_sbc(read(ams_zero(read_program(prgPc++),c_reg_x)));
                cpu_cycle-=4;
                break;
                //AND_ABS_Y
            case 0x39:
                c_reg_a &= read(ams_abs_x(c_reg_y));
                set_nz(c_reg_a);
                cpu_cycle-=4;
                break;
                //ASL_ABS
            case 0x0E:
                abs_data = ams_abs();
                data = read(abs_data);
                flag_c=(data >> 7) & 1;
                data2 = data << 1;
                set_nz(data2);
                write(abs_data,data2);
                cpu_cycle-=6;
                break;
                //AND_ABS
            case 0x2D:
                abs_data = ams_abs();
                c_reg_a &= read(abs_data);
                set_nz(c_reg_a);
                cpu_cycle-=4;
                break;
                //ADC_ABS_X
            case 0x7D:
                abs_data = ams_abs_x(c_reg_x);
                ams_adc(read(abs_data));
                cpu_cycle-=4;
                break;
                //CMP_ABS_X
            case 0xDD:
                abs_data = ams_abs_x(c_reg_x);
                ams_cmp(read(abs_data));
                cpu_cycle-=2;
                break;
                //ADC_ZERO_X
            case 0x75:
                abs_data = ams_zero(read_program(prgPc++),c_reg_x);
                ams_adc(read(abs_data));
                cpu_cycle-=2;
                break;
                //PHP
            case 0x08:
                push_stack(flag_merge());
                cpu_cycle-=3;
                break;
                //CPX_ZERO
            case 0xE4:
                ams_cpx(read(read_program(prgPc++)&0xFF));
                cpu_cycle-=3;
                break;
                //SBC_ZERO
            case 0xE5:
                data = read(read_program(prgPc++)&0xff);
                ams_sbc(data);
                cpu_cycle-=3;
                break;
                //AND_ZERO
            case 0x25:
                data = read(read_program(prgPc++)&0xff);
                ams_and(data);
                cpu_cycle-=3;
                break;
                //PLP
            case 0x28:
                flag_set(pop8_stack());
                cpu_cycle-=4;
                break;
                //ASL_ZERO
            case 0x06:
                ams_asl(read_program(prgPc++));
                cpu_cycle-=5;
                break;
                //AND_INDIRECT_Y
            case 0x31:
                ams_and(indirect_y());
                cpu_cycle-=5;
                break;
                //LDY_ZERO_X
            case 0xB4:
                addr = read_program(prgPc++);
                int data = ams_zero(addr,c_reg_x);
                data2 = read(data);
                c_reg_y = data2;
                set_nz(c_reg_y);
                cpu_cycle-=4;
                break;
                //EOR_ABS
            case 0x4D:
                abs_data = ams_abs();
                data = read(abs_data);
                flag_c=(data >> 7) & 1;
                data2 = data << 1;
                set_nz(data2);
                write(addr, data2);
                cpu_cycle-=5;
                break;
                //TSX
            case 0xBA:
                c_reg_x = c_reg_s;
                set_nz(c_reg_x);
                cpu_cycle-=2;
                break;
                //DEC_ZERO_X
            case 0xD6:
                data = read_program(prgPc++);
                int addr = ams_zero(data,c_reg_x);
                data2 = read(addr) - 1;
                write(addr,data2);
                set_nz(data2);
                cpu_cycle-=6;
                break;
                //EOR_ZERO_X
            case 0x55:
                addr = read_program(prgPc++);
                ams_eor_a(read(ams_zero(addr, c_reg_x)));
                cpu_cycle-=4;
                break;
                //AND_ZERO_X
            case 0x35:
                addr = read_program(prgPc++);
                ams_and(read(ams_zero(addr, c_reg_x)));
                cpu_cycle-=4;
                break;
                //STY_ZERO_X
            case 0x94:
                addr = read_program(prgPc++);
                data = ams_zero(addr, c_reg_x);
                write(data,c_reg_y);
                cpu_cycle-=4;
                break;
                //INC_ZERO_X
            case 0xF6:
                addr = ams_zero(read_program(prgPc++), c_reg_x);
                data = read(addr) + 1;
                write(addr, data);
                set_nz(data);
                cpu_cycle-=6;
                break;
                //ORA_ZERO_X
            case 0x15:
                addr = read_program(prgPc++);
                ams_ora(read(addr));
                cpu_cycle-=4;
                break;
                //BVC
            case 0x50:
                data = read_program(prgPc++);
                cpu_cycle-=ams_branch(data,!flag_v);
                break;
                //SBC_ABS_X
            case 0xFD:
                addr = ams_abs_x(c_reg_x);
                ams_sbc(read(addr));
                cpu_cycle-=4;
                break;
                //CPY_ZERO
            case 0xC4:
                data = read(read_program(prgPc++)&0xFF);
                ams_cpy(data);
                cpu_cycle-=3;
                break;
                //ORA_INDIRECT_Y
            case 0x11:
                ams_ora(indirect_y());
                cpu_cycle-=5;
                break;
                //ORA_ABS_Y
            case 0x19:
                addr = ams_abs_x(c_reg_y);
                ams_ora(read(addr));
                cpu_cycle-=4;
                break;
                //ADC_INDIRECT_Y
            case 0x71:
                ams_adc(indirect_y());
                cpu_cycle-=5;
                break;
                //ORA_ABS_X
            case 0x1D:
                addr = ams_abs_x(c_reg_x);
                ams_ora(read(addr));
                cpu_cycle-=4;
                break;
                //CMP_INDIRECT_Y
            case 0xD1:
                ams_cmp(indirect_y());
                cpu_cycle-=5;
                break;
                //SBC_INDIRECT_Y
            case 0xF1:
                ams_sbc(indirect_y());
                cpu_cycle-=5;
                break;
                //LDA_INDIRECT_X
            case 0xA1:
                ams_lda(indirect_x());
                cpu_cycle-=6;
                break;
                //STA_INDIRECT_X
            case 0x81:
                st_x(indirect_x()&0xff,c_reg_a);
                cpu_cycle-=6;
                break;
                //INC_ABS_X
            case 0xFE:
                addr = ams_abs_x(c_reg_x);
                data = read(addr) + 1;
                write(addr, data);
                set_nz(data);
                cpu_cycle-=7;
                break;
                //CPY_ABS
            case 0xCC:
                addr = ams_abs();
                ams_cpy(read(addr));
                cpu_cycle-=4;
                break;
                //ROR
            case 0x66:
                ams_ror(read_program(prgPc++)&0xff);
                cpu_cycle-=5;
                break;
                //ROR_ABS
            case 0x6E:
                ams_ror(ams_abs());
                cpu_cycle-=6;
                break;
                //BVS
            case 0x70:
                cpu_cycle-= ams_branch(read_program(prgPc++),flag_v);
                break;
                //ROL_ABS_X
            case 0x3E:
                ams_rol(ams_abs_x(c_reg_x));
                cpu_cycle-=7;
                break;
                //ASL_ABS_X
            case 0x1E:
                ams_asl(ams_abs_x(c_reg_x));
                cpu_cycle-=7;
                break;
                //EOR_ABS_X
            case 0x5D:
                ams_eor_a(read(ams_abs_x(c_reg_x)));
                cpu_cycle-=4;
                break;
                //CPX_ABS
            case 0xEC:
                ams_cpx(read(ams_abs()));
                cpu_cycle-=4;
                break;
                //ROR_ZERO_X
            case 0x76:
                ams_ror(ams_zero(read_program(prgPc++),c_reg_x));
                cpu_cycle-=6;
                break;
                //LSR_ZERO_X
            case 0x56:
                ams_lsr(ams_zero(read_program(prgPc++),c_reg_x));
                cpu_cycle-=6;
                break;
                //ROL_ZERO_X
            case 0x36:
                ams_rol(ams_zero(read_program(prgPc++),c_reg_x));
                cpu_cycle-=6;
                break;
                //LDX_ZERO_Y
            case 0xB6:
                c_reg_x = read(ams_zero(read_program(prgPc++),c_reg_y));
                set_nz(c_reg_x);
                cpu_cycle-=4;
                break;
                //EOR_INDIRECT_Y
            case 0x51:
                ams_eor_a(indirect_y());
                cpu_cycle-=5;
                break;
                //STX_ZERO_Y
            case 0x96:
                st_x(ams_zero(read_program(prgPc++),c_reg_y),c_reg_x);
                cpu_cycle-=4;
                break;
                //CLI
            case 0x58:
                flag_i = 0;
                cpu_cycle-=2;
                break;
                //LSR_ABS_X
            case 0x5E:
                ams_lsr(ams_abs_x(c_reg_x));
                cpu_cycle-=7;
                break;
            default:
                cpu_cycle-=0;
                printf("unknown ins %X\n",opc&0xFF);
                break;
        }
//        printf("\n");
//        fflush(stdout);
    }
}

void coarseY(){
    if (showBg()||showSpr()) {
        // if fine Y < 7
        if ((p_vram_addr & 0x7000) != 0x7000) {
            // increment fine Y
            p_vram_addr += 0x1000;
        }else{
            // fine Y = 0
            p_vram_addr &= ~0x7000;
            // let y = coarse Y
            int y = (p_vram_addr & 0x03E0) >> 5;
            if (y == 29){
                // coarse Y = 0
                y = 0;
                // switch vertical nametableelse if (y == 31)
                p_vram_addr ^= 0x0800;
            }else if (y == 31) {
                // coarse Y = 0, nametable not switched
                y = 0;
            }else{
                // increment coarse Y
                y += 1;
            }
            // put coarse Y back into v
            p_vram_addr = (p_vram_addr & ~0x03E0) | (y << 5);
        }
    }
}

void NES_Start() {

    uint8_t n = *(__IO uint8_t*)(0x08020000);
    uint8_t e = *(__IO uint8_t*)(0x08020001);
    uint8_t s = *(__IO uint8_t*)(0x08020002);
//    printf("%c",n);
//    printf("%c",e);
//    printf("%c\n",s);
    int rom_prg_size = *(__IO uint8_t*)(0x08020004);
    int rom_chr_size = *(__IO uint8_t*)(0x08020005);

    program_length = 16 * 1024 * rom_prg_size;
    int chr_length = 8 * 1024 * rom_chr_size;
    //printf("rom_prg_size:%d\nrom_chr_size:%d\n",program_length,chr_length);

    LCD_SetRegion(30,0,255+30,LCD_HEIGHT);
    LL_DMA_ConfigAddresses(DMA2,LL_DMA_STREAM_2,(uint32_t)&render, LL_SPI_DMA_GetRegAddr(SPI1),LL_DMA_GetDataTransferDirection(DMA2, LL_DMA_STREAM_2));
    LL_DMA_SetDataLength(DMA2, LL_DMA_STREAM_2, 256*120);
    while (1) {
        for (int l = 0; l < 240; l++) {
            if(showBg() || showSpr()){
                p_vram_addr = (p_vram_addr & 0xfbe0) | (p_vram_temp_addr & 0x041f);
            }
            ppu_render(l);
            exec_instruction();
            coarseY();
        }
        if(showBg() || showSpr()) {
            LCD_CS_CLR
            LCD_DC_SET
            for (int i = 0; i < 2; ++i) {
                render_name_table(i);
                LL_DMA_EnableStream(DMA2, LL_DMA_STREAM_2);
                LL_mDelay(10);
            }
            LCD_CS_SET;
        }
        //设置vblank true
        set_bit(&p_reg_2002,1,7);
        set_bit(&p_reg_2002,0,6);
        exec_instruction();
        //NMI中断
        interrupt_nmi();
        //242-260
        for (int i = 242; i < 252; i++) {
            exec_instruction();
        }
        set_bit(&p_reg_2002,0,7);
        //vblank结束后 如果有渲染 将t复制到v
        if(showBg()||showSpr()){
            p_vram_addr = p_vram_temp_addr;
        }
    }
}