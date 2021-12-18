package com.wxclog.util;

import java.math.BigDecimal;

public class NesWatch {

    private double begin;
    private double end;
    private double spend;

    public void start(){
        begin = System.nanoTime();
    }

    public void stop(){
        end = System.nanoTime();
        spend = end - begin;
    }

    public double getNs(){
        return spend;
    }

    public double getUs(){
        return new BigDecimal(spend+"").divide(new BigDecimal(1000)).doubleValue();
    }

    public double getMs(){
        return new BigDecimal(getUs()+"").divide(new BigDecimal(1000)).doubleValue();
    }

}
