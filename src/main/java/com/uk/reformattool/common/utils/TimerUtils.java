package com.uk.reformattool.common.utils;

public class TimerUtils {
    private static long START_TIMER;

    public static void start() {
        START_TIMER = System.currentTimeMillis();
    }

    public static double end() {
        return (System.currentTimeMillis() - START_TIMER) / 1000d;
    }
}
