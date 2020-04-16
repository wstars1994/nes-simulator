package com.iwstars.mcnes;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * @author WStars
 * @date 2020/4/12 18:28
 */
public class Main {

    private static int romNumber = 0;
    private static int vromNumber = 0;


    public static void main(String[] args) {
        InputStream fileInputStream = null;
        try{
            fileInputStream = new FileInputStream(new File("C:\\Users\\王新晨\\Desktop\\1.nes"));
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            readHeader(dataInputStream);
            readRom(dataInputStream);
            readVRom(dataInputStream);
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(fileInputStream!=null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void readHeader(DataInputStream dataInputStream) throws IOException {
        System.out.println("-------Header 读取开始(16K)-------");
        System.out.println("1.读取magic");
        byte[] magic = new byte[4];
        dataInputStream.read(magic,0,4);
        System.out.println(new String(magic));

        System.out.println("3.读取程序镜像(ROM)数量");
        romNumber = dataInputStream.readByte();
        System.out.println(romNumber);

        System.out.println("4.读取图形镜像(VROM)数量");
        vromNumber = dataInputStream.readByte();
        System.out.println(vromNumber);

        System.out.println("5.读取8位附加信息(其中后四位为ROM Mapper的低4位)");
        byte b = dataInputStream.readByte();
        System.out.println(binaryStrto8(Integer.toString(b&0xFF,2)));

        System.out.println("6.读取8位附加信息(副4位Mapper号 + ROM Mapper的高4位)");
        b = dataInputStream.readByte();
        System.out.println(binaryStrto8(Integer.toString(b&0xFF,2)));

        System.out.println("7.读取8字节附加信息(保留0)");
        dataInputStream.readLong();
        System.out.println("-------Header 读取结束(16K)-------");
        System.out.println("");
    }

    private static void readRom(DataInputStream dataInputStream) throws IOException {
        int romDataSize = 16 * romNumber;
        System.out.println("-------ROM读取开始(16K*ROM数量 = 16k x "+romNumber+"="+romDataSize+"k)-------");
        dataInputStream.skipBytes(1024*romDataSize);
        System.out.println("-------ROM读取结束(16K*ROM数量 = 16k x "+romNumber+"="+romDataSize+"k)-------");
        System.out.println("");
    }

    private static void readVRom(DataInputStream dataInputStream) throws IOException {
        //256x240
        int romDataSize = 8 * vromNumber;
        System.out.println("-------VROM读取开始(8K*VROM数量 = 8k x "+vromNumber+"="+romDataSize+"k)-------");
        System.out.println("字模点阵读取");
        System.out.println(dataInputStream.available());
        BufferedImage bufferedImage = new BufferedImage(256,128,BufferedImage.TYPE_INT_RGB);
        Graphics graphics = bufferedImage.getGraphics();
        for (int y = 0; y <16;y++){
            for(int x = 0; x < 32; x++){
                draw(x,y,dataInputStream,graphics);
            }
        }
        ImageIO.write(bufferedImage,"png",new File("D:\\1.png"));
        graphics.dispose();
        System.out.println("-------VROM程序主体读取结束(8K*VROM数量 = 8k x "+vromNumber+"="+romDataSize+"k)-------");
    }

    private static void draw(int x, int y, DataInputStream dataInputStream, Graphics graphics) throws IOException {
        //低8byte
        byte [] lowTile = getTile(dataInputStream);
        //高8byte
        byte [] highTile = getTile(dataInputStream);
        for(int t=0;t<8;t++) {
            byte low = lowTile[t];
            byte high = highTile[t];
            for(int i=0;i<8;i++) {
                //组合 高低位
                int highBit = ((high >> (7-i)) & 1)<<1;
                int lowBit = (low >> (7-i)) & 1;
                //调色板颜色 组成4位颜色中的低两位,高两位在属性表中获得
                int color = highBit | lowBit;
                if(color == 0) {
                    graphics.setColor(Color.LIGHT_GRAY);
                }else if(color == 1){
                    graphics.setColor(Color.RED);
                }else if(color == 2){
                    graphics.setColor(Color.orange);
                }else if(color == 3){
                    graphics.setColor(new Color(123,161,6));
                }
                graphics.drawRect((x*8)+i,(y*8)+t,1,1);
            }
        }
    }

    /**
     * 读取8Byte tile 从左至右
     * @param dataInputStream
     * @return
     * @throws IOException
     */
    private static byte[] getTile(DataInputStream dataInputStream) throws IOException {
        byte[] data = new byte[8];
        for(int i=0;i<8;i++) {
            data[i] = dataInputStream.readByte();
        }
        return data;
    }

    private static String binaryStrto8(String binStr){
        int bLength = binStr.length();
        for(int i=bLength;i<8;i++) {
            binStr="0"+binStr;
        }
        return binStr;
    }

}
