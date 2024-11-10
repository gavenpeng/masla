package com.msw.masla.protocol.http.netty.util;

/**
 * check if can use native epoll
 * <p/>
 * 基于GLIBC2.10编译，而CentOS 5.8就只有GLIBC2.5
 * <p/>
 * check centOS ver >= 6.5
 *
 * @author Gavin.peng
 */
public class SystemUtil {

    public static boolean canUseNative() {

        try {
            if (System.getProperty("os.name").equalsIgnoreCase("linux")) {
                // 2.6.32-431.17.1.el6.x86_64
                String versionstr = System.getProperty("os.version");
                String[] verArray = versionstr.split("-");
                if (verArray.length > 0) {
                    String[] subVerArray = verArray[0].split("\\.");
                    if (subVerArray.length == 3) {
                        if (Integer.parseInt(subVerArray[0]) >= 2) {
                            return true;
                        } else if (Integer.parseInt(subVerArray[1]) >= 6) {
                            return true;
                        } else if (Integer.parseInt(subVerArray[2]) >= 32) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // pass
        }
        return false;
    }

    public static void main(String[] args) {

    }

}

