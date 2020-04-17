package com.iwstars.mcnes;

import com.iwstars.mcnes.rom.HeaderData;
import com.iwstars.mcnes.rom.RomData;
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

    public RomData loadData(File nesFile) {
        RomData romData = new RomData();
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
        String filePath = "C:\\Users\\王新晨\\Desktop\\1.nes";
        RomData romData = application.loadData(new File(filePath));
        System.out.println(romData);
    }
}
