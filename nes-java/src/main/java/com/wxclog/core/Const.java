package com.wxclog.core;

import com.wxclog.core.mapper.IMapper;

import java.awt.*;
import java.util.HashMap;

/**
 * 常量值
 *
 * @author WStars
 * @date 2021/10/31 11:28
 */
public class Const {

    /**
     * 调试模式
     */
    public static boolean debug;
    /**
     * 放大倍数 默认256*240
     * 会影响游戏运行速度
     */
    public static int videoScale = 1;
    /**
     * 主控制器
     */
    public static boolean gamepadMain = true;

    public static IMapper mapper;

    public static HashMap<String,Integer> gamepadMapping = new HashMap<>();
    static {
        gamepadMapping.put("w",4);
        gamepadMapping.put("s",5);
        gamepadMapping.put("a",6);
        gamepadMapping.put("d",7);
        gamepadMapping.put("1",2);
        gamepadMapping.put("2",3);
        gamepadMapping.put("j",0);
        gamepadMapping.put("k",1);
    }

}
