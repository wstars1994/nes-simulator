var rpg16KSize, chr8KSize;
var mirrorType;
var mapper;
var prgData = [],
	chrData = [];
var c_mem = [],
	p_mem = [],
	p_spr_ram = [];
var prgPc = 0;
var cpu_cycle = 113;

//状态寄存器
var flag_n = 0,
	flag_v = 0,
	flag_b = 1,
	flag_d = 0,
	flag_i = 1,
	flag_z = 0,
	flag_c = 0;
//CPU寄存器
var c_reg_a=0, c_reg_x=0, c_reg_y=0, c_reg_s = 0xFF;
//PPU寄存器
var p_reg_2000=0, p_reg_2001=0, p_reg_2002=0, p_reg_2003=0, p_reg_2005=0, p_reg_2007=0, p_reg_2006_flag=0, p_reg_2007=0;

function get_bit(data,index){
    return (data>>index)&1;
}


//标志位操作-------------------------------
function set_i(flag) {
	flag_i = flag;
}

function set_d(flag) {
	flag_d = flag;
}

function set_n(flag) {
	flag_n = get_bit(flag, 7);
}

function set_z(flag) {
	flag_z = flag == 0?1:0;
}

function set_nz(flag) {
	set_n(flag);
	set_z(flag);
}

function set_v(flag) {
	flag_v = flag;
}
//标志位操作结束-------------------------------


//指令操作---------------------------
function concat16(low, high){
    return (low & 0xFF) | ((high&0xff)<<8);
}

function ams_abs() {
    let low = read_program(prgPc++);
    let high = read_program(prgPc++);
    return concat16(low,high);
}

function ams_abs_x(reg){
	switch(reg){
		case 'x':
			reg = c_reg_x;
			break;
		case 'y':
			reg = c_reg_y;
			break;
	}
    return ams_abs() + (reg & 0xff);
}
function ams_cmp(data){
    let cmpData = (c_reg_a & 0xff) - (data & 0xff);
    flag_n = (cmpData >> 7) & 1;
    set_z(cmpData&0xFF);
    flag_c = (cmpData & 0xff00) == 0 ? 1 : 0;
}

function st_x(addr,reg){
    write(addr, reg);
    return 3;
}
function indirect(){
    let data = read_program(prgPc++);
    let r1 = read(data);
    let r2 = read(data+1);
    return concat16(r1,r2);
}

//栈操作----------------------------------
function push_stack(data){
    write(0x0100 | (c_reg_s&0xFF),data);
    c_reg_s-=1;
}

function push16_stack(data){
    push_stack((data>>8)&0xFF);
    push_stack(data&0xFF);
}

function pop8_stack(){
    c_reg_s++;
    return read(0x0100 | (c_reg_s & 0xFF));
}

function pop16_stack(){
    let pcLow8 = pop8_stack()&0xFF;
    let pcLow16 = pop8_stack()&0xFF;
    return  (pcLow16 << 8) | pcLow8;
}


//中断
function interrupt_nmi() {
    if(get_bit(p_reg_2000,7)){
        push16_stack(prgPc);
        push_stack(flag_merge());
        set_i(1);
        prgPc = (read(0xFFFA) & 0xff) | ((read(0xFFFB) & 0xff) << 8);
    }
}


