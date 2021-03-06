var debug = false;
self.addEventListener('message', function (e) {
	let data = e.data;
	if(data.type==1) {
		loadData(data.data);
	}else{
		debug = data.data;
	}
}, false);


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

var palettes = [
         [ 0x75, 0x75, 0x75 ],[ 0x27, 0x1B, 0x8F ],[ 0x00, 0x00, 0xAB ],[ 0x47, 0x00, 0x9F ],[ 0x8F, 0x00, 0x77 ],[ 0xAB, 0x00, 0x13 ],[ 0xA7, 0x00, 0x00 ],[ 0x7F, 0x0B, 0x00 ],
         [ 0x43, 0x2F, 0x00 ],[ 0x00, 0x47, 0x00 ],[ 0x00, 0x51, 0x00 ],[ 0x00, 0x3F, 0x17 ],[ 0x1B, 0x3F, 0x5F ],[ 0x00, 0x00, 0x00 ],[ 0x05, 0x05, 0x05 ],[ 0x05, 0x05, 0x05 ],
         [ 0xBC, 0xBC, 0xBC ],[ 0x00, 0x73, 0xEF ],[ 0x23, 0x3B, 0xEF ],[ 0x83, 0x00, 0xF3 ],[ 0xBF, 0x00, 0xBF ],[ 0xE7, 0x00, 0x5B ],[ 0xDB, 0x2B, 0x00 ],[ 0xCB, 0x4F, 0x0F ],
         [ 0x8B, 0x73, 0x00 ],[ 0x00, 0x97, 0x00 ],[ 0x00, 0xAB, 0x00 ],[ 0x00, 0x93, 0x3B ],[ 0x00, 0x83, 0x8B ],[ 0x11, 0x11, 0x11 ],[ 0x09, 0x09, 0x09 ],[ 0x09, 0x09, 0x09 ],
         [ 0xFF, 0xFF, 0xFF ],[ 0x3F, 0xBF, 0xFF ],[ 0x5F, 0x97, 0xFF ],[ 0xA7, 0x8B, 0xFD ],[ 0xF7, 0x7B, 0xFF ],[ 0xFF, 0x77, 0xB7 ],[ 0xFF, 0x77, 0x63 ],[ 0xFF, 0x9B, 0x3B ],
         [ 0xF3, 0xBF, 0x3F ],[ 0x83, 0xD3, 0x13 ],[ 0x4F, 0xDF, 0x4B ],[ 0x58, 0xF8, 0x98 ],[ 0x00, 0xEB, 0xDB ],[ 0x66, 0x66, 0x66 ],[ 0x0D, 0x0D, 0x0D ],[ 0x0D, 0x0D, 0x0D ],
         [ 0xFF, 0xFF, 0xFF ],[ 0xAB, 0xE7, 0xFF ],[ 0xC7, 0xD7, 0xFF ],[ 0xD7, 0xCB, 0xFF ],[ 0xFF, 0xC7, 0xFF ],[ 0xFF, 0xC7, 0xDB ],[ 0xFF, 0xBF, 0xB3 ],[ 0xFF, 0xDB, 0xAB ],
         [ 0xFF, 0xE7, 0xA3 ],[ 0xE3, 0xFF, 0xA3 ],[ 0xAB, 0xF3, 0xBF ],[ 0xB3, 0xFF, 0xCF ],[ 0x9F, 0xFF, 0xF3 ],[ 0xDD, 0xDD, 0xDD ],[ 0x11, 0x11, 0x11 ],[ 0x11, 0x11, 0x11 ]
        ,[ 0x00, 0x00, 0x00 ]
];

//状态寄存器
var flag_n = 0,
	flag_v = 0,
	flag_b = 1,
	flag_d = 0,
	flag_i = 1,
	flag_z = 0,
	flag_c = 0;
//CPU寄存器
var c_reg_a = 0,
	c_reg_x = 0,
	c_reg_y = 0,
	c_reg_s = 0xFF;
//PPU寄存器
var p_reg_2000 = 0,
	p_reg_2001 = 0,
	p_reg_2002 = 0,
	p_reg_2003 = 0,
	p_reg_2005 = 0,
	p_reg_2007 = 0,
	p_reg_2006_flag = 0,
	p_reg_2007 = 0;

function get_bit(data, index) {
	return (data >> index) & 1;
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
	flag_z = flag == 0 ? 1 : 0;
}

function set_nz(flag) {
	set_n(flag&0xff);
	set_z(flag&0xff);
}

function set_v(flag) {
	flag_v = flag;
}
//标志位操作结束-------------------------------


