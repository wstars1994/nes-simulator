package com.iwstars.mcnes.ui;

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
    private BufferedImage image = new BufferedImage(256, 240, BufferedImage.TYPE_3BYTE_BGR);


    public NesUIRender(Frame frame) {
        this.frame = frame;
    }

    public void render(short[][] pixelColorBuff){
        for(int h=0; h<240; h++) {
            for(int w=0;w<256; w++) {
                short[] pixels = pixelColorBuff[w + (h*256)];
                int rgb = ((pixels[0] << 16) | ((pixels[1] << 8) | pixels[2]));
                image.setRGB(w, h, rgb );
            }
        }
        frame.getGraphics().drawImage(image, 0, 15, frame);
    }
}
