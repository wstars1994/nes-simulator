package com.iwstars.mcnes;

import com.iwstars.mcnes.core.DataBus;
import com.iwstars.mcnes.core.NesMobo;
import com.iwstars.mcnes.core.cpu.Cpu6502;
import com.iwstars.mcnes.core.ppu.Ppu;
import com.iwstars.mcnes.rom.HeaderData;
import com.iwstars.mcnes.rom.NESRomData;
import com.iwstars.mcnes.ui.NesUIRender;
import com.iwstars.mcnes.util.RomReaderUtil;
import lombok.Cleanup;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @description: 主程序入口
 * @author: WStars
 * @date: 2020-04-17 10:38
 */
public class Main {

    /**
     * 调试模式
     */
    public static boolean debug = true;

    private Frame frame;
    /**
     * 加载.nes文件数据到内存
     * @param nesFile
     * @return
     */
    public NESRomData loadData(File nesFile) {
        NESRomData romData = new NESRomData();
        try {
            @Cleanup InputStream fileInputStream = new FileInputStream(nesFile);
            @Cleanup DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            HeaderData headerData = RomReaderUtil.readHeader(dataInputStream);
            romData.setHeaderData(headerData);
            //16k PRG-ROM
            romData.setRomPRG(RomReaderUtil.readRomData(dataInputStream,headerData.getRomPRGSize(),16));
            if(headerData.getRomPRGSize() == 1) {
                byte[] romPRG = romData.getRomPRG();
                byte[] full = new byte[romPRG.length * 2];
                System.arraycopy(romPRG,0,full,0,romPRG.length);
                System.arraycopy(romPRG,0,full,romPRG.length,romPRG.length);
                romData.setRomPRG(full);
            }
            //8k CHR-ROM
            romData.setRomCHR(RomReaderUtil.readRomData(dataInputStream,headerData.getRomCHRSize(),8));
        }catch (Exception e) {
            e.printStackTrace();
        }
        return romData;
    }

    public static void main(String[] args) {
        debug = true;
        new Main().launch();
    }

    private void launch() {
        frame = new Frame("MCNES");
        //设置窗口的大小和位置
        frame.setSize(260, 255);
        //不可最大化
        frame.setResizable(false);
        //居中
        frame.setLocationRelativeTo(null);
        //退出
        frame.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        //显示
        frame.setVisible(true);
        //运行模拟器
        new Thread(() -> {
            String filePath = "1.nes";
            //读取.nes文件数据
            NESRomData romData = this.loadData(new File(filePath));
            //创建PPU
            Ppu ppu = new Ppu(romData.getRomCHR());
            //创建CPU
            Cpu6502 cpu6502 = new Cpu6502(romData.getRomPRG());
            //挂载到总线
            DataBus.cpuMemory = cpu6502.getCpuMemory();
            DataBus.ppuMemory = ppu.getPpuMemory();
            //创建主板
            NesMobo nesMobo = new NesMobo();
            nesMobo.setPpu(ppu);
            nesMobo.setCpu6502(cpu6502);
            nesMobo.setNesRender(new NesUIRender(frame));
            nesMobo.reset();
            //上电启动
            nesMobo.powerUp();
        }).start();
    }
}