//指令操作开始---------------------------
function concat16(low, high) {
	return (low & 0xFF) | ((high & 0xff) << 8);
}

function indirect_y(data){
    let low = read(data & 0xFF);
    let high = read((data & 0xFF) + 1);
    let addr = concat16(low,high)+ (c_reg_y & 0xFF);
    return read(addr);
}

function ams_abs() {
	let low = read_program(prgPc++);
	let high = read_program(prgPc++);
	return concat16(low, high);
}

function ams_abs_x(reg) {
	switch (reg) {
		case 'x':
			reg = c_reg_x;
			break;
		case 'y':
			reg = c_reg_y;
			break;
	}
	return ams_abs() + (reg & 0xff);
}

function ams_cmp(data) {
	let cmpData = (c_reg_a & 0xff) - (data & 0xff);
	flag_n = (cmpData >> 7) & 1;
	set_z(cmpData & 0xFF);
	flag_c = (cmpData & 0xff00) == 0 ? 1 : 0;
}

function st_x(addr, reg) {
	write(addr, reg);
	return 3;
}

function indirect() {
	let data = read_program(prgPc++);
	let r1 = read(data);
	let r2 = read(data + 1);
	return concat16(r1, r2);
}

function ams_sbc(data){
    let abs_data = (c_reg_a & 0xff) - (data&0xff) - (flag_c == 0 );
    flag_c = (abs_data & 0xff00) == 0?1:0;
    set_nz(abs_data&0xff);
    set_v((((c_reg_a ^ data) & 0x80) != 0)&& ((c_reg_a ^ abs_data) & 0x80) != 0?1:0);
    set_reg_a(abs_data&0xff);
}
function ams_adc(data){
    let abs_data = (c_reg_a & 0xff) + (data & 0xff) + (flag_c & 0xff);
    flag_c = abs_data >> 8;
    set_nz(abs_data & 0xff);
    flag_v = ((c_reg_a ^ data) & 0x80) == 0 && ((c_reg_a ^ abs_data) & 0x80) != 0?1:0;
	set_reg_a(abs_data&0xff);
}
//指令操作结束---------------------------

function to8Signed(data){
	if(data>127){
		return (data&0x7f)-128;
	}else if(data<-128){
		return data&0x7f;
	}
	return data;
}

function set_reg_a(data){
	c_reg_a = to8Signed(data&0xff);
}
function set_reg_x(data){
	c_reg_x = to8Signed(data&0xff);
}
function set_reg_y(data){
	c_reg_y = to8Signed(data&0xff);
}

//栈操作----------------------------------
function push_stack(data) {
	write(0x0100 | (c_reg_s & 0xFF), to8Signed(data));
	c_reg_s -= 1;
}

function push16_stack(data) {
	push_stack((data >> 8) & 0xFF);
	push_stack(data & 0xFF);
}

function pop8_stack() {
	c_reg_s++;
	return read(0x0100 | (c_reg_s & 0xFF));
}

function pop16_stack() {
	let pcLow8 = pop8_stack()&0xff;
	let pcLow16 = pop8_stack()&0xff;
	return (pcLow16 << 8) | pcLow8;
}

function flag_merge() {
    return (flag_n << 7) | (flag_v << 6) | 0x20 | (flag_b << 4)| (flag_d << 3) | (flag_i << 2) | (flag_z << 1) | flag_c;
}
function flag_set(data) {
    flag_n = (data >> 7) & 0x01;
    flag_v = (data >> 6) & 0x01;
    flag_b = (data >> 4) & 0x01;
    flag_d = (data >> 3) & 0x01;
    flag_i = (data >> 2) & 0x01;
    flag_z = (data >> 1) & 0x01;
    flag_c = data & 0x01;
}

//中断
function interrupt_nmi() {
	if (get_bit(p_reg_2000, 7)) {
		push16_stack(prgPc);
		push_stack(flag_merge());
		set_i(1);
		prgPc = (read(0xFFFA) & 0xff) | ((read(0xFFFB) & 0xff) << 8);
	}
}


function ppu_render(line) {
	let render = [[]];
	//渲染背景
	if (get_bit(p_reg_2001, 3)) {
		let ntAddr = get_bit(p_reg_2000, 0);
		let bgAddr = get_bit(p_reg_2000, 4);
		let nameTableAddr = 0;
		let patternAddr = 0;
		if (ntAddr == 0x00) {
			nameTableAddr = 0x2000;
		} else if (ntAddr == 0x01) {
			nameTableAddr = 0x2400;
		} else if (ntAddr == 0x10) {
			nameTableAddr = 0x2800;
		} else if (ntAddr == 0x11) {
			nameTableAddr = 0x2C00;
		}
		if (bgAddr == 1) {
			patternAddr = 0x1000;
		}
		render_name_table(line, nameTableAddr, patternAddr, render);
	}
	return render;
	// //渲染精灵
	// if(get_bit(p_reg_2001,4)) {
	//     let spritePatternAddr = get_bit(p_reg_2000,3)==0 ? 0:0x1000;
	//     let spriteSize = get_bit(p_reg_2000,5);
	//     renderSprite(line,spritePatternAddr,spriteSize,render);
	// }

}

