package com.msw.masla.common.util;

/**
 * Created by Gavin.peng on 2017/6/9.
 */
public class StringBuilderHolder {

    // 公共的Holder
    private static ThreadLocal<StringBuilder> globalStringBuilder = new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder(512);
        }
    };

    // 独立的Holder
    private ThreadLocal<StringBuilder> stringBuilder = new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder(initSize);
        }
    };

    private int initSize;

    /**
     * 创建独立的Holder.
     *
     * 用于StringBuilder在使用过程中，会调用其他可能也使用StringBuilderHolder的子函数.
     *
     * Holder 要重用
     *
     * @param initSize StringBulder的初始大小, 建议512,如果容量不足将进行扩容，扩容后的数组将一直保留.
     */
    public StringBuilderHolder(int initSize) {
        this.initSize = initSize;
    }

    /**
     * 获取公共Holder的StringBuilder.
     *
     * 当StringBuilder会被连续使用，期间不会调用其他可能也使用StringBuilderHolder的子函数时使用.
     *
     * 重置StringBuilder内部的writerIndex, 而char[]保留不动.
     */
    public static StringBuilder getGlobal() {
        StringBuilder sb = globalStringBuilder.get();
        sb.setLength(0);
        return sb;
    }

    /**
     * 获取独立Holder的StringBuilder.
     *
     * 重置StringBuilder内部的writerIndex, 而char[]保留不动.
     */
    public StringBuilder get() {
        StringBuilder sb = stringBuilder.get();
        sb.setLength(0);
        return sb;
    }
}
