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

    public static HashMap<Integer,Integer> gamepadMapping = new HashMap<>();
    static {
        gamepadMapping.put(87,4);
        gamepadMapping.put(83,5);
        gamepadMapping.put(65,6);
        gamepadMapping.put(68,7);
        gamepadMapping.put(49,2);
        gamepadMapping.put(50,3);
        gamepadMapping.put(74,0);
        gamepadMapping.put(75,1);
    }

}
