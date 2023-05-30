package org.fran.demo.springsecurity.jwttoken.util;

import java.util.Collection;

public abstract class Assert {
    public static void mustNull(Object object, String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void mustNull(Collection collections, String message) {
        if (collections != null && collections.size() >0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNull(String str, String message) {
        if (StringUtils.isEmpty(str)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
