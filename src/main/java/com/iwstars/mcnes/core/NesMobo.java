package com.iwstars.mcnes.core;

import com.iwstars.mcnes.core.cpu.Cpu6502;
import com.iwstars.mcnes.core.cpu.CpuMemory;
import com.iwstars.mcnes.core.cpu.CpuRegister;
import com.iwstars.mcnes.core.ppu.Ppu;
import com.iwstars.mcnes.util.LogUtil;
import com.iwstars.mcnes.util.MemUtil;
import lombok.Setter;

/**
 * @description: nes模拟板
 * @author: WStars
 * @date: 2020-04-19 10:06
 */
@Setter
public class NesMobo {

    /**
     * CPU
     */
    private Cpu6502 cpu6502;

    /**
     * PPU图形处理
     */
    private Ppu ppu;

    /**
     * 主板通电
     */
    public void powerUp(){
        while (true)  {
            long start = System.nanoTime();
            //256x240 分辨率
            //设置vblank false
            DataBus.p_2002[7] = 0;
            for (int i = 0; i < 262; i++) {
                //HBlank start
                if(i<240) {
                    LogUtil.logLn("");
                    LogUtil.logLn("");
                    ppu.startRender(i);
                }
                //VBlank
                if(i==241) {
                    //设置vblanking
                    DataBus.p_2002[7] = 1;
                    //NMI中断
                    if(DataBus.p_2000[7] == 1) {
                        CpuRegister.NMI(cpu6502.getCpuMemory());
                    }
                }
                this.cpu6502.go();
            }
            long elapsed = System.nanoTime() - start;
            long wait = (long) (1.0 / 60 - elapsed / 1e-9);
            try {
                if (wait > 0) {
                    Thread.sleep(wait);
                }
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * 断电
     */
    public void powerDown(){

    }

    /**
     * 复位
     */
    public void reset(){
        CpuMemory cpuMemory = cpu6502.getCpuMemory();
        int addr = MemUtil.concatByte(cpuMemory.read(0xFFFC), cpuMemory.read(0xFFFD));
        cpuMemory.setPrgPc(addr);
    }
}
