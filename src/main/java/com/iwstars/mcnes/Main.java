package com.iwstars.mcnes;

import com.iwstars.mcnes.core.DataBus;
import com.iwstars.mcnes.core.NesMobo;
import com.iwstars.mcnes.core.cpu.Cpu6502;
import com.iwstars.mcnes.core.mapper.IMapper;
import com.iwstars.mcnes.core.ppu.Ppu;
import com.iwstars.mcnes.net.NesNetMain;
import com.iwstars.mcnes.rom.HeaderData;
import com.iwstars.mcnes.rom.NESRomData;
import com.iwstars.mcnes.ui.NesUIRender;
import com.iwstars.mcnes.util.RomReaderUtil;

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
public class Main {

    /**
     * 调试模式
     */
    public static boolean debug = false;
    /**
     * 放大倍数 默认256*240
     */
    public static int videoScale = 1;

    private Frame frame;

    public static boolean controlMain = true;

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
            headerData.setMapperNo((byte) (headerData.getControlData1().getRomMapperLow() | headerData.getControlData2().getRomMapperHigh()<<4));
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
        new Main().launch();
    }

    private byte getKeyIndex(int keyCode){
        byte key = -1;
        switch (keyCode){
            //W UP
            case 87:
                key = 4;
                break;
            //S DOWN
            case 83:
                key = 5;
                break;
            //A LEFT
            case 65:
                key = 6;
                break;
            //D RIGHT
            case 68:
                key = 7;
                break;
            //1 SELECT
            case 49:
                key = 2;
                break;
            //2 START
            case 50:
                key = 3;
                break;
            //J A
            case 74:
                key = 0;
                break;
            //K B
            case 75:
                key = 1;
                break;
            default:
                key = -1;
        }
        return key;
    }

    private void launch() {
        frame = new Frame("MCNES");
        //设置窗口的大小和位置
        frame.setSize(260 * videoScale, 265*videoScale);
        //不可最大化
        frame.setResizable(false);
        //居中
        frame.setLocationRelativeTo(null);
        //显示
        frame.setVisible(true);

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                byte keyIndex = getKeyIndex(keyCode);
                if(keyIndex !=-1){
                    if(!controlMain){
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
                    if(!controlMain){
                        DataBus.c_4017_datas[keyIndex] = 0;
                    }else{
                        DataBus.c_4016_datas[keyIndex] = 0;
                    }
                    if(NesNetMain.channel!=null){
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

        new Thread(() -> {
            //读取.nes文件数据
            NESRomData romData = this.loadData("chd.nes");
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

            DataBus.cpuMemory.setMapper(IMapper.getMapper(headerData.getMapperNo()));
            //创建主板
            NesMobo nesMobo = new NesMobo();
            nesMobo.setPpu(ppu);
            nesMobo.setCpu6502(cpu6502);
            nesMobo.setNesRender(new NesUIRender(frame));
            nesMobo.reset();
            //网络启动
//            NesNetMain.init();
            //上电启动
            nesMobo.powerUp();
        }).start();
    }
}
