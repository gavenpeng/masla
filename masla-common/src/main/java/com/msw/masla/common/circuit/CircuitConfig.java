package com.msw.masla.common.circuit;

/**
 * Created by Gavin.peng on 2018/1/17.
 */
public class CircuitConfig {
    //自动升降级比例
    public static long CIRCUIT_UP_AND_DOWN_PERCENT = 5;
    //自动升级的条件,即错误数少于等于该值时，可以升级
    public static float CIRCUIT_LOW_WATER_MARK = 0.5f;

    public static int MIDDLE_PERCENT_LEVEL = 4;//错误比例达到50%后，直接熔断50%
}
