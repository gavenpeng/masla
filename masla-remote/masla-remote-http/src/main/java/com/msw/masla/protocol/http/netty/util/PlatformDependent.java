package com.msw.masla.protocol.http.netty.util;


import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by Gavin.peng on 2017/9/7.
 */
public class PlatformDependent {


    /**
     * Returns a new concurrent {@link Deque}.
     */
    public static <C> Deque<C> newConcurrentDeque() {
        if (majorVersionFromJavaSpecificationVersion() < 7) {
            return new LinkedBlockingDeque<C>();
        } else {
            return new ConcurrentLinkedDeque<C>();
        }
    }


    static int majorVersionFromJavaSpecificationVersion() {
        try {
            final String javaSpecVersion = AccessController.doPrivileged(new PrivilegedAction<String>() {
                @Override
                public String run() {
                    return System.getProperty("java.specification.version");
                }
            });
            return majorVersion(javaSpecVersion);
        } catch (SecurityException e) {
            return 6;
        }
    }

    static int majorVersion(final String javaSpecVersion) {
        final String[] components = javaSpecVersion.split("\\.");
        final int[] version = new int[components.length];
        for (int i = 0; i < components.length; i++) {
            version[i] = Integer.parseInt(components[i]);
        }

        if (version[0] == 1) {
            assert version[1] >= 6;
            return version[1];
        } else {
            return version[0];
        }
    }


}
