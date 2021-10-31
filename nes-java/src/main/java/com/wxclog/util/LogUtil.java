package com.wxclog.util;

import com.wxclog.Boot;
import com.wxclog.core.Const;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Formatter;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 日志控制输出
 * @author WStars
 * @date 2020/5/4 8:46
 */
public class LogUtil {

    static BufferedWriter bw;
    static LinkedBlockingQueue<String> logQueue;
    static {
        logQueue = new LinkedBlockingQueue<>();
        try {
            bw = new BufferedWriter(new FileWriter("log.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            while (true){
                String poll = logQueue.poll();
                if(poll!=null){
                    if(poll.equals("ln")){
                        System.out.println();
                        continue;
                    }
                    System.out.print(poll);
                }
            }
        }).start();
    }

    public static void log(String info) {
//        if(Const.debug) {
//            try {
//                bw.write(info);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            System.out.print(info);
//        }
    }

    public static void logf(String format,Object ...args) {
        if(Const.debug) {
            try {
//                System.out.printf(format,args);
                Formatter formatter = new Formatter();
                Formatter format1 = formatter.format(format, args);
                bw.write(format1.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
