package com.ammar.sharing.common;

public final class MathsUtils {
    private MathsUtils(){}

    public static boolean isDividableBy( int dividend, int divisor) {
        return dividend % divisor == 0;
    }
}