function loadData(romDataBlob) {
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
	while (true) {
		var renderBuff=[[]];
		for (var i = 0; i < 240; i++) {
			execInstrcution();
			let render = ppu_render(i);
			for(let r = 0; r < 256; r++) {
				renderBuff[i * 256 + r] = render[r];
			}
		}
		p_reg_2002 = p_reg_2002 | 0x80;
		//Sprite 0 Hit false 第7位
		p_reg_2002 = p_reg_2002 & 0xBF;
		execInstrcution();
		//NMI中断
		interrupt_nmi();
		for (var i = 242; i < 262; i++) {
			execInstrcution();
		}
		//设置vblank false 第8位
		p_reg_2002 = p_reg_2002 & 0x7F;
		
		if (get_bit(p_reg_2001, 3)&&renderBuff && !debug) {
			self.postMessage(renderBuff);
		}
		
	}
}

function read_program(addr) {
	if (addr < 0x8000) {
		let data = c_mem[addr];
		if(data == undefined){
			data = 0;
		}
		return data;
	}
	return prgData[addr - 0x8000];
}

String.format = function(string) {
	var args = Array.prototype.slice.call(arguments, 1, arguments.length);
	return string.replace(/{(\d+)}/g, function(match, number) {
		return typeof args[number] != "undefined" ? args[number] : match;
	});
};
var count = 0;

//内存操作----------------------------------
function read(addr) {
	if(addr<0x8000 && debug){
		self.postMessage(String.format(" | RD:[addr:{0} INDEX:{1}]",addr,addr));
	}
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
			p_reg_2002 &= 0x7f;
			p_reg_2006_flag = 0;
			return ret_p_reg_2002;
		case 0x2007:
			temp_p_reg_2006 = p_reg_2006&0x3fff;
			p_reg_2006 += get_bit(p_reg_2000, 2) ? 32 : 1;
			if (addr <= 0x3EFF) {
				//读取PPU
				let res = p_reg_2007;
				p_reg_2007 = p_read(temp_p_reg_2006);
				return res;
			}
			break;
		default:
			return read_program(addr);
	}
}
function write(addr, data) {
	data = to8Signed(data&0xFF);
	if(debug){
		self.postMessage(String.format(" | WR:[ADDR:{0} INDEX:{1} DATA:{2}]",addr&0xFFFF,addr&0xFFFF,data));
	}
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
			p_reg_2003 += 1;
			p_write_spr(p_reg_2003, data);
			break;
		case 0x2005:
			p_reg_2005 = data;
			break;
		case 0x2006:
			if (!p_reg_2006_flag) {
				//第一次写将写入高6位;
				p_reg_2006 = (data & 0x3F) << 8;
			} else {
				//第二次写将写入低8位
				p_reg_2006 |= (data & 0xFF);
			}
			p_reg_2006_flag = !p_reg_2006_flag;
			break;
		case 0x2007:
			p_write(p_reg_2006, data);
			p_reg_2006 += get_bit(p_reg_2000, 2) ? 32 : 1;
			break;
			//OAM DMA register (high let)
		case 0x4014:
			start = data<<8;
			for (var i = 0; i < 256; i++) {
				let readData = read(start++);
				p_write_spr(i, readData);
			}
			break;
		default:
			c_mem[addr & 0xFFFF] = data;
	}
}

function p_write(addr,data){
    //写入 $3F00-3FFF 的 D7-D6 字节被忽略.
    if(addr >= 0x3F00 && addr <= 0x3FFF) {
        data = data & 0x3f;
        if(addr==0x3F00){
            p_mem[0x3F10]=data;
        }else if(addr==0x3F10){
            p_mem[0x3F00]=data;
        }
    }
	if(debug){
		self.postMessage(String.format(" | PWR:[addr:{0} INDEX:{1} DATA:{2}]",addr&0xFFFF,addr&0xFFFF,data));
	}
    //printf(" | PWR:[addr:%02X INDEX:%d DATA:%d]",addr&0xFFFF,addr&0xFFFF,data);
    p_mem[addr] = data;
}

