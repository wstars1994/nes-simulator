package com.wxclog.ui;

import com.wxclog.core.Const;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 渲染
 * @author: WStars
 * @date: 2020-05-07 10:14
 */
public class NesPpuRender {

    private Frame frame;
    private BufferedImage image;

    public NesPpuRender(Frame frame) {
        this.frame = frame;
        image = new BufferedImage((256) * Const.videoScale, 240 * Const.videoScale, BufferedImage.TYPE_INT_ARGB);
    }

    public void render(int[] pixelColorBuff){
        for(int h=0; h<240*Const.videoScale; h++) {
            for(int w=0;w<(256)*Const.videoScale; w++) {
                int pixels = pixelColorBuff[(w / Const.videoScale) + ((h / Const.videoScale) * 256)];
                int rgb1 = image.getRGB(w, h);
                if(pixels!=rgb1){
                    image.setRGB(w, h, pixels);
                }
            }
        }
        frame.getGraphics().drawImage(image, 2, 20, frame);
//        System.out.println("渲染："+nesWatch.getMs());
    }
}
