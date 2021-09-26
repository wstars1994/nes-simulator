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

    public void beginVBlank(){
        //设置vblank true
        DataBus.p_2002[7] = 1;
        //Sprite 0 Hit false
        DataBus.p_2002[6] = 0;
    }
    public void endVBlank(){
        //设置vblank false
        DataBus.p_2002[7] = 0;
    }

    /**
     * 主板通电
     */
    public void powerUp(){
        int frame = 1;

        int perFrameMillis = 1000 / 50;

        while (true)  {
            long begin = System.currentTimeMillis();
            //256x240 分辨率
            short[][] renderBuff = new short[(256+16)*240][3];
            for (int i = 0; i < 240; i++) {
                if(DataBus.p_2001[3] == 1 || DataBus.p_2001[4] == 1){
                    DataBus.p_vram_addr = (short) ((DataBus.p_vram_addr & 0xfbe0) | (DataBus.p_vram_temp_addr & 0x041f));
                }
                short[][] shorts = ppu.preRender(i);
                for(int r = 0; r < 256+16; r++) {
                    renderBuff[i * 256 + r] = shorts[r];
                }
                this.cpu6502.go();
                if (DataBus.p_2001[3] == 1 || DataBus.p_2001[4] == 1) {
                    if ((DataBus.p_vram_addr & 0x7000) != 0x7000) {        // if fine Y < 7
                        DataBus.p_vram_addr += 0x1000;                     // increment fine Y
                    }else{
                        DataBus.p_vram_addr &= ~0x7000;                     // fine Y = 0
                        int y = (DataBus.p_vram_addr & 0x03E0) >> 5;        // let y = coarse Y
                        if (y == 29){
                            y = 0;// coarse Y = 0
                            DataBus.p_vram_addr ^= 0x0800; // switch vertical nametableelse if (y == 31)
                        }else if (y == 31) {
                            y = 0;                          // coarse Y = 0, nametable not switched
                        }else{
                            y += 1;                         // increment coarse Y
                        }
                        DataBus.p_vram_addr = (short) ((DataBus.p_vram_addr & ~0x03E0) | (y << 5));     // put coarse Y back into v
                    }
                }
            }
            this.beginVBlank();
            //nmi
            if(DataBus.p_2000[7] == 1){
                cpu6502.getCpuRegister().NMI();
            }
            //242-260
            for (int i = 241; i < 262; i++) {
                this.cpu6502.go();
            }
            this.endVBlank();
            //vblank结束后 如果有渲染 将t复制到v
            if(DataBus.p_2001[3] == 1 || DataBus.p_2001[4] == 1){
                DataBus.p_vram_addr = DataBus.p_vram_temp_addr;
            }
            //渲染图像
            nesRender.render(renderBuff);
            //模拟器运行延时
//            long end = System.currentTimeMillis();
//            if(end-begin<perFrameMillis){
//                try {
//                    Thread.sleep(perFrameMillis-(end-begin));
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }
}
