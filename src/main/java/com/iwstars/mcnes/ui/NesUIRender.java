package com.iwstars.mcnes.ui;

import com.iwstars.mcnes.Main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DirectColorModel;

/**
 * @description: 渲染controller
 * @author: WStars
 * @date: 2020-05-07 10:14
 */
public class NesUIRender {

    private Frame frame;
    private BufferedImage image;


    public NesUIRender(Frame frame) {
        this.frame = frame;
        image = new BufferedImage((256+16) * Main.videoScale, 240 * Main.videoScale, BufferedImage.TYPE_INT_ARGB);
    }

    public void render(short[][] pixelColorBuff){
        for(int h=0; h<240*Main.videoScale; h++) {
            for(int w=0;w<(256+16)*Main.videoScale; w++) {
                short[] pixels = pixelColorBuff[(w / Main.videoScale) + ((h / Main.videoScale) * 256)];
                int rgb = ((pixels[0] << 24) |(pixels[0] << 16) | ((pixels[1] << 8) | pixels[2]));
                int a = rgb == 0?0x00:0xFF;
                image.setRGB(w, h, (a<<24) | rgb );
            }
        }
        frame.getGraphics().drawImage(image, 2, 15, frame);
    }
}
