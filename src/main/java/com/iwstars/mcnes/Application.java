package com.iwstars.mcnes;

import com.iwstars.mcnes.core.ppu.PPU;
import com.iwstars.mcnes.rom.HeaderData;
import com.iwstars.mcnes.rom.NESRomData;
import com.iwstars.mcnes.util.RomReaderUtil;
import lombok.Cleanup;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @description: 主程序入口
 * @author: WStars
 * @date: 2020-04-17 10:38
 */
public class Application {

    public NESRomData loadData(File nesFile) {
        NESRomData romData = new NESRomData();
        try {
            @Cleanup InputStream fileInputStream = new FileInputStream(nesFile);
            @Cleanup DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            HeaderData headerData = RomReaderUtil.readHeader(dataInputStream);
            romData.setHeaderData(headerData);
            //16k PRG-ROM
            romData.setRomPRG(RomReaderUtil.readRomData(dataInputStream,headerData.getRomPRGSize(),16));
            //8k CHR-ROM
            romData.setRomCHR(RomReaderUtil.readRomData(dataInputStream,headerData.getRomCHRSize(),8));
        }catch (Exception e) {
            e.printStackTrace();
        }
        return romData;
    }

    public static void main(String[] args) {
        Application application = new Application();
        String filePath = "1.nes";
        //读取.nes文件数据
        NESRomData romData = application.loadData(new File(filePath));
        //创建PPU
        PPU ppu = new PPU(romData.getRomCHR());
        //读取程序数据
        byte[] romPRG = romData.getRomPRG();
        //每8位渲染
        byte[] nameTableData = new byte[8];
        for (int i=0;i<8;i++) {
            System.out.println(romPRG[i]&0xFF);
            nameTableData[i] = romPRG[i];
        }
        ppu.renderNameTable(nameTableData);
    }
}
