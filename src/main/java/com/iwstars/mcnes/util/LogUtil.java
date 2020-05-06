package com.iwstars.mcnes.util;

import com.iwstars.mcnes.Application;

/**
 * 日志控制输出
 * @author WStars
 * @date 2020/5/4 8:46
 */
public class LogUtil {

    public static void logLn(String info) {
        if(Application.debug) {
            System.out.println(info);
        }
    }
    public static void log(String info) {
        if(Application.debug) {
            System.out.print(info);
        }
    }

    public static void logf(String  format,Object ...args) {
        if(Application.debug) {
            System.out.printf(format,args);
        }
    }
}
