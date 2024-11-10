package com.msw.masla.common.util;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 签名工具类
 *
 * @author jimmy.zhong
 */
public class SignUtils {

    private static final String SECRET_KEY = "7OpDKuw37dLBALcehQwQA8t2jX9YJkWj";


    /**
     * 计算请求签名
     *
     * @param params 请求参数
     * @return 签名字符串
     */
    public static String sign(Map<String, String> params) {
        return sign(params, SECRET_KEY);
    }


    /**
     * 计算请求签名
     *
     * @param params 请求参数
     * @param secretKey 秘钥
     * @return 签名字符串
     */
    public static String sign(Map<String, String> params, String secretKey) {
        SortedMap<String, String> sortedParams = new TreeMap<String, String>(params);
        sortedParams.remove("sign");
        StringBuilder sb = new StringBuilder();
        for (String key : sortedParams.keySet()) {
            String val = sortedParams.get(key);
            sb.append(key).append("=").append(StringUtils.defaultIfEmpty(val, "")).append("|");
        }
        return md5(sb.append(secretKey).toString());
    }


    public static final String md5(String input) {
        return DigestUtils.md5Hex(input);
    }

    public static boolean checkSign(Map<String, String> paramMap) {
        String sign = paramMap.get("sign");
        if (SignUtils.sign(paramMap).equals(sign)) {
            return true;
        }
        return false;
    }


    public static void main(String[] args) {
        String secretKey = RandomStringUtils.randomAlphanumeric(32);
        System.out.println(secretKey);
    }
}
