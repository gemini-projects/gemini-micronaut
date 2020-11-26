package it.at7.gemini.micronaut.core;

import java.util.Collection;
import java.util.Map;

public class CheckArgument {

    public static void isNotNull(Object t, String message) {
        if (t == null)
            throw new IllegalArgumentException(message);
    }

    public static void notEmpty(String st, String message) {
        if (st == null || st.isEmpty())
            throw new IllegalArgumentException(message);
    }

    public static void notEmpty(Collection c, String message) {
        if (c == null || c.isEmpty())
            throw new IllegalArgumentException(message);
    }

    public static void notEmpty(Map c, String message) {
        if (c == null || c.isEmpty())
            throw new IllegalArgumentException(message);
    }

    public static void isTrue(boolean condition, String message) {
        if(!condition)
            throw new IllegalArgumentException(message);
    }
}