function ppu_render(line){
    let render=[];
    //渲染背景
    if(get_bit(p_reg_2001,3)) {
        let ntAddr = get_bit(p_reg_2000,0);
        let bgAddr = get_bit(p_reg_2000,4);
        let nameTableAddr = 0;
        let patternAddr = 0;
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
    // //渲染精灵
    // if(get_bit(p_reg_2001,4)) {
    //     let spritePatternAddr = get_bit(p_reg_2000,3)==0 ? 0:0x1000;
    //     let spriteSize = get_bit(p_reg_2000,5);
    //     renderSprite(line,spritePatternAddr,spriteSize,render);
    // }

}



var debugLog;

function loadData(romDataBlob,logObj) {
	debugLog = logObj;
	var reader = new FileReader();
	reader.readAsArrayBuffer(romDataBlob);
	reader.onload = function() {
		//读取完毕后输出结果
		let romData = new Int8Array(this.result);
		let n = romData[0];
		let e = romData[1];
		let s = romData[2];
		console.log(n, e, s);
		rpg16KSize = romData[4];
		chr8KSize = romData[5];
		let control1 = romData[6];
		mirrorType = control1 & 0x1;
		let control2 = romData[7];
		mapper = ((control1 >> 4) & 0xf) | (((control2 >> 4) & 0xf) << 4)
		console.log('rpg16KSize', rpg16KSize);
		console.log('chr8KSize', chr8KSize);
		console.log('mirrorType', mirrorType);
		console.log('mapper', mapper);
		for (var i = 0; i < rpg16KSize * 16 * 1024; i++) {
			prgData[i] = romData[16 + i];
		}
		for (var i = 0; i < chr8KSize * 8 * 1024; i++) {
			chrData[i] = romData[16 + rpg16KSize * 16 * 1024 + i];
		}
		reset();
		start();
	}
}

function reset() {
	prgPc = concat16(read(0xFFFC), read(0xFFFD));
}

function start() {
	console.log('GO');
	let c=0;
	while (true) {
		for (var i = 0; i < 240; i++) {
			execInstrcution();
			ppu_render(i);
		}
		p_reg_2002 = p_reg_2002|0x80;
		//NMI中断
		interrupt_nmi();
		for (var i = 242; i < 262; i++) {
			if( i == 261 ) {
				//Sprite 0 Hit false 第7位
				p_reg_2002 = p_reg_2002&0xBF;
				//设置vblank false 第8位
				p_reg_2002 = p_reg_2002&0x7F;
			}
			execInstrcution();
		}
		c++;
		if(c==10){
			break;
		}
		
	}
}

function read_program(addr) {
	if (addr < 0x8000) {
		return c_mem[addr];
	}
	return prgData[addr - 0x8000];
}

String.format = function (string) {
    var args = Array.prototype.slice.call(arguments, 1, arguments.length);
    return string.replace(/{(\d+)}/g, function (match, number) {
        return typeof args[number] != "undefined" ? args[number] : match;
    });
};
var count = 0;

//内存操作----------------------------------
function read(addr){
    //printf(" | RD:[addr:%02X INDEX:%d]",addr,addr);
    let ret_p_reg_2002;
    let temp_p_reg_2006;
    switch (addr) {
        case 0x2000:
            return p_reg_2000;
        case 0x2001:
            return p_reg_2001;
        case 0x2003:
            return p_reg_2003;
        case 0x2005:
            return p_reg_2005;
        case 0x2006:
            return p_reg_2006;
        //读PPUSTATUS状态寄存器
        case 0x2002:
            ret_p_reg_2002 = p_reg_2002;
			p_reg_2002 &=0x7f;
            p_reg_2006_flag=0;
            return ret_p_reg_2002;
        case 0x2007:
            temp_p_reg_2006 = p_reg_2006;
            p_reg_2006 += get_bit(p_reg_2000,2)?32:1;
            if(addr <= 0x3EFF) {
                //读取PPU
                let res = p_reg_2007;
                p_reg_2007 = p_read(temp_p_reg_2006);
                return res;
            }else if(addr <= 0x3FFF) {
                //读取调色板
            }
            break;
        default:
            return read_program(addr);
    }
}

function write(addr,data){
    //printf(" | WR:[ADDR:%02X INDEX:%d DATA:%d]",addr&0xFFFF,addr&0xFFFF,data);
    let start;
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
                p_reg_2006 = (data&0x3F) << 8;
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
            for(var i=0; i < 256; i++) {
                let readData = read(start++);
                p_write_spr(i,readData);
            }
            break;
        default:
            c_mem[addr&0xFFFF] = data;
    }
}

