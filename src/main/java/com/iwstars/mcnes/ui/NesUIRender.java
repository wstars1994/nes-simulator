package com.iwstars.mcnes.ui;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;

/**
 * @description: 渲染controller
 * @author: WStars
 * @date: 2020-05-07 10:14
 */
public class NesUIRender {

    @FXML
    private Canvas mainCanvas;

    public void render(short[][] pixelColorBuff){
        GraphicsContext graphics = mainCanvas.getGraphicsContext2D();
        PixelWriter pixelWriter = graphics.getPixelWriter();
        for(int h=0; h<240; h++) {
            for(int w=0;w<256;w++) {
                short[] i = pixelColorBuff[w + (h*256)];
                int color = ((0xFF << 24)|(i[0] << 16)|(i[1] << 8)|i[2]);
                pixelWriter.setArgb(w,h,color);
            }
        }
    }
}