function p_read(addr){
	if(addr<0x2000){
		return chrData[addr];
	}
    return p_mem[addr];
}

function p_write_spr(addr,data){
    p_spr_ram[addr&0xFF] = data;
}

function getPatternColorHighData(attributeData,i,line) {
    let x = i % 4;
    let y = line % 32 / 8;
    let high2;
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

function render_name_table(scanLineIndex,nametableStartAddr,patternStartAddr,render){
    //32*30个Tile = (256*240 像素)
    for (let i=0;i<32;i++) {
        //1 读取name table数据,其实就是Tile图案表索引  (图案+颜色 = 8字节+8字节=16字节)
        let nameTableData = (p_read((nametableStartAddr + (scanLineIndex/8) * 32) + i)&0xFF) * 16;
        //2 读取图案,图案表起始地址+索引+具体渲染的8字节中的第几字节
        let patternAddr = patternStartAddr + nameTableData + (scanLineIndex % 8);
        let patternColor = patternAddr + 8;
        //图案表数据
        let patternData = p_read(patternAddr);
        //图案表颜色数据
        let colorData = p_read(patternColor);
        //取每像素的低两位颜色

        let patternColorLowData = [];
        for(let i=7;i>=0;i--) {
            patternColorLowData[i] = (((get_bit(colorData,i)<<1)&3) | (get_bit(patternData,i)&1));
        }
        //取颜色高两位,属性表数据64byte,每32*32像素一个字节,每32条扫描线占用8字节
        let attributeData = p_read(nametableStartAddr + 0x3C0 + (scanLineIndex/32*8)+i/4);
        let patternColorHighData = getPatternColorHighData(attributeData,i,scanLineIndex);
        let p0 = p_read(0x3F00);
        //合并 取最终4位颜色
        for (let i1 = 0; i1 <8; i1++) {
            let patternColorLowBit = patternColorLowData[7 - i1];
            //透明色 显示背景色
            if(patternColorLowBit == 0) {
                render[i*8+i1] = palettes[p0];
            }else {
                let colorAddr = 0x3f00 + (((patternColorHighData << 2) & 0xF) | (patternColorLowBit & 0x3));
                let paletteIndex = p_read(colorAddr);
                render[i*8+i1] = palettes[paletteIndex];
            }
        }
//        print8(patternData);
    }
//    printf("\n");
}



function execInstrcution() {
	cpu_cycle = 113;
	do {
		var opc = read_program(prgPc++);
		if(debug){
			self.postMessage("enter");
			let s = String.format(
				"PC:[{0}] | CYC:[{1}] | PC:[{2}] | OPC:[{3}] | R:[A:{4} X:{5} Y:{6} S:{7}] | F:[N:{8} V:{9} B:{10} D:{11} I:{12} Z:{13} C:{14}]",
				++count,
				cpu_cycle,
				((prgPc - 1) & 0xFFFF).toString(16).toUpperCase(),
				(opc & 0xFF).toString(16).toUpperCase(),
				(c_reg_a & 0xFF).toString(16).toUpperCase(), (c_reg_x & 0xFF).toString(16).toUpperCase(), (c_reg_y & 0xFF).toString(16).toUpperCase(), (c_reg_s & 0xFF).toString(16).toUpperCase(),
				flag_n, flag_v, flag_b, flag_d, flag_i, flag_z, flag_c);
			self.postMessage(s);
		}
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
				set_reg_a(read_program(prgPc++));
				set_nz(c_reg_a);
				cpu_cycle -= 2;
				break;
			case 0x8D:
				abs_data = ams_abs();
				write(abs_data, c_reg_a);
				cpu_cycle -= 4;
				break;
			case 0xA2:
				set_reg_x(read_program(prgPc++))
				set_nz(c_reg_x);
				cpu_cycle -= 2;
				break;
			case 0x9A:
				c_reg_s = c_reg_x;
				cpu_cycle -= 2;
				break;
			case 0xAD:
				set_reg_a(read(ams_abs()));
				set_nz(c_reg_a);
				cpu_cycle -= 4;
				break;
			case 0x10:
				data = read_program(prgPc++);
				if (flag_n == 0) {
					cpu_cycle -= 2 + ((prgPc & 0xff00) == ((prgPc + data) & 0xff00) ? 1 : 2);
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
				let addr = ams_abs_x('x', c_reg_x);
				data = read(addr);
				if(data==undefined){
					debugger
				}
				set_reg_a(data);
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
					cpu_cycle -= 2 + ((prgPc & 0xff00) == ((prgPc + data) & 0xff00) ? 1 : 2);
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
					cpu_cycle -= 2 + ((prgPc & 0xff00) == ((prgPc + data) & 0xff00) ? 1 : 2);
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
				c_reg_y = (c_reg_y-1)&0xff;
				set_nz(c_reg_y);
				cpu_cycle -= 2;
				break;
				//CPY
			case 0xC0:
				data = read_program(prgPc++);
				let cmpData = (c_reg_y & 0xFF) - (data & 0xFF);
				set_nz(cmpData);
				flag_c = (cmpData & 0xff00) == 0 ? 1 : 0;
				cpu_cycle -= 2;
				break;
				//RTS
			case 0x60:
				prgPc = pop16_stack();
				prgPc += 1;
				cpu_cycle -= 6;
				break;
				//BRK
			case 0x00:
				flag_b=1;
				flag_i=1;
				push16_stack(prgPc + 2)
				push_stack(flag_merge())
				prgPc = concat16(read(0xFFFE),read(0xFFFF));
				cpu_cycle-= 7;
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
				st_x(ams_abs_x('y', c_reg_y), c_reg_a);
				cpu_cycle -= 5;
				break;
				//INY
			case 0xC8:
				c_reg_y = (c_reg_y+1)&0xFF;
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
				set_reg_x(read(ams_abs()));
				set_nz(c_reg_x);
				cpu_cycle -= 4;
				break;
				//LDA_INDIRECT_Y
			case 0xB1:
				set_reg_a(indirect_y(read_program(prgPc++)));
				set_nz(c_reg_a);
				cpu_cycle -= 5;
				break;
				//LDX_ABS_Y
			case 0xBE:
				set_reg_x(read(ams_abs_x('y', c_reg_y)));
				set_nz(c_reg_x);
				cpu_cycle -= 4;
				break;
				//STA_ABS_X
			case 0x9D:
				abs_data = ams_abs_x('x', c_reg_x);
				write(abs_data, c_reg_a);
				cpu_cycle -= 5;
				break;
				//LSR
			case 0x4A:
				flag_c = c_reg_a & 1;
				set_reg_a((c_reg_a & 0xff) >> 1);
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
				set_reg_a((data << 1) | flag_c);
				flag_c = (data >> 7) & 1;
				set_nz(c_reg_a);
				cpu_cycle -= 2;
				break;
				//AND_ABS_X
			case 0x3D:
				abs_data = ams_abs_x('x', c_reg_x);
				c_reg_a &= read(abs_data);
				set_nz(c_reg_a);
				cpu_cycle -= 4;
				break;
				//BEQ
			case 0xF0:
				data = read_program(prgPc++);
				if (flag_z) {
					cpu_cycle -= 2 + ((prgPc & 0xff00) == ((prgPc + data) & 0xff00) ? 1 : 2);
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
				ams_sbc(read(ams_abs_x('y', c_reg_y)));
				cpu_cycle -= 4;
				break;
				//BCC
			case 0x90:
				data = read_program(prgPc++);
				if (flag_c == 0) {
					cpu_cycle -= 2 + ((prgPc & 0xff00) == ((prgPc + data) & 0xff00) ? 1 : 2);
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
				abs_data = ams_abs_x('x', c_reg_x);
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
				abs_data = ams_abs_x('y', c_reg_y);
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
				set_reg_x(read(read_program(prgPc++) & 0xFF));
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
					cpu_cycle -= 2 + ((prgPc & 0xff00) == ((prgPc + data) & 0xff00) ? 1 : 2);
					prgPc += data;
					break;
				}
				cpu_cycle -= 2;
				break;
				//ADC_ABS_Y
			case 0x79:
				abs_data = ams_abs_x('y', c_reg_y);
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
				set_reg_y(data);
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
				ams_cmp(read(ams_abs_x('y', c_reg_y)));
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
				data = read(ams_abs_x('x', c_reg_x));
				set_reg_y(data);
				set_nz(c_reg_y);
				cpu_cycle -= 4;
				break;
				//DEC_ABS_X
			case 0xDE:
				abs_data = ams_abs_x('x', c_reg_x);
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
				set_reg_a(data);
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
				c_reg_a &= read(ams_abs_x('y', c_reg_y));
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
				abs_data = ams_abs_x('x', c_reg_x);
				ams_adc(read(abs_data));
				cpu_cycle -= 4;
				break;
				//CMP_ABS_X
			case 0xDD:
				abs_data = ams_abs_x('x', c_reg_x);
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
				console.log("unknown ins"+(opc & 0xFF).toString(16));
				self.postMessage("enter")
				self.postMessage("unknown ins"+(opc & 0xFF).toString(16))
				break;
		}
	} while (cpu_cycle > 0);
}
