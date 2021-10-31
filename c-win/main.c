#include "stdio.h"
#include "Windows.h"
#include "windowsx.h"

#define byte char

byte get_bit(byte data,byte index){
    return (data>>index)&1;
}

void set_bit(byte *data ,byte set_data,byte index){
    if(set_data) {
        (*data) |= 1<<index;
    }else{
        (*data) &= ~(1<<index);
    }
}

//文件
FILE *fr,*lf;

long count = 0;

int prgPc = 0x8000;
//状态寄存器
byte flag_n = 0,flag_v = 0,flag_b = 1,flag_d = 0,flag_i = 1,flag_z = 0,flag_c = 0;
//CPU寄存器
byte c_reg_a,c_reg_x,c_reg_y,c_reg_s=0xFF;
//CPU内存
byte c_mem[0x8000];
byte program_data[0x10000];
//PPU寄存器
byte p_reg_2000,p_reg_2001,p_reg_2002 = 0,p_reg_2003,p_reg_2005,p_reg_2007,p_reg_2006_flag,p_reg_2007;
int p_reg_2006;
//PPU内存
byte p_mem[0x3FFF];
//精灵数据
byte p_spr_ram[256];

byte palettes[65][3] = {
     { 0x75, 0x75, 0x75 },{ 0x27, 0x1B, 0x8F },{ 0x00, 0x00, 0xAB },{ 0x47, 0x00, 0x9F },{ 0x8F, 0x00, 0x77 },{ 0xAB, 0x00, 0x13 },{ 0xA7, 0x00, 0x00 },{ 0x7F, 0x0B, 0x00 },
     { 0x43, 0x2F, 0x00 },{ 0x00, 0x47, 0x00 },{ 0x00, 0x51, 0x00 },{ 0x00, 0x3F, 0x17 },{ 0x1B, 0x3F, 0x5F },{ 0x00, 0x00, 0x00 },{ 0x05, 0x05, 0x05 },{ 0x05, 0x05, 0x05 },
     { 0xBC, 0xBC, 0xBC },{ 0x00, 0x73, 0xEF },{ 0x23, 0x3B, 0xEF },{ 0x83, 0x00, 0xF3 },{ 0xBF, 0x00, 0xBF },{ 0xE7, 0x00, 0x5B },{ 0xDB, 0x2B, 0x00 },{ 0xCB, 0x4F, 0x0F },
     { 0x8B, 0x73, 0x00 },{ 0x00, 0x97, 0x00 },{ 0x00, 0xAB, 0x00 },{ 0x00, 0x93, 0x3B },{ 0x00, 0x83, 0x8B },{ 0x11, 0x11, 0x11 },{ 0x09, 0x09, 0x09 },{ 0x09, 0x09, 0x09 },
     { 0xFF, 0xFF, 0xFF },{ 0x3F, 0xBF, 0xFF },{ 0x5F, 0x97, 0xFF },{ 0xA7, 0x8B, 0xFD },{ 0xF7, 0x7B, 0xFF },{ 0xFF, 0x77, 0xB7 },{ 0xFF, 0x77, 0x63 },{ 0xFF, 0x9B, 0x3B },
     { 0xF3, 0xBF, 0x3F },{ 0x83, 0xD3, 0x13 },{ 0x4F, 0xDF, 0x4B },{ 0x58, 0xF8, 0x98 },{ 0x00, 0xEB, 0xDB },{ 0x66, 0x66, 0x66 },{ 0x0D, 0x0D, 0x0D },{ 0x0D, 0x0D, 0x0D },
     { 0xFF, 0xFF, 0xFF },{ 0xAB, 0xE7, 0xFF },{ 0xC7, 0xD7, 0xFF },{ 0xD7, 0xCB, 0xFF },{ 0xFF, 0xC7, 0xFF },{ 0xFF, 0xC7, 0xDB },{ 0xFF, 0xBF, 0xB3 },{ 0xFF, 0xDB, 0xAB },
     { 0xFF, 0xE7, 0xA3 },{ 0xE3, 0xFF, 0xA3 },{ 0xAB, 0xF3, 0xBF },{ 0xB3, 0xFF, 0xCF },{ 0x9F, 0xFF, 0xF3 },{ 0xDD, 0xDD, 0xDD },{ 0x11, 0x11, 0x11 },{ 0x11, 0x11, 0x11 }
    ,{ 0x00, 0x00, 0x00 }
};

