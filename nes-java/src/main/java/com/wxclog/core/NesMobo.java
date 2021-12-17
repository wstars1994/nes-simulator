package com.wxclog.core;

import com.wxclog.core.cpu.Cpu6502;
import com.wxclog.core.cpu.CpuMemory;
import com.wxclog.core.ppu.Ppu;
import com.wxclog.ui.NesPpuRender;
import com.wxclog.util.MemUtil;

/**
 * @description: nes模拟板
 * @author: WStars
 * @date: 2020-04-19 10:06
 */
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
    private NesPpuRender nesRender;

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

    public void beginVBlank() {
        //设置vblank true
        DataBus.p_2002[7] = 1;
        //Sprite 0 Hit false
        DataBus.p_2002[6] = 0;
    }

    public void endVBlank(){
        //设置vblank false
        DataBus.p_2002[7] = 0;
        //vblank结束后 如果有渲染 将t复制到v
        if(DataBus.showBg()||DataBus.showSpr()){
            DataBus.p_vram_addr = DataBus.p_vram_temp_addr;
        }
    }

    /**
     * 主板通电
     */
    public void powerUp(){
        byte perFrameMillis = 1000 / 1000;
        int[] renderBuff = new int[(256+16)*240];
        short[][] frameData = new short[240][3];
        byte[][] frameSpriteData = new byte[240][2];
        long s = System.currentTimeMillis();
        int frame = 0;
        while (true)  {
//            NesWatch watch = new NesWatch();
//            watch.start();
            long begin = System.currentTimeMillis();
            for (int i = 0; i < 240; i++) {
//                System.out.println(i);
                if(DataBus.showBg() || DataBus.showSpr()){
                    DataBus.p_vram_addr = (short) ((DataBus.p_vram_addr & 0xfbe0) | (DataBus.p_vram_temp_addr & 0x041f));
                }
                ppu.preRender(i,frameData,frameSpriteData);
                this.cpu6502.go();
                this.coarseY();
            }
            if(DataBus.showBg() || DataBus.showSpr()) {
                ppu.renderNameTable(frameData,renderBuff);
                ppu.renderSprite(renderBuff,frameSpriteData);
                nesRender.render(renderBuff);
            }
            this.beginVBlank();
            this.cpu6502.go();
            //nmi
            if(DataBus.p_2000[7] == 1){
                cpu6502.getCpuRegister().NMI();
            }
            //242-260
            for (int i = 242; i < 262; i++) {
                this.cpu6502.go();
            }
            this.endVBlank();
//            watch.stop();

            if(System.currentTimeMillis()-s>=1000){
                System.out.println(frame);
                s = System.currentTimeMillis();
                frame=0;
            }else{
                frame++;
            }

//            System.out.println(watch.getMs());
//            this.delay(begin,perFrameMillis);
        }
    }

    private void delay(long begin, byte perFrameMillis) {
        //模拟器运行延时
        long end = System.currentTimeMillis();
        if(end-begin < perFrameMillis){
            try {
                Thread.sleep(perFrameMillis-(end-begin));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void coarseY() {
        if (DataBus.showBg()||DataBus.showSpr()) {
            // if fine Y < 7
            if ((DataBus.p_vram_addr & 0x7000) != 0x7000) {
                // increment fine Y
                DataBus.p_vram_addr += 0x1000;
            }else{
                // fine Y = 0
                DataBus.p_vram_addr &= ~0x7000;
                // let y = coarse Y
                int y = (DataBus.p_vram_addr & 0x03E0) >> 5;
                if (y == 29){
                    // coarse Y = 0
                    y = 0;
                    // switch vertical nametableelse if (y == 31)
                    DataBus.p_vram_addr ^= 0x0800;
                }else if (y == 31) {
                    // coarse Y = 0, nametable not switched
                    y = 0;
                }else{
                    // increment coarse Y
                    y += 1;
                }
                // put coarse Y back into v
                DataBus.p_vram_addr = (short) ((DataBus.p_vram_addr & ~0x03E0) | (y << 5));
            }
        }
    }

    public void setCpu6502(Cpu6502 cpu6502) {
        this.cpu6502 = cpu6502;
    }

    public void setPpu(Ppu ppu) {
        this.ppu = ppu;
    }

    public void setNesRender(NesPpuRender nesRender) {
        this.nesRender = nesRender;
    }
}
