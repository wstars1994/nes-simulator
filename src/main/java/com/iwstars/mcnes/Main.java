package com.iwstars.mcnes;

import com.iwstars.mcnes.core.NesMobo;
import com.iwstars.mcnes.core.cpu.Cpu6502;
import com.iwstars.mcnes.core.ppu.Ppu;
import com.iwstars.mcnes.rom.HeaderData;
import com.iwstars.mcnes.rom.NESRomData;
import com.iwstars.mcnes.ui.NesRenderController;
import com.iwstars.mcnes.util.RomReaderUtil;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import lombok.Cleanup;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * @description: 主程序入口
 * @author: WStars
 * @date: 2020-04-17 10:38
 */
public class Main extends Application {

    /**
     * 调试模式
     */
    public static boolean debug = false;

    private NesRenderController nesRender;

    @Override
    public void start(Stage primaryStage) throws Exception{
        URL resource = getClass().getResource("/nesRender.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(resource);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 300));
        this.nesRender = fxmlLoader.getController();
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });
        //加载程序
        new Thread(()->{
            String filePath = "1.nes";
            //读取.nes文件数据
            NESRomData romData = this.loadData(new File(filePath));
            //创建PPU
            Ppu ppu = new Ppu(romData.getRomCHR());
            //创建CPU
            Cpu6502 cpu6502 = new Cpu6502(romData.getRomPRG());
            //主板
            NesMobo nesMobo = new NesMobo();
            nesMobo.setPpu(ppu);
            nesMobo.setCpu6502(cpu6502);
            nesMobo.setNesRender(this.nesRender);
            nesMobo.reset();
            //上电启动
            nesMobo.powerUp();
        }).start();
    }

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
        launch(args);
        debug = false;
    }
}