byte read_program(int addr){
    if(addr < 0x8000){
        return c_mem[addr];
    } else {
        return program_data[addr-0x8000];
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
byte p_read(int addr){
    byte ppuDatum = p_mem[addr];
    fprintf(lf," | PR:[addr:%02X DATA:%d]",addr&0xFFFF,ppuDatum&0xff);
    return ppuDatum;
}
void p_write(int addr,byte data){
    //写入 $3F00-3FFF 的 D7-D6 字节被忽略.
    if(addr >= 0x3F00 && addr <= 0x3FFF) {
        data = data & 0x3f;
        if(addr==0x3F00){
            p_mem[0x3F10] = data;
        }else if(addr==0x3F10){
            p_mem[0x3F00] = data;
        }
    }
    fprintf(lf," | PWR:[addr:%02X INDEX:%d DATA:%d]",addr&0xFFFF,addr&0xFFFF,data);
    p_mem[addr] = data;
}
void p_write_spr(byte addr, byte data){
    p_spr_ram[addr&0xFF] = data;
}

void print8(byte data){
    for (int i = 7; i >=0; i--) {
        printf("%d",get_bit(data,i));
    }
}

//内存操作----------------------------------

byte read(int addr){
    byte ret_p_reg_2002;
    int temp_p_reg_2006;
    fprintf(lf," | RD:[addr:%02X INDEX:%d]",addr,addr);
    fflush( stdout );
    switch (addr) {
        //读PPUSTATUS状态寄存器
        case 0x2002:
            ret_p_reg_2002 = p_reg_2002;
            set_bit(&p_reg_2002,0,7);
            p_reg_2006_flag=0;
            return ret_p_reg_2002;
        case 0x2007:
            temp_p_reg_2006 = p_reg_2006;
            p_reg_2006 += get_bit(p_reg_2000,2)?32:1;
            if(addr <= 0x3EFF) {
                //读取PPU
                byte res = p_reg_2007;
                p_reg_2007 = p_read(temp_p_reg_2006);
                return res;
            }else if(addr <= 0x3FFF) {
                //读取调色板
                printf("%s","读取调色板");
            }
            break;
        case 0x4016:
            break;
        case 0x4017:
            break;
    }
    return read_program(addr);
}

void write(int addr,byte data){
    fprintf(lf," | WR:[ADDR:%02X INDEX:%d DATA:%d]",addr&0xFFFF,addr&0xFFFF,data);
    fflush( stdout );
    short start;
    switch (addr) {
        case 0x2000:
            p_reg_2000 = data;
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
            p_reg_2005 = data;
            break;
        case 0x2006:
            if(!p_reg_2006_flag) {
                //第一次写将写入高6位;
                p_reg_2006 = (short) ((data&0x3F) << 8);
            }else {
                //第二次写将写入低8位
                p_reg_2006|=(data&0xFF);
            }
            p_reg_2006_flag = !p_reg_2006_flag;
            break;
        case 0x2007:
            p_write(p_reg_2006,data);
            p_reg_2006 += get_bit(p_reg_2000,2)?32:1;
            break;
        //OAM DMA register (high byte)
        case 0x4014:
            start = (short) (data*0x100);
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
    byte r1 = read(data);
    byte r2 = read(data+1);
    return concat16(r1,r2);
}

byte indirect_y(byte data){
    char low = read(data & 0xFF);
    char high = read((data & 0xFF) + 1);
    int addr = concat16(low,high)+ (c_reg_y & 0xFF);
    return read(addr);
}
int indirect_x(byte data){
    byte addr = (byte) ((data & 0xFF) + (c_reg_x & 0xFF));
    return concat16(read(data&0xFF), read((addr & 0xFF) + 1));
}
void ams_and(byte data){
    c_reg_a &= data;
    set_nz(c_reg_a);
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


byte getPatternColorHighData(byte attributeData,int i,int line) {
    int x = i % 4;
    int y = line % 32 / 8;
    byte high2;
    if(y<2) {
        if(x<2) {
            high2 = (attributeData&1) + (((attributeData>>1)&1)<<1);
        }else {
            high2 = ((attributeData>>2)&1) + (((attributeData>>3)&1)<<1);
        }
    }else {
        if(x<2) {
            high2 = ((attributeData>>4)&1) + (((attributeData>>5)&1)<<1);
        }else {
            high2 = ((attributeData>>6)&1)+ (((attributeData>>7)&1)<<1);
        }
    }
    return high2;
}

void render_name_table(int scanLineIndex,short nametableStartAddr,short patternStartAddr,byte render[256][3]){
    //32*30个Tile = (256*240 像素)
    for (int i=0;i<32;i++) {
        //1 读取name table数据,其实就是Tile图案表索引  (图案+颜色 = 8字节+8字节=16字节)
        int nameTableData = (p_read((nametableStartAddr + (scanLineIndex/8) * 32) + i)&0xFF) * 16;
        //2 读取图案,图案表起始地址+索引+具体渲染的8字节中的第几字节
        int patternAddr = patternStartAddr + nameTableData + (scanLineIndex % 8);
        int patternColor = patternAddr + 8;
        //图案表数据
        byte patternData = p_read(patternAddr);
        //图案表颜色数据
        byte colorData = p_read(patternColor);
        //取每像素的低两位颜色
        byte patternColorLowData[7];
        for(byte i=7;i>=0;i--) {
            patternColorLowData[i] = (byte) (((get_bit(colorData,i)<<1)&3) | (get_bit(patternData,i)&1));
        }
        //取颜色高两位,属性表数据64byte,每32*32像素一个字节,每32条扫描线占用8字节
        byte attributeData = p_read(nametableStartAddr + 0x3C0 + (scanLineIndex/32*8)+i/4);
        byte patternColorHighData = getPatternColorHighData(attributeData,i,scanLineIndex);
        byte p0 = p_read(0x3F00);
        //合并 取最终4位颜色
        for (int i1 = 0; i1 <8; i1++) {
            int patternColorLowBit = patternColorLowData[7 - i1];
            //透明色 显示背景色
            if(patternColorLowBit == 0) {
                render[i*8+i1][0] = palettes[p0][0];
                render[i*8+i1][1] = palettes[p0][1];
                render[i*8+i1][2] = palettes[p0][2];
            }else {
                int colorAddr = 0x3f00 + (((patternColorHighData << 2) & 0xF) | (patternColorLowBit & 0x3));
                int paletteIndex = p_read(colorAddr);
                render[i*8+i1][0] = palettes[paletteIndex][0];
                render[i*8+i1][1] = palettes[paletteIndex][1];
                render[i*8+i1][2] = palettes[paletteIndex][2];
            }
        }
        print8(patternData);
//        free(patternColorLowData);
    }
    printf("\n");
}

void renderSprite(int sl,short spritePatternStartAddr, int spriteSize,byte render[256][3]) {
    //获取精灵高度
    byte spriteHeight = spriteSize == 0?8:16;
    //获取内存中的精灵数据
    for (int i = 0; i < 256; i += 4) {
        short y = (short) (p_spr_ram[i]&0xff);
        short patternIndex = (short) (p_spr_ram[i+1]&0xff);
        //子图形数据
        byte attributeData = p_spr_ram[i+2];
        //[5] 背景层级
        byte backgroundPriority = (attributeData>>5)&1;
        //图案垂直翻转
        byte vFlip = (attributeData>>7)&1;
        //图案水平翻转
        byte hFlip = (attributeData>>6)&1;
        short x = p_spr_ram[i+3]&0xff;
        if(sl >= y && sl <= y + spriteHeight) {
            //获取图案地址
            if(spriteHeight == 16) {
                spritePatternStartAddr = (short) (get_bit(patternIndex,0) == 0 ? 0x0000:0x1000);
            }
            int spritePatternAddr = spritePatternStartAddr + patternIndex * 16 + (sl - y);
            byte spritePatternData = p_read(spritePatternAddr);
//                if(vFlip == 1) {
//                    byte[] patterBytes = MemUtil.toBits(spritePatternData);
//                    for (int j = 0; j < 4; j++) {
//                        int temp = patterBytes[j];
//                        patterBytes[j] = patterBytes[7-j];
//                        patterBytes[7-j] = (byte) temp;
//                    }
//                    spritePatternData = MemUtil.bitsToByte(patterBytes);
//                }
            //获取图案颜色数据
            byte colorData = p_read(spritePatternAddr + 8);
            byte patternColorLowData[7];
            for(int i=7;i>=0;i--) {
                patternColorLowData[i] = (byte) (((get_bit(colorData,i)<<1)&3) | (get_bit(spritePatternData,i)&1));
            }
            byte patternColorHighData = attributeData & 0x03;
            //命中非透明背景 sprite hit
            if(spritePatternData + colorData != 0 && get_bit(p_reg_2001,1)!= 0 &&get_bit(p_reg_2001,2) != 0) {
                set_bit(&p_reg_2002,1,6);
            }
            for (int i1 = 0; i1 < 8; i1++) {
                if(backgroundPriority == 0) {
                    //获取4位颜色
                    int colorAddr = 0x3f10 + (((patternColorHighData << 2) & 0xF) | ((patternColorLowData[7 - i1]) & 0x3));
                    if(colorAddr != 0x3f10) {
                        byte pRead = p_read(colorAddr);
                        render[x + i1][0] = palettes[pRead][0];
                        render[x + i1][1] = palettes[pRead][1];
                        render[x + i1][2] = palettes[pRead][2];
                    }
                }
            }
            if(hFlip == 1) {
                for (int j = 0; j < 4; j++) {
                    byte *temp = render[x + j];
                    render[x + j][0] = render[(x+7)-j][0];
                    render[x + j][1] = render[(x+7)-j][1];
                    render[x + j][2] = render[(x+7)-j][2];

                    render[(x+7)-j][0]=temp[0];
                    render[(x+7)-j][1]=temp[1];
                    render[(x+7)-j][2]=temp[2];
                }
            }
        }
    }
}


//short[][] render = new short[256][3];
void ppu_render(int line,byte render[256][3]){
    //渲染背景
    if(get_bit(p_reg_2001,3)) {
        byte ntAddr = get_bit(p_reg_2000,0);
        byte bgAddr = get_bit(p_reg_2000,4);
        short nameTableAddr = 0;
        short patternAddr = 0;
        if(ntAddr == 0x00) {
            nameTableAddr = 0x2000;
        }else if (ntAddr == 0x01){
            nameTableAddr = 0x2400;
        }else if (ntAddr == 0x10){
            nameTableAddr = 0x2800;
        }else if (ntAddr == 0x11){
            nameTableAddr = 0x2C00;
        }
        if(bgAddr == 1) {
            patternAddr = 0x1000;
        }
        render_name_table(line,nameTableAddr,patternAddr,render);
    }
    //渲染精灵
    if(get_bit(p_reg_2001,4)) {
        short spritePatternAddr = get_bit(p_reg_2000,3)==0 ? 0:0x1000;
        int spriteSize = get_bit(p_reg_2000,5);
        renderSprite(line,spritePatternAddr,spriteSize,render);
    }else if(get_bit(p_reg_2001,3) == 0 && get_bit(p_reg_2001,4) == 0) {
        //无背景和无精灵 渲染背景色
        byte palette[3];
        byte pRead = p_read(0x3F00);
        palette[0] = palettes[pRead][0];
        palette[1] = palettes[pRead][1];
        palette[2] = palettes[pRead][2];
        int i;
        for (i = 0; i < 256; i++) {
            render[i][0] = palette[0];
            render[i][1] = palette[1];
            render[i][2] = palette[2];
        }

    }
}

void exec_instruction() {
    byte cpu_cycle = 113;
    byte data,data2,data3;
    int abs_data = 0;
    while (cpu_cycle>=0) {
        byte opc = read_program(prgPc++);
        fprintf(lf,"\n");
        fprintf(lf,"PC:[%06d] | CYC:[%03d] | PC:[%X] | OPC:[%02X] | R:[A:%02X X:%02X Y:%02X S:%02X] | F:[N:%d V:%d B:%d D:%d I:%d Z:%d C:%d] | 2002:%02X",
               ++count,
               cpu_cycle,
               (prgPc-1)&0xFFFF,
               opc&0xFF,
               c_reg_a&0xFF,c_reg_x&0xFF,c_reg_y&0xFF,c_reg_s&0xFF,
               flag_n,flag_v,flag_b,flag_d,flag_i,flag_z,flag_c,
               p_reg_2002&0xff);
        fflush( stdout );
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
                if (flag_n == 0) {
                    cpu_cycle -= 2 + (prgPc & 0xff00) == ((prgPc + data) & 0xff00) ? 1 : 2;
                    prgPc += data;
                    break;
                }
                cpu_cycle -= 2;
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
                if(flag_c) {
                    cpu_cycle -= 2 + (prgPc & 0xff00) == ((prgPc + data) & 0xff00) ? 1 : 2;
                    prgPc += data;
                    break;
                }
                cpu_cycle -= 2;
                break;
            case 0xCA:
                c_reg_x -= 1;
                set_nz(c_reg_x);
                cpu_cycle -= 2;
                break;
            case 0xD0:
                data = read_program(prgPc++);
                if(flag_z == 0) {
                    cpu_cycle -= 2 + (prgPc & 0xff00) == ((prgPc + data) & 0xff00) ? 1 : 2;
                    prgPc += data;
                    break;
                }
                cpu_cycle -= 2;
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
            case 0xE0:
                data = c_reg_x - read_program(prgPc++);
                set_nz(data);
                flag_c = (data & 0xff00) == 0?1:0;
                cpu_cycle -= 2;
                break;
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
                short cmpData = (short) ((c_reg_y&0xFF) - (data&0xFF));
                set_nz(cmpData);
                flag_c = (cmpData & 0xff00) == 0;
                cpu_cycle -= 2;
                break;
            //RTS
            case 0x60:
                prgPc = pop16_stack();
                prgPc+=1;
                cpu_cycle -= 6;
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
                c_reg_a |= data;
                set_nz(c_reg_a);
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
                c_reg_a = indirect_y(read_program(prgPc++));
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
                if (flag_z) {
                    cpu_cycle -= 2 + (prgPc & 0xff00) == ((prgPc + data) & 0xff00) ? 1 : 2;
                    prgPc+=data;
                    break;
                }
                cpu_cycle-=2;
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
                if(flag_c == 0) {
                    cpu_cycle -= 2 + (prgPc & 0xff00) == ((prgPc + data) & 0xff00) ? 1 : 2;
                    prgPc+=data;
                    break;
                }
                cpu_cycle -=2;
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
            //EOR_ZERO
            case 0x45:
                data = read( read_program(prgPc++)&0xFF);
                c_reg_a ^= data;
                set_nz(c_reg_a);
                cpu_cycle-=3;
                break;
            //CLC
            case 0x18:
                flag_c = 0;
                cpu_cycle-=2;
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
                char low = read(abs_data);
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
                cpu_cycle-=4;
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
                if(flag_n == 1) {
                    cpu_cycle-= 2 + (prgPc & 0xff00) == ((prgPc+ data) & 0xff00) ? 1 : 2;
                    prgPc+=data;
                    break;
                }
                cpu_cycle-= 2;
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
                abs_data = ams_abs();
                data = read(abs_data);
                flag_c = data&1;
                data2 = (data & 0xff) >> 1;
                set_nz(data2);
                write(abs_data,data2);
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
                cpu_cycle-=4;
                break;
            //NOP
            case 0xEA:
                cpu_cycle-=2;
                break;
            //LDA_ZERO_X
            case 0xB5:
                data = read(ams_zero(read_program(prgPc++),c_reg_x));
                c_reg_a = data;
                set_nz(c_reg_a);
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
            default:
                printf("unknown ins %X\n",opc&0xFF);
                break;
        }
    }
}


int main() {
    setbuf(stdout,NULL);

    fr = fopen("D:/workspace/wnes/1.nes", "rb");
    if(fr == NULL) {//若打开文件失败则退出
        puts("不能打开文件！");
        return 0;
    }

    int n = fgetc(fr);
    int e = fgetc(fr);
    int s = fgetc(fr);
    int b = fgetc(fr);
    printf("%c-%c-%c-%d\n",n,e,s,b);
    int rom_prg_size = fgetc(fr);
    int rom_chr_size = fgetc(fr);
    printf("rom_prg_size: %d\n",rom_prg_size);
    printf("rom_chr_size: %d\n",rom_chr_size);

    int rom_control_data = fgetc(fr);
    int rom_control_data_2 = fgetc(fr);
    long zero[2];
    fread(zero, 8, 1, fr);

    int program_length = 16 * 1024 * rom_prg_size;
    printf("program_size: %d\n", program_length);

    byte p_data[program_length + 1];
    fread(p_data, program_length, 1, fr);
    for (int i = 0; i < program_length + 1; ++i) {
        program_data[i] = p_data[i];
    }
    int chr_length = 8 * 1024 * rom_chr_size;
    byte chrData[chr_length + 1];
    fread(chrData, chr_length, 1, fr);

    for (int i = 0; i < chr_length + 1; ++i) {
        p_mem[i] = chrData[i];
    }

    fclose(fr);

    lf = fopen("D:/WorkSpace/wnes/exec.txt","w");

    while (1) {
        byte renderBuff[256*240][3];
        for (int l = 0; l < 240; l++) {
            byte render[256][3];
            exec_instruction();
            //render
            ppu_render(l,render);
            for(int r = 0; r < 256; r++) {
                renderBuff[l * 256 + r][0] = render[r][0];
                renderBuff[l * 256 + r][1] = render[r][1];
                renderBuff[l * 256 + r][2] = render[r][2];
            }
        }
//        exec_instruction();
        //设置vblank true
        set_bit(&p_reg_2002,1,7);
        //NMI中断
        interrupt_nmi();
        //242-
        for (int i = 242; i < 262; i++) {
            if( i == 261 ) {
                //Sprite 0 Hit false 第7位
                set_bit(&p_reg_2002,0,6);
                //设置vblank false 第8位
                set_bit(&p_reg_2002,0,7);
            }
            exec_instruction();
        }
    }

    return 0;
}



