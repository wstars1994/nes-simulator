package com.iwstars.mcnes.core;

import com.iwstars.mcnes.core.cpu.Cpu6502;
import com.iwstars.mcnes.core.cpu.CpuMemory;
import com.iwstars.mcnes.core.ppu.Ppu;
import com.iwstars.mcnes.ui.NesUIRender;
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
     * CPU6502
     */
    private Cpu6502 cpu6502;
    /**
     * PPU图形处理
     */
    private Ppu ppu;
    /**
     * 屏幕输出
     */
    private NesUIRender nesRender;

    /**
     * 主板通电
     */
    public void powerUp(){
        while (true)  {
            //256x240 分辨率
            short[][] renderBuff = new short[256*240][3];
            for (int i = 0; i < 240; i++) {
                this.cpu6502.go();
                short[][] shorts = ppu.preRender(i);
                for(int r = 0; r < 256; r++) {
                    renderBuff[i * 256 + r] = shorts[r];
                }
            }
            //240
            this.cpu6502.go();
            //241
            //设置vblank true
            DataBus.p_2002[7] = 1;
            //NMI中断
            if(DataBus.p_2000[7] == 1) {
                cpu6502.getCpuRegister().NMI();
            }
            //242-
            for (int i = 242; i < 262; i++) {
                if( i == 261 ) {
                    //Sprite 0 Hit false
                    DataBus.p_2002[6] = 0;
                    //设置vblank false
                    DataBus.p_2002[7] = 0;
                }
                this.cpu6502.go();
            }
            nesRender.render(renderBuff);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 复位
     */
    public void reset(){
        CpuMemory cpuMemory = cpu6502.getCpuMemory();
        int initPc = MemUtil.concatByte(cpuMemory.read(0xFFFC), cpuMemory.read(0xFFFD));
        cpuMemory.setPrgPc(initPc);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
