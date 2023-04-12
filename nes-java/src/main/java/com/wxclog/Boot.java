package com.wxclog;

import com.wxclog.core.Const;
import com.wxclog.ui.NesKeyAdapter;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @description: 主程序入口
 * @author: WStars
 * @date: 2020-04-17 10:38
 */
public class Boot {

    private Frame frame;

    private void launch() {
        frame = new Frame("NES游戏机");
        frame.setSize(260 * Const.videoScale, 260*Const.videoScale);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        //退出
        frame.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.addKeyListener(new NesKeyAdapter());
        //调试模式 打印运行日志
        Const.debug = false;
        NesBoot.start(frame);
    }

    public static void main(String[] args) {
        new Boot().launch();
    }
}
