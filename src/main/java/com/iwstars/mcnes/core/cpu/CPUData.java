package com.iwstars.mcnes.core.cpu;

/**
 * CPU数据
 *
 * Address range	Size	Device
 * $0000-$07FF	   $0800	2KB internal RAM
 * $0800-$0FFF	   $0800	Mirrors of $0000-$07FF
 * $1000-$17FF	   $0800    Mirrors of $0000-$07FF
 * $1800-$1FFF	   $0800    Mirrors of $0000-$07FF
 * $2000-$2007	   $0008	NES PPU registers PPU寄存器
 * $2008-$3FFF	   $1FF8	Mirrors of $2000-2007 (repeats every 8 bytes) 镜像 每8个字节
 * $4000-$4017	   $0018	NES APU and I/O registers
 * $4018-$401F	   $0008	APU and I/O functionality that is normally disabled. See CPU Test Mode.
 * $4020-$FFFF	   $BFE0	Cartridge space: PRG ROM, PRG RAM, and mapper registers
 *
 * @author WStars
 * @date 2020/4/18 15:25
 */
public class CPUData {
}
