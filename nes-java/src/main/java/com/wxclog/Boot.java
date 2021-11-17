package com.wxclog;

import com.wxclog.core.Const;
import com.wxclog.core.DataBus;
import com.wxclog.core.NesMobo;
import com.wxclog.core.cpu.Cpu6502;
import com.wxclog.core.mapper.IMapper;
import com.wxclog.core.ppu.Ppu;
import com.wxclog.net.NesNetMain;
import com.wxclog.rom.HeaderData;
import com.wxclog.rom.NESRomData;
import com.wxclog.ui.NesUIRender;
import com.wxclog.util.RomReaderUtil;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.FileInputStream;

/**
 * @description: 主程序入口
 * @author: WStars
 * @date: 2020-04-17 10:38
 */
public class Boot {

    private Frame frame;

    /**
     * 加载.nes文件数据到内存
     * @param filePath
     * @return
     */
    public NESRomData loadData(String filePath) {
        NESRomData romData = new NESRomData();
        try {
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(filePath));
            HeaderData headerData = RomReaderUtil.readHeader(dataInputStream);
            headerData.setMapperNo((byte) (headerData.getControlData1().getRomMapperLow() | (headerData.getControlData2().getRomMapperHigh()<<4)));
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

    private void launch() {
        frame = new Frame("NES游戏机");
        frame.setSize(260 * Const.videoScale, 260*Const.videoScale);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                byte keyIndex = getKeyIndex(keyCode);
                if(keyIndex !=-1){
                    if(!Const.gamepadMain){
                        DataBus.c_4017_datas[keyIndex] = 1;
                    }else{
                        DataBus.c_4016_datas[keyIndex] = 1;
                    }
                    if(NesNetMain.channel!=null){
                        NesNetMain.channel.writeAndFlush(keyIndex);
                    }
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
                int keyCode = e.getKeyCode();
                byte keyIndex = getKeyIndex(keyCode);
                if(keyIndex !=-1){
                    if(!Const.gamepadMain){
                        DataBus.c_4017_datas[keyIndex] = 0;
                    }else{
                        DataBus.c_4016_datas[keyIndex] = 0;
                    }
                    if(NesNetMain.channel != null){
                        NesNetMain.channel.writeAndFlush(keyIndex*10);
                    }
                }
            }
        });
        //退出
        frame.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        //调试模式 打印运行日志
        Const.debug = false;
        new Thread(() -> {
            //读取.nes文件数据
            NESRomData romData = this.loadData("../超级玛丽.nes");
            HeaderData headerData = romData.getHeaderData();
            byte romPRGSize = headerData.getRomPRGSize();
            byte romCHRSize = headerData.getRomCHRSize();
            System.out.println("Game MapperNo: "+headerData.getMapperNo());
            byte fourScreen = headerData.getControlData1().getFourScreen();
            System.out.println("Game Four-Screen Mirroring: "+ (fourScreen==0?"F":"T"));
            byte mirrorType = headerData.getControlData1().getMirrorType();
            System.out.println("Game Mirroring: "+ (mirrorType==0?"H":"V"));
            System.out.println("PRG ROM Size: "+romPRGSize);
            System.out.println("CHR ROM Size: "+romCHRSize);
            //创建CPU
            Cpu6502 cpu6502 = new Cpu6502(romData.getRomPRG());
            //创建PPU
            Ppu ppu = new Ppu(romData.getRomCHR(), headerData.getControlData1().getMirrorType());
            //挂载到总线
            DataBus.cpuMemory = cpu6502.getCpuMemory();
            DataBus.ppuMemory = ppu.getPpuMemory();
            //设置mapper
            Const.mapper = IMapper.getMapper(headerData.getMapperNo(), romPRGSize, romCHRSize, romData.getRomCHR());
            //创建主板
            NesMobo nesMobo = new NesMobo();
            nesMobo.setPpu(ppu);
            nesMobo.setCpu6502(cpu6502);
            nesMobo.setNesRender(new NesUIRender(frame));
            nesMobo.reset();
            //启动
            nesMobo.powerUp();
        }).start();
    }

    private byte getKeyIndex(int keyCode){
        byte key = -1;
        Integer data = Const.gamepadMapping.get(keyCode);
        if(data != null){
            return data.byteValue();
        }
        return key;
    }

    public static void main(String[] args) {
        new Boot().launch();
    }
}
