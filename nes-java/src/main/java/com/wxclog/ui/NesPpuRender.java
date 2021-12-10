package com.wxclog.ui;

import com.wxclog.core.Const;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;

/**
 * 渲染
 * @author: WStars
 * @date: 2020-05-07 10:14
 */
public class NesPpuRender {

    private Canvas renderCanvas;

    public NesPpuRender(Canvas renderCanvas) {
        this.renderCanvas = renderCanvas;
    }

    public void render(short[][] pixelColorBuff){
        WritableImage writableImage = new WritableImage(256,240);
        for(int h = 0; h<240* Const.videoScale; h++) {
            for(int w = 0; w<(256)* Const.videoScale; w++) {
                short[] pixels = pixelColorBuff[(w / Const.videoScale) + ((h / Const.videoScale) * 256)];
                int rgb = ((pixels[0] << 24) |(pixels[0] << 16) | ((pixels[1] << 8) | pixels[2]));
                int a = rgb == 0?0x00:0xFF;
                writableImage.getPixelWriter().setArgb(w, h,(a<<24) | rgb);
            }
        }
        GraphicsContext context2D = renderCanvas.getGraphicsContext2D();
        context2D.drawImage(writableImage,0, 0);
    }
}
