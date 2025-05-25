/*
 * Copyright 2025 msw
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.msw.masla.common.util;

import static org.springframework.util.StringUtils.commaDelimitedListToStringArray;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class StringUtil {

	/**
	 * 判断一个字符串Str是否为空 return true if it is supplied with an empty, zero length,
	 * or whitespace-only string. documented
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmptyString(String str) {
		return (str == null) || (str.length() == 0);
	}

	public static boolean isEmptyNull(String str) {
		return "null".equals(str);
	}

	/**
	 * 判断一个数组元素是否为空 return true if it is supplied with an empty, zero length, or
	 * whitespace-only string. documented
	 * 
	 * @param list
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isEmptyArray(List list) {
		return (list == null) || (list.size() == 0);
	}

	/**
	 * 将String数组转换成Integer数组
	 * 
	 * @param s
	 * @return
	 */
	public static Integer[] convertToIntegerArray(String[] s) {
		Integer[] num = new Integer[s.length];
		for (int i = 0; i < s.length; i++) {
			num[i] = new Integer(s[i]);
		}
		return num;
	}

	/**
	 * 将逗号分隔的数字字符串转换成Integer数组
	 */
	public static Integer[] splitCommaConvertToIntegerArray(String s) {
		return convertToIntegerArray(commaDelimitedListToStringArray(s));
	}

	/**
	 * 将字符串数组转换成字符串
	 * 
	 * @param str
	 * @return
	 */
	public static String arrayToString(String[] str) {
		if (str == null)
			return "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length; i++) {
			sb.append(str[i]);
			sb.append(", ");
		}
		return sb.toString();
	}

	// 判断字符串是否存在于指定的字符串数组中
	public static boolean isExist(String str, String[] array) {
		boolean result = false;
		if (array == null)
			return result;

		for (int i = 0; i < array.length; i++) {
			if (str.equals(array[i]))
				result = true;
		}
		return result;
	}

	/**
	 * 右对齐填充字符
	 * 
	 * @param data
	 * @param length
	 * @param fill
	 * @return
	 */
	public static String rightAlign(String data, int length, String fill) {
		for (int i = data.length(); i < length; i++) {
			data = fill + data;
		}
		return data;
	}

	/**
	 * 左对齐填充字符
	 * 
	 * @param data
	 * @param length
	 * @param fill
	 * @return
	 */
	public static String leftAlign(String data, int length, String fill) {
		for (int i = data.length(); i < length; i++) {
			data = data + fill;
		}
		return data;
	}

	/**
	 * MD5转换
	 * 
	 * @param plainText
	 * 
	 * @return MD5字符串
	 */
	public static String toMD5(String plainText) throws NoSuchAlgorithmException {

		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		messageDigest.update(plainText.getBytes());
		byte by[] = messageDigest.digest();

		StringBuffer buf = new StringBuffer();
		int val;
		for (int i = 0; i < by.length; i++) {
			val = by[i];
			if (val < 0) {
				val += 256;
			} else if (val < 16) {
				buf.append("0");
			}
			buf.append(Integer.toHexString(val));
		}
		return buf.toString();
	}

	public static boolean isEqualString(String arg0, String arg1) {
		return arg0.trim().equals(arg1.trim());
	}

	/**
	 * String转换成String数组 并对第一个元素赋值
	 * 
	 * @param arg
	 * @return
	 */
	public static String[] stringToStringArray(String arg) {
		String[] strArr = new String[1];
		strArr[0] = arg;
		return strArr;
	}

	/**
	 * 格式化字符串
	 * 
	 * @param arg
	 * @param objects
	 * @return
	 */
	public static String formatterString(String arg, Object... objects) {
		return MessageFormat.format(arg, objects);
	}

    /**
     * 字符串转list
     * @param str
     * @param regex
     * @return
     */
	public static List string2List(String str, String regex) {
	    if (isEmptyString(str) || isEmptyString(regex))
	        return null;
        String[] strings = str.split(regex);
        if (strings.length == 0)
            return null;
        List list = Arrays.asList(strings);
        return new ArrayList(list);
    }

    public static void main(String[] args) {
        /*String str = "1,2,3";
        List<Long> list = string2List(str, ",");
        System.out.println(list.get(0).getClass().getCanonicalName());
        System.out.println(new Long(100l));*/
    }

	/**
	 * Minify the json string
	 *
	 * @param jsonString formatted json string
	 * @return minify json string
	 */
	public static String minifyJSONString(String jsonString) {
		JSONObject jsonObject = JSON.parseObject(jsonString);
		return JSON.toJSONString(jsonObject, false);
	}
}
