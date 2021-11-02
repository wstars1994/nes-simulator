package com.wxclog.util;

import com.wxclog.core.Const;

import java.io.BufferedWriter;
import java.util.Formatter;

/**
 * 日志控制输出
 * @author WStars
 * @date 2020/5/4 8:46
 */
public class LogUtil {

    static BufferedWriter bw;

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
