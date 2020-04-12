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
            fileInputStream = new FileInputStream(new File("C:\\Users\\Administrator\\Desktop\\supermario.nes"));
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

    private static void printHex(byte b) {
        System.out.println(Integer.toHexString(b).toUpperCase());
    }


    private static void readHeader(DataInputStream dataInputStream) throws IOException {
        System.out.println("-------Header 读取开始(16K)-------");
        System.out.println("1.读取magic");
        Byte b = dataInputStream.readByte();
        System.out.print(new String(new byte[]{b}));
        b = dataInputStream.readByte();
        System.out.print(new String(new byte[]{b}));
        b = dataInputStream.readByte();
        System.out.print(new String(new byte[]{b}));
        System.out.println("");

        System.out.println("2.读取EOF(0x1A)");
        b = dataInputStream.readByte();
        printHex(b);

        System.out.println("3.读取程序镜像(ROM)数量");
        romNumber = dataInputStream.readByte();
        System.out.println(romNumber);

        System.out.println("4.读取图形镜像(VROM)数量");
        vromNumber = dataInputStream.readByte();
        System.out.println(vromNumber);

        System.out.println("5.读取8位附加信息(二进制)");
        b = dataInputStream.readByte();
        System.out.println(Integer.toBinaryString(b));

        System.out.println("6.读取8位附加信息(二进制)");
        b = dataInputStream.readByte();
        System.out.println(Integer.toBinaryString(b));

        System.out.println("7.读取程序RAM大小");
        b = dataInputStream.readByte();
        System.out.println(Integer.toBinaryString(b));

        System.out.println("8.读取8位附加信息(二进制)");
        b = dataInputStream.readByte();
        System.out.println(Integer.toBinaryString(b));

        System.out.println("9.读取8位附加信息(二进制)");
        b = dataInputStream.readByte();
        System.out.println(Integer.toBinaryString(b));

        System.out.println("10.读取Header数据最后保留5字节");
        int unknow1 = dataInputStream.readInt();
        b = dataInputStream.readByte();
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

        int imageWidth = 200;
        int imageHeight = 200;
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        int romDataSize = 8 * vromNumber;
        System.out.println("-------VROM读取开始(8K*VROM数量 = 8k x "+vromNumber+"="+romDataSize+"k)-------");
        System.out.println("字模点阵读取");
//        for (int y = 0; y < 16;y++){
//            for(int x = 0; x < 32; x++){
//                draw(x,y,dataInputStream,graphics);
//            }
//        }
        draw(1,1,dataInputStream,graphics);
        ImageIO.write(image, "PNG", new File("C:\\Users\\Administrator\\Desktop\\2.png"));
        graphics.dispose();
        System.out.println("-------VROM程序主体读取结束(8K*VROM数量 = 8k x "+vromNumber+"="+romDataSize+"k)-------");
    }

    private static void draw(int x,int y,DataInputStream dataInputStream,Graphics graphics) throws IOException {
        int[] high = new int[8];
        int[] low = new int[8];
        for(int j=0;j<2;j++){
            for(int i=0;i<8;i++){
                byte b = dataInputStream.readByte();
                if(j==0) {
                    high[i] = b;
                    String s = Integer.toString(b & 0xff, 2);
                    String s1 = binaryStrto8(s);
                    System.out.println(s1);
                    char[] chars = s1.toCharArray();
                    for(int cc=0;cc<8;cc++) {
                        int c =Integer.parseInt(chars[cc]+"");
                        if(c==0) {
                            graphics.setColor(Color.white);
                        }else {
                            graphics.setColor(new Color(255,0,0));
                        }
                        graphics.drawRect(cc,i,1,1);
                    }
                }else{
                    low[i]=b;
                }
            }
//            if(j%2==1) {
//                for(int m=0;m<8;m++){
//                    int highData = (high[m] >> (7 - m) << 1 ) & 2;
//                    int lowData = (low[m] >> (7 - m)) & 1;
//                    int d = highData | lowData;
//                    if(d==1) {
//                        graphics.setColor(new Color(255,0,0));
//                    }else if (d==2) {
//                        graphics.setColor(new Color(0,255,0));
//                    }else if(d==3) {
//                        graphics.setColor(new Color(0,0,255));
//                    }else{
//                        graphics.setColor(Color.BLACK);
//                    }
//                    graphics.drawRect(x,y,1,1);
//                }
//            }
        }
    }

    private static String binaryStrto8(String binStr){
        int bLength = binStr.length();
        for(int i=bLength;i<8;i++) {
            binStr="0"+binStr;
        }
        return binStr;
    }

}
