package com.msw.masla.common.util;

/**
 * @author: acone.wu
 */
public class MathUtils {

    private static final double DEFAULT_DELTA = 0.000001;

    public static long mod(int hashCode) {
        if (hashCode == Integer.MIN_VALUE) {
            return 0;
        } else {
            return Math.abs(hashCode);
        }
    }

    /**
     * 判断double 类型是否相等
     */
    public static boolean equal(double v1, double v2) {
        return Double.compare(v1, v2) == 0 || Math.abs(v1 - v2) <= DEFAULT_DELTA;
    }
}
