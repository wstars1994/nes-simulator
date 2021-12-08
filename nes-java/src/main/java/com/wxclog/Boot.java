package com.wxclog;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wxclog.core.Const;
import com.wxclog.core.DataBus;
import com.wxclog.core.NesMobo;
import com.wxclog.core.cpu.Cpu6502;
import com.wxclog.core.mapper.IMapper;
import com.wxclog.core.ppu.Ppu;
import com.wxclog.net.NesNetMain;
import com.wxclog.rom.HeaderData;
import com.wxclog.rom.NESRomData;
import com.wxclog.ui.NesKeyAdapter;
import com.wxclog.ui.NesUIRender;
import com.wxclog.util.RomReaderUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    private JFrame frame;

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
        frame = new JFrame("FC");
        frame.setSize(260 * Const.videoScale, 260*Const.videoScale);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addKeyListener(new NesKeyAdapter());
        //退出
        frame.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        JPanel modelSelectPanel = new JPanel();
        modelSelectPanel.setLayout(null);

        JButton singleBtn = new JButton("单机");
        singleBtn.setBounds(20,60,70,70);
        JButton multiBtn = new JButton("联机");
        multiBtn.setBounds(155,60,70,70);
        modelSelectPanel.add(singleBtn);
        modelSelectPanel.add(multiBtn);
        frame.add(modelSelectPanel);

        singleBtn.addActionListener(e -> {
            modelSelectPanel.setVisible(false);
            this.run();
        });
        multiBtn.addActionListener(e -> {
            modelSelectPanel.setVisible(false);

            JPanel multiPanel = new JPanel();
            JButton createRoom = new JButton("创建房间");
            JLabel loadLabel = new JLabel("正在连接服务器");
            multiPanel.add(loadLabel);
            createRoom.addActionListener(e1 -> NesNetMain.send(1,null));
            frame.add(multiPanel);

            new Thread(()->{
                NesNetMain.connectServer((type, data) -> {
                    System.out.println(type+"   "+data);
                    switch (type){
                        case -1:
                            loadLabel.setText("连接成功,开始获取房间列表");
                            multiPanel.add(createRoom);
                            break;
                        case 0:
                            loadLabel.setVisible(false);
                            JSONObject list = JSONObject.parseObject(data);
                            JSONArray roomList = list.getJSONArray("roomList");
                            for (Object o : roomList) {
                                JSONObject room = (JSONObject) o;
                                JButton roomBtn = new JButton(room.getString("roomId"));
                                roomBtn.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        String actionCommand = e.getActionCommand();
                                        NesNetMain.send(2,actionCommand);
                                    }
                                });
                                multiPanel.add(roomBtn);
                                multiPanel.updateUI();
                            }
                            break;
                    }
                });
            }).start();
        });
    }

    private void run(){
        //调试模式 打印运行日志
//        Const.debug = false;
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


    public static void main(String[] args) {
        new Boot().launch();
    }
}