function execInstrcution() {
	cpu_cycle = 113;
	do {
		var opc = read_program(prgPc++);
		
	  let s = String.format("PC:[{0}] | CYC:[{1}] | PC:[{2}] | OPC:[{3}] | R:[A:{4} X:{5} Y:{6} S:{7}] | F:[N:{8} V:{9} B:{10} D:{11} I:{12} Z:{13} C:{14}]",
			   ++count,
			   cpu_cycle,
			   ((prgPc-1)&0xFFFF).toString(16),
				(opc&0xFF).toString(16),
			   c_reg_a&0xFF,c_reg_x&0xFF,c_reg_y&0xFF,c_reg_s&0xFF,
			   flag_n,flag_v,flag_b,flag_d,flag_i,flag_z,flag_c);
		debugLog.push(s);
		
		switch (opc & 0xff) {
			case 0x78:
				set_i(1);
				cpu_cycle -= 2;
				break;
			case 0xD8:
				set_d(0);
				cpu_cycle -= 2;
				break;
			case 0xA9:
				c_reg_a = read_program(prgPc++);
				set_nz(c_reg_a);
				cpu_cycle -= 2;
				break;
			case 0x8D:
				abs_data = ams_abs();
				write(abs_data, c_reg_a);
				cpu_cycle -= 4;
				break;
			case 0xA2:
				c_reg_x = read_program(prgPc++);
				set_nz(c_reg_x);
				cpu_cycle -= 2;
				break;
			case 0x9A:
				c_reg_s = c_reg_x;
				cpu_cycle -= 2;
				break;
			case 0xAD:
				c_reg_a = read(ams_abs());
				set_nz(c_reg_a);
				cpu_cycle -= 4;
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
				c_reg_y = read_program(prgPc++);
				set_nz(c_reg_y);
				cpu_cycle -= 2;
				break;
			case 0xBD:
				abs_data = ams_abs_x('x',c_reg_x);
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
				if (flag_c) {
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
				if (flag_z == 0) {
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
				cpu_cycle -= st_x(read_program(prgPc++) & 0xff, c_reg_a);
				break;
			case 0x86:
				cpu_cycle -= st_x(read_program(prgPc++) & 0xff, c_reg_x);
				break;
			case 0xE0:
				data = c_reg_x - read_program(prgPc++);
				set_nz(data);
				flag_c = (data & 0xff00) == 0 ? 1 : 0;
				cpu_cycle -= 2;
				break;
			case 0x91:
				write(indirect() + (c_reg_y & 0xff), c_reg_a);
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
				let cmpData = (c_reg_y & 0xFF) - (data & 0xFF);
				set_nz(cmpData);
				flag_c = (cmpData & 0xff00) == 0?1:0;
				cpu_cycle -= 2;
				break;
				//RTS
			case 0x60:
				prgPc = pop16_stack();
				prgPc += 1;
				cpu_cycle -= 6;
				break;
				//BIT_ABS
			case 0x2C:
				data = read(ams_abs());
				set_n(data);
				set_v((data >> 6) & 1);
				set_z(c_reg_a & data);
				cpu_cycle -= 4;
				break;
				//STA_ABS_Y
			case 0x99:
				st_x(ams_abs_x('y',c_reg_y), c_reg_a);
				cpu_cycle -= 5;
				break;
				//INY
			case 0xC8:
				c_reg_y += 1;
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
				data = (read(abs_data) + 1) & 0xff;
				write(abs_data, data);
				set_nz(data);
				cpu_cycle -= 6;
				break;
				//执行到AC前会触发NMI
				//LDY_ABS
			case 0xAC:
				c_reg_y = read(ams_abs());
				set_nz(c_reg_y);
				cpu_cycle -= 4;
				break;
				//LDX_ABS
			case 0xAE:
				c_reg_x = read(ams_abs());
				set_nz(c_reg_x);
				cpu_cycle -= 4;
				break;
				//LDA_INDIRECT_Y
			case 0xB1:
				c_reg_a = indirect_y(read_program(prgPc++));
				set_nz(c_reg_a);
				cpu_cycle -= 5;
				break;
				//LDX_ABS_Y
			case 0xBE:
				c_reg_x = read(ams_abs_x('y',c_reg_y));
				set_nz(c_reg_x);
				cpu_cycle -= 4;
				break;
				//STA_ABS_X
			case 0x9D:
				abs_data = ams_abs_x('x',c_reg_x);
				write(abs_data, c_reg_a);
				cpu_cycle -= 5;
				break;
				//LSR
			case 0x4A:
				flag_c = c_reg_a & 1;
				c_reg_a = (c_reg_a & 0xff) >> 1;
				set_nz(c_reg_a);
				cpu_cycle -= 2;
				break;
				//TAX
			case 0xAA:
				c_reg_x = c_reg_a;
				set_nz(c_reg_x);
				cpu_cycle -= 2;
				break;
				//PHA
			case 0x48:
				push_stack(c_reg_a);
				cpu_cycle -= 3;
				break;
				//ORA_ZERO
			case 0x05:
				data = read_program(prgPc++);
				c_reg_a |= read(data & 0xFF);
				set_nz(c_reg_a);
				cpu_cycle -= 3;
				break;
				//PLA
			case 0x68:
				c_reg_a = pop8_stack();
				set_nz(c_reg_a);
				cpu_cycle -= 4;
				break;
				//ROL
			case 0x2A:
				data = c_reg_a;
				c_reg_a = (data << 1) | flag_c;
				flag_c = (data >> 7) & 1;
				set_nz(c_reg_a);
				cpu_cycle -= 2;
				break;
				//AND_ABS_X
			case 0x3D:
				abs_data = ams_abs_x('x',c_reg_x);
				c_reg_a &= read(abs_data);
				set_nz(c_reg_a);
				cpu_cycle -= 4;
				break;
				//BEQ
			case 0xF0:
				data = read_program(prgPc++);
				if (flag_z) {
					cpu_cycle -= 2 + (prgPc & 0xff00) == ((prgPc + data) & 0xff00) ? 1 : 2;
					prgPc += data;
					break;
				}
				cpu_cycle -= 2;
				break;
				//INX
			case 0xE8:
				c_reg_x += 1;
				set_nz(c_reg_x);
				cpu_cycle -= 2;
				break;
				//SEC
			case 0x38:
				flag_c = 1;
				cpu_cycle -= 2;
				break;
				//SBC_ABS_Y
			case 0xF9:
				ams_sbc(read(ams_abs_x('y',c_reg_y)));
				cpu_cycle -= 4;
				break;
				//BCC
			case 0x90:
				data = read_program(prgPc++);
				if (flag_c == 0) {
					cpu_cycle -= 2 + (prgPc & 0xff00) == ((prgPc + data) & 0xff00) ? 1 : 2;
					prgPc += data;
					break;
				}
				cpu_cycle -= 2;
				break;
				//DEC_ABS
			case 0xCE:
				abs_data = ams_abs();
				data = read(abs_data) - 1;
				write(abs_data, data);
				set_nz(data);
				cpu_cycle -= 6;
				break;
				//INC_ZERO
			case 0xE6:
				data = read_program(prgPc++);
				data2 = read(data & 0xFF) + 1;
				write(data & 0xFF, data2);
				set_nz(data2);
				cpu_cycle -= 5;
				break;
				//EOR_ZERO
			case 0x45:
				data = read(read_program(prgPc++) & 0xFF);
				c_reg_a ^= data;
				set_nz(c_reg_a);
				cpu_cycle -= 3;
				break;
				//CLC
			case 0x18:
				flag_c = 0;
				cpu_cycle -= 2;
				break;
				//ROR_ABS_X
			case 0x7E:
				abs_data = ams_abs_x('x',c_reg_x);
				data = read(abs_data);
				data2 = ((data & 0xff) >> 1) | (flag_c << 7);
				flag_c = data & 1;
				set_nz(data2);
				write(abs_data, data2);
				cpu_cycle -= 7;
				break;
				//ASL_A
			case 0x0A:
				flag_c = (c_reg_a >> 7) & 1;
				c_reg_a <<= 1;
				set_nz(c_reg_a);
				cpu_cycle -= 2;
				break;
				//TAY
			case 0xA8:
				c_reg_y = c_reg_a;
				set_nz(c_reg_y);
				cpu_cycle -= 2;
				break;
				//JMP_INDIRECT
			case 0x6C:
				abs_data = ams_abs();
				let low = read(abs_data);
				prgPc = concat16(low, read(abs_data + 1));
				cpu_cycle -= 5;
				break;
				//LDA_ABS_Y
			case 0xB9:
				abs_data = ams_abs_x('y',c_reg_y);
				c_reg_a = read(abs_data);
				set_nz(c_reg_a);
				cpu_cycle -= 4;
				break;
				//ADC_ABS
			case 0x6D:
				data = read(ams_abs());
				ams_adc(data);
				cpu_cycle -= 4;
				break;
				//ADC
			case 0x69:
				data = read_program(prgPc++);
				ams_adc(data);
				cpu_cycle -= 2;
				break;
				//STY_ABS
			case 0x8C:
				write(ams_abs(), c_reg_y);
				cpu_cycle -= 4;
				break;
				//LDA_ZERO
			case 0xA5:
				data = read_program(prgPc++);
				c_reg_a = read(data & 0xFF);
				set_nz(c_reg_a);
				cpu_cycle -= 3;
				break;
				//RTI
			case 0x40:
				flag_set(pop8_stack());
				prgPc = pop16_stack();
				cpu_cycle -= 6;
				break;
				//DEC_ZERO
			case 0xC6:
				data = read_program(prgPc++) & 0xFF;
				data2 = read(data) - 1;
				write(data, data2);
				set_nz(data2);
				cpu_cycle -= 5;
				break;
				//TYA
			case 0x98:
				c_reg_a = c_reg_y;
				set_nz(c_reg_a);
				cpu_cycle -= 2;
				break;
				//ADC_ZERO
			case 0x65:
				data = read(read_program(prgPc++) & 0xFF);
				ams_adc(data);
				cpu_cycle -= 3;
				break;
				//LDX_ZERO
			case 0xA6:
				c_reg_x = read(read_program(prgPc++) & 0xFF);
				set_nz(c_reg_x);
				cpu_cycle -= 3;
				break;
				//STX_ABS
			case 0x8E:
				abs_data = ams_abs();
				write(abs_data, c_reg_x);
				cpu_cycle -= 4;
				break;
				//BMI
			case 0x30:
				data = read_program(prgPc++);
				if (flag_n == 1) {
					cpu_cycle -= 2 + (prgPc & 0xff00) == ((prgPc + data) & 0xff00) ? 1 : 2;
					prgPc += data;
					break;
				}
				cpu_cycle -= 2;
				break;
				//ADC_ABS_Y
			case 0x79:
				abs_data = ams_abs_x('y',c_reg_y);
				ams_adc(read(abs_data));
				cpu_cycle -= 4;
				break;
				//SBC
			case 0xE9:
				data = read_program(prgPc++);
				ams_sbc(data);
				cpu_cycle -= 2;
				break;
				//STY_ZERO
			case 0x84:
				write(read_program(prgPc++) & 0xFF, c_reg_y);
				cpu_cycle -= 3;
				break;
				//BIT_ZERO
			case 0x24:
				data = read(read_program(prgPc++) & 0xFF);
				set_n(data);
				set_v((data >> 6) & 1);
				set_z(c_reg_a & data);
				cpu_cycle -= 3;
				break;
				//LDY_ZERO
			case 0xA4:
				data = read(read_program(prgPc++) & 0xFF);
				c_reg_y = data;
				set_nz(c_reg_y);
				cpu_cycle -= 3;
				break;
				//CMP_ABS
			case 0xCD:
				ams_cmp(read(ams_abs()));
				cpu_cycle -= 4;
				break;
				//CMP_ABS_Y
			case 0xD9:
				ams_cmp(read(ams_abs_x('y',c_reg_y)));
				cpu_cycle -= 4;
				break;
				//EOR
			case 0x49:
				c_reg_a ^= read_program(prgPc++);
				set_nz(c_reg_a);
				cpu_cycle -= 2;
				break;
				//ROL_ZERO
			case 0x26:
				data = read(read_program(prgPc++) & 0xFF);
				data2 = (data << 1) | flag_c;
				flag_c = (data >> 7) & 1;
				set_nz(data2);
				write(read_program(prgPc - 1) & 0xFF, data2);
				cpu_cycle -= 5;
				break;
				//LSR_ZERO
			case 0x46:
				data = read_program(prgPc++) & 0xFF;
				data3 = read(data & 0xff);
				data2 = (data3 & 0xff) >> 1;
				flag_c = data3 & 1;
				set_nz(data2);
				write(data, data2);
				cpu_cycle -= 5;
				break;
				//LDY_ABS_X
			case 0xBC:
				data = read(ams_abs_x('x',c_reg_x));
				c_reg_y = data;
				set_nz(c_reg_y);
				cpu_cycle -= 4;
				break;
				//DEC_ABS_X
			case 0xDE:
				abs_data = ams_abs_x('x',c_reg_x);
				data = read(abs_data) - 1;
				write(abs_data, data);
				set_nz(data);
				cpu_cycle -= 7;
				break;
				//LSR_ABS
			case 0x4E:
				abs_data = ams_abs();
				data = read(abs_data);
				flag_c = data & 1;
				data2 = (data & 0xff) >> 1;
				set_nz(data2);
				write(abs_data, data2);
				cpu_cycle -= 6;
				break;
				//ROR_A
			case 0x6A:
				data = ((c_reg_a & 0xff) >> 1) | (flag_c << 7);
				flag_c = c_reg_a & 1;
				set_nz(data);
				c_reg_a = data;
				cpu_cycle -= 2;
				break;
				//ROL_ABS
			case 0x2E:
				abs_data = ams_abs();
				data = read(abs_data);
				data2 = (data << 1) | flag_c;
				flag_c = (data >> 7) & 1;
				set_nz(data2);
				write(abs_data, data2);
				cpu_cycle -= 6;
				break;
				//CMP_ZERO
			case 0xC5:
				data = read(read_program(prgPc++) & 0xFF);
				ams_cmp(data);
				cpu_cycle -= 3;
				break;
				//ORA_ABS
			case 0x0D:
				c_reg_a |= read(ams_abs());
				set_nz(c_reg_a);
				cpu_cycle -= 2;
				break;
				//NOP
			case 0xEA:
				cpu_cycle -= 2;
				break;
				//LDA_ZERO_X
			case 0xB5:
				data = read(ams_zero(read_program(prgPc++), c_reg_x));
				c_reg_a = data;
				set_nz(c_reg_a);
				cpu_cycle -= 4;
				break;
				//STA_ZERO_X
			case 0x95:
				write(ams_zero(read_program(prgPc++), c_reg_x), c_reg_a);
				cpu_cycle -= 4;
				break;
				//SBC_ABS
			case 0xED:
				ams_sbc(read(ams_abs()));
				cpu_cycle -= 4;
				break;
				//CMP_ZERO_X
			case 0xD5:
				ams_cmp(read(ams_zero(read_program(prgPc++), c_reg_x)));
				cpu_cycle -= 4;
				break;
				//SBC_ZERO_X
			case 0xF5:
				ams_sbc(read(ams_zero(read_program(prgPc++), c_reg_x)));
				cpu_cycle -= 4;
				break;
				//AND_ABS_Y
			case 0x39:
				c_reg_a &= read(ams_abs_x('y',c_reg_y));
				set_nz(c_reg_a);
				cpu_cycle -= 4;
				break;
				//ASL_ABS
			case 0x0E:
				abs_data = ams_abs();
				data = read(abs_data);
				flag_c = (data >> 7) & 1;
				data2 = data << 1;
				set_nz(data2);
				write(abs_data, data2);
				cpu_cycle -= 6;
				break;
				//AND_ABS
			case 0x2D:
				abs_data = ams_abs();
				c_reg_a &= read(abs_data);
				set_nz(c_reg_a);
				cpu_cycle -= 4;
				break;
				//ADC_ABS_X
			case 0x7D:
				abs_data = ams_abs_x('x',c_reg_x);
				ams_adc(read(abs_data));
				cpu_cycle -= 4;
				break;
				//CMP_ABS_X
			case 0xDD:
				abs_data = ams_abs_x('x',c_reg_x);
				ams_cmp(read(abs_data));
				cpu_cycle -= 2;
				break;
				//ADC_ZERO_X
			case 0x75:
				abs_data = ams_zero(read_program(prgPc++), c_reg_x);
				ams_adc(read(abs_data));
				cpu_cycle -= 2;
				break;
				//SBC_ZERO
			case 0xE5:
				data = read(read_program(prgPc++) & 0xff);
				ams_sbc(data);
				cpu_cycle -= 3;
				break;
				//AND_ZERO
			case 0x25:
				data = read(read_program(prgPc++) & 0xff);
				ams_and(data);
				cpu_cycle -= 3;
				break;
			default:
				cpu_cycle -= 0;
				console.log("unknown ins",(opc & 0xFF).toString(16));
				break;
		}
	} while (cpu_cycle > 0);
}
