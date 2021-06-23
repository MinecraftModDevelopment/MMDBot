package com.mcmoddev.bot.util;

public class StringUtilities {
    public static String arrayToString(String[] array, int start, String delimiter) {
        String ret = "";
        for (int i = start; i < array.length; i++) {
            ret += array[i];
            if (i < array.length - 1)
                ret += delimiter;
        }
        return ret;
    }
}
