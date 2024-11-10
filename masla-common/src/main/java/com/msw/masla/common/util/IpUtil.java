package com.msw.masla.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpUtil {
	
	protected static final Logger logger = LoggerFactory.getLogger(IpUtil.class);

	//标准IPv4地址的正则表达式：
	private static final Pattern IPV4_REGEX =
			Pattern.compile(
					"^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

	//无全0块，标准IPv6地址的正则表达式
	private static final Pattern IPV6_STD_REGEX =
			Pattern.compile(
					"^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");

	//非边界压缩正则表达式
	private static final Pattern IPV6_COMPRESS_REGEX =
			Pattern.compile(
					"^((?:[0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4})*)?)::((?:([0-9A-Fa-f]{1,4}:)*[0-9A-Fa-f]{1,4})?)$");

	//边界压缩情况正则表达式
	private static final Pattern IPV6_COMPRESS_REGEX_BORDER =
			Pattern.compile(
					"^(::(?:[0-9A-Fa-f]{1,4})(?::[0-9A-Fa-f]{1,4}){5})|((?:[0-9A-Fa-f]{1,4})(?::[0-9A-Fa-f]{1,4}){5}::)$");

	/**
	 * 当前机器的ip地址
	 */
	private static InetAddress inetAddress = null;

	static{
		try {
			inetAddress = InetAddress.getLocalHost();
			
			if( inetAddress.getHostAddress().equals("127.0.0.1") ){
				throw new UnknownHostException("please configure hostname");
			}
		} catch (UnknownHostException e) {
			logger.error("InetAddress.getLocalHost error.", e);
			try {
				throw new UnknownHostException("please configure hostname");
			} catch (UnknownHostException e1) {
				logger.error("InetAddress.getLocalHost error.", e1);
			}
		}
	}

	/**
	 * 获取当前机器的ip
	 */
	public static String getIp() {
		if (inetAddress !=null) {
			return inetAddress.getHostAddress();
		}
		return null;
	}

	/**
	 * 获取当前机器的域名
	 */
	public static String getHostName() {
		if (inetAddress !=null) {
			return inetAddress.getHostName();
		}
		return null;
	}


	/**
	 * 判断是否为合法IPv4地址
	 */
	public static boolean isIPv4Address(final String input) {
		return IPV4_REGEX.matcher(input).matches();
	}

	/**
	 * 判断是否为合法IPv6地址
	 */
	public static boolean isIPv6Address(final String input) {
		int NUM = 0;
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) == ':') NUM++;
		}
		//合法IPv6地址中不可能有多余7个的冒号(:)
		if (NUM > 7) return false;
		if (IPV6_STD_REGEX.matcher(input).matches()) {
			return true;
		}
		// 冒号(:)数量等于7有两种情况：无压缩、边界压缩，所以需要特别进行判断
		if (NUM == 7) {
			return IPV6_COMPRESS_REGEX_BORDER.matcher(input).matches();
		}
		// 冒号(:)数量小于七，使用于飞边界压缩的情况
		else {
			return IPV6_COMPRESS_REGEX.matcher(input).matches();
		}
	}


	public static void main(String args[]) {
        String ipAddr = "A00:a00:100:f261::F15";
//        String ipAddr = "fe80:1295:8030:49ec:1fc6:57fa:0000:0000";
//        String ipAddr = "A00:a00:100::f261:F15";
//        String ipAddr = "fe80:1295:8030:49ec:1fc6:57fa::";
//		String ipAddr = "192.168.1.1";
		System.out.println(ipAddr);
		if (isIPv6Address(ipAddr)) {
			System.out.println("IPV6地址");
		} else if (isIPv4Address(ipAddr)) {
			System.out.println("IPV4地址");
		} else {
			System.out.println("不合法地址");
		}
	}

}
