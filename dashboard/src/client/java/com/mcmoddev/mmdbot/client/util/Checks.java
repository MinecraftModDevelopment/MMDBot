package com.mcmoddev.mmdbot.client.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Checks {

    public static <T> T notNull(T object, String name) {
        if (object == null) {
            throw new NullPointerException(name);
        }
        return object;
    }

}
