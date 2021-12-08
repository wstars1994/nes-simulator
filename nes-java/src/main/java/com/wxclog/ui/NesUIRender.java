package com.wxclog.ui;

import com.wxclog.core.Const;

import javax.swing.*;
import java.awt.image.BufferedImage;

/**
 * 渲染
 * @author: WStars
 * @date: 2020-05-07 10:14
 */
public class NesUIRender {

    private JFrame frame;
    private BufferedImage image;


    public NesUIRender(JFrame frame) {
        this.frame = frame;
        image = new BufferedImage((256) * Const.videoScale, 240 * Const.videoScale, BufferedImage.TYPE_INT_ARGB);
    }

    public void render(short[][] pixelColorBuff){
        for(int h = 0; h<240* Const.videoScale; h++) {
            for(int w = 0; w<(256)* Const.videoScale; w++) {
                short[] pixels = pixelColorBuff[(w / Const.videoScale) + ((h / Const.videoScale) * 256)];
                int rgb = ((pixels[0] << 24) |(pixels[0] << 16) | ((pixels[1] << 8) | pixels[2]));
                int a = rgb == 0?0x00:0xFF;
                image.setRGB(w, h, (a<<24) | rgb );
            }
        }
        frame.getGraphics().drawImage(image, 2, 20, frame);
    }
}
