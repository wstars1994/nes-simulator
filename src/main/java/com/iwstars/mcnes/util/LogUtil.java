package com.iwstars.mcnes.util;

/**
 * 日志控制输出
 * @author WStars
 * @date 2020/5/4 8:46
 */
public class LogUtil {

    private static boolean logFlag = false;

    public static void logLn(String info) {
        if(logFlag) {
            System.out.println(info);
        }
    }
    public static void log(String info) {
        if(logFlag) {
            System.out.print(info);
        }
    }

    public static void logf(String  format,Object ...args) {
        if(logFlag) {
            System.out.printf(format,args);
        }
    }
}
