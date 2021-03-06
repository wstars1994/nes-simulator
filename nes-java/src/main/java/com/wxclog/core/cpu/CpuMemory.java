package com.wxclog.core.cpu;

import com.wxclog.core.Const;
import com.wxclog.core.DataBus;
import com.wxclog.util.LogUtil;
import com.wxclog.util.MemUtil;

import java.util.Iterator;

/**
 * CPU数据
 * +---------+-------+-------+-----------------------+
 * | 地址    | 大小  | 标记  |         描述          |
 * +---------+-------+-------+-----------------------+
 * | $0000   | $800  |       | RAM                   |
 * | $0800   | $800  | M     | RAM                   |
 * | $1000   | $800  | M     | RAM                   |
 * | $1800   | $800  | M     | RAM                   |
 * | $2000   | 8     |       | Registers             |
 * | $2008   | $1FF8 |  R    | Registers             |
 * | $4000   | $20   |       | Registers             |
 * | $4020   | $1FDF |       | Expansion ROM         |
 * | $6000   | $2000 |       | SRAM                  |
 * | $8000   | $4000 |       | PRG-ROM               |
 * | $C000   | $4000 |       | PRG-ROM               |
 * +---------+-------+-------+-----------------------+
 * @author WStars
 * @date 2020/4/18 15:25
 */
public class CpuMemory {

    /**
     * PRG程序计数器指针
     * 2字节指针
     */
    private int prgPc = 0x8000;
    /**
     * cpu内存
     */
    private byte[] data;

    public int getPrgPc() {
        return prgPc;
    }

    public void setPrgPc(int prgPc) {
        this.prgPc = prgPc;
    }

    /**
     * PRG数据迭代器
     * @return
     */
    public Iterator<Byte> iteratorPrgData() {
        return new Iterator<Byte>() {

            @Override
            public boolean hasNext() {
                return prgPc < data.length;
            }

            @Override
            public Byte next() {
                return Const.mapper.read(prgPc++);
            }

            @Override
            public void remove() {}
        };
    }

    /**
     * 写数据
     * @param addr
     * @param data
     */
    private byte write_count_4016 = 0,read_count_4016 = 0,write_count_4017 = 0,read_count_4017 = 0;
    public void write(int addr,byte data) {
        LogUtil.logf(" | WR:[ADDR:%d INDEX:%d DATA:%d]",addr&0xFFFF,addr&0xFFFF,data);
        switch (addr) {
            case 0x2000:
                DataBus.p_2000 = MemUtil.toBits(data);
                //将nametable选择写到临时t的10-11位
                DataBus.p_vram_temp_addr = (short) ((DataBus.p_vram_temp_addr & 0xf3ff) | ((data & 3) << 10));
                break;
            case 0x2001:
                DataBus.p_2001 = MemUtil.toBits(data);
                break;
            case 0x2003:
                DataBus.p_2003 = MemUtil.toBits(data);
                break;
            case 0x2004:
                byte arrd = (byte) ((MemUtil.bitsToByte(DataBus.p_2003)+1) & 0xff);
                DataBus.p_2003 = MemUtil.toBits(arrd);
                DataBus.writePpuSprRam(arrd,data);
                break;
            case 0x2005:
                //写两次 第一次x 第二次y
                if(!DataBus.p_write_toggle) {
                    //设置fine_x
                    DataBus.p_scroll_x = (byte) (data&0x7);
                    //设置coarse_x
                    DataBus.p_vram_temp_addr &= ~0x1F;
                    DataBus.p_vram_temp_addr |= ((data&0xff)>>3)&0x1F;
                } else {
                    //设置coarse_y
                    DataBus.p_vram_temp_addr &= ~0x3E0;
                    DataBus.p_vram_temp_addr |= (((data&0xff)>>3)<<5);
                    //设置fine_y
                    DataBus.p_vram_temp_addr &= ~0x7000;
                    DataBus.p_vram_temp_addr |= (data&0x7)<<12;
                }
                DataBus.p_write_toggle = !DataBus.p_write_toggle;
                break;
            case 0x2006:
                if(!DataBus.p_write_toggle) {
                    //高6位置0 PPU高7/8位无效 置0
                    DataBus.p_vram_temp_addr &= ~(0xFF<<8);
                    //第一次写将写入高6位;
                    DataBus.p_vram_temp_addr |= ((data&0x3F) << 8);
                }else {
                    //低8位置0
                    DataBus.p_vram_temp_addr &= ~0xFF;
                    //第二次写将写入低8位
                    DataBus.p_vram_temp_addr |= data&0xFF;
                    DataBus.p_vram_addr = DataBus.p_vram_temp_addr;
                }
                DataBus.p_write_toggle = !DataBus.p_write_toggle;
                break;
            case 0x2007:
                DataBus.writePpuMemory(DataBus.p_vram_addr,data);
                DataBus.p_vram_addr += (DataBus.p_2000[2]==0?1:32);
                break;
            //OAM DMA register (high byte)
            case 0x4014:
                short start = (short) (data<<8);
                for(int i=0; i < 256; i++) {
                    byte readData = read(start++);
                    DataBus.writePpuSprRam((byte) i,readData);
                }
                break;
            //输入设备写入
            case 0x4016:
                DataBus.c_4016 = 0;
                DataBus.c_4017 = 0;
                if(data == 1){
                    read_count_4016 = 0;
                    read_count_4017 = 0;
                }else{
                    for (byte i = 0; i < 8; i++) {
                        if(DataBus.c_4016_datas[i] == 1) {
                            DataBus.c_4016 = (byte) ((DataBus.c_4016) | 1<<i);
                        }
                        if(DataBus.c_4017_datas[i] == 1) {
                            DataBus.c_4017 = (byte) ((DataBus.c_4017) | 1<<i);
                        }
                    }
                }
                break;
            default:
                Const.mapper.write(addr,data);
                break;
        }
    }

    /**
     * 写块数据
     * @param addr
     * @param prgData
     */
    public void write(int addr,byte[] prgData){
        data = new byte[addr+prgData.length];
        System.arraycopy(prgData,0,data,addr,prgData.length);
    }

    public byte readMem(int addr){
        return this.data[addr];
    }
    public void writeMem(int addr,byte data){
        this.data[addr&0xffff] = data;
    }

    /**
     * 读数据
     * @param addr
     * @return
     */
    public byte read(int addr){
        if(addr<0x8000){
            LogUtil.logf(" | RD:[addr:%d INDEX:%d]",addr,addr);
        }
        switch (addr) {
            //读PPUSTATUS状态寄存器
            case 0x2002:
                byte readData = MemUtil.bitsToByte(DataBus.p_2002);
                //当CPU读取$2002后vblank标志设置为0
                DataBus.p_2002[7] = 0;
                DataBus.p_write_toggle = false;
                return readData;
            case 0x2007:
                int p2006 = DataBus.p_vram_addr&0x3fff;
                DataBus.p_vram_addr += (DataBus.p_2000[2]==0?1:32);
                if(addr <= 0x3EFF) {
                    //读取PPU
                    byte res = DataBus.p_2007_read;
                    DataBus.p_2007_read = DataBus.ppuMemory.read(p2006);
                    return res;
                }
                break;
            case 0x4016:
                byte returnNum = (byte) ((DataBus.c_4016 >> read_count_4016) & 1);
                read_count_4016++;
                return returnNum;
            case 0x4017:
                byte returnNum2 = (byte) ((DataBus.c_4017 >> read_count_4017) & 1);
                read_count_4017++;
                return returnNum2;
        }
        return Const.mapper.read(addr);
    }
}
