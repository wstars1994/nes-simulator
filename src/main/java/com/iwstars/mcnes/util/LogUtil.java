package com.iwstars.mcnes.util;

import com.iwstars.mcnes.Main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;

/**
 * 日志控制输出
 * @author WStars
 * @date 2020/5/4 8:46
 */
public class LogUtil {

    static BufferedWriter bw;
    static {
        try {
            bw = new BufferedWriter(new FileWriter("D:\\workspace\\mcnes\\log.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void logLn(String info) {
        if(Main.debug) {
            try {
                bw.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(info);
        }
    }
    public static void log(String info) {
//        if(Main.debug) {
//            try {
//                bw.write(info);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            System.out.print(info);
//        }
    }

    public static void logf(String  format,Object ...args) {
        if(Main.debug) {
//            System.out.printf(format,args);
            Formatter formatter = new Formatter();
            Formatter format1 = formatter.format(format, args);
            try {
                bw.write(format1.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
