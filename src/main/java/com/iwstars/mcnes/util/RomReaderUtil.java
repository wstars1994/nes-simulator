package com.iwstars.mcnes.util;

import com.iwstars.mcnes.rom.HeaderData;
import com.iwstars.mcnes.rom.HeaderRomControl1Data;
import com.iwstars.mcnes.rom.HeaderRomControl2Data;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * @description: nesrom数据
 * @author: WStars
 * @date: 2020-04-17 10:43
 */
public class RomReaderUtil {


    /**
     * 读取头数据
     * @param dataInputStream
     * @return
     * @throws IOException
     */
    public static HeaderData readHeader(DataInputStream dataInputStream) throws IOException {
        HeaderData headerData = new HeaderData();
        byte n = dataInputStream.readByte();
        byte e = dataInputStream.readByte();
        byte s = dataInputStream.readByte();
        String magic = new String(new byte[]{n, e, s});
        if(!"NES".equals(magic)) {
            throw new RuntimeException("magic检验错误,不是一个nes文件");
        }
        headerData.setMagic(magic);
        headerData.setMagicEof(dataInputStream.readByte());
        headerData.setRomPRGSize(dataInputStream.readByte());
        headerData.setRomCHRSize(dataInputStream.readByte());
        headerData.setHeaderRomControlData(readHeaderRomControl1Data(dataInputStream));
        headerData.setHeaderRomControl2Data(readHeaderRomControl2Data(dataInputStream));
        headerData.setZero(dataInputStream.readLong());
        return headerData;
    }

    private static HeaderRomControl1Data readHeaderRomControl1Data(DataInputStream dataInputStream) throws IOException {
        HeaderRomControl1Data headerRomControl1Data = new HeaderRomControl1Data();
        byte b = dataInputStream.readByte();
        headerRomControl1Data.setMirrorType(b&0x1);
        headerRomControl1Data.setSRAMEnabled((b&0x11)>>1);
        headerRomControl1Data.setTrainerPresent((b&0x111)>>2);
        headerRomControl1Data.setVram4((b&0xF)>>3);
        headerRomControl1Data.setRomMapperLow(getMapper(b));
        return headerRomControl1Data;
    }

    private static HeaderRomControl2Data readHeaderRomControl2Data(DataInputStream dataInputStream) throws IOException {
        HeaderRomControl2Data headerRomControl2Data = new HeaderRomControl2Data();
        byte b = dataInputStream.readByte();
        headerRomControl2Data.setZero1(new byte[]{0,0,0,0});
        headerRomControl2Data.setRomMapperHigh(getMapper(b));
        return headerRomControl2Data;
    }

    private static byte[] getMapper(byte b) {
        byte []mapper= new byte[4];
        int m = b >> 4;
        mapper[0] = (byte) (m&0x1);
        mapper[1] = (byte) ((m&0x11)>>1);
        mapper[2] = (byte) ((m&0x111)>>2);
        mapper[3] = (byte) ((m&0xF)>>3);
        return mapper;
    }

    public static byte[] readRomData(DataInputStream dataInputStream, byte number,int baseSize) throws IOException {
        byte[] data = new byte[baseSize * 1024 * number];
        dataInputStream.read(data);
        return data;
    }
}
