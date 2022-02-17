package com.mcmoddev.mmdbot.client.util;

import javafx.scene.paint.Color;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ColourUtils {

    public static String toRGBAString(Color colour) {
        int r = (int) Math.round(colour.getRed() * 255.0);
        int g = (int) Math.round(colour.getGreen() * 255.0);
        int b = (int) Math.round(colour.getBlue() * 255.0);
        int o = (int) Math.round(colour.getOpacity() * 255.0);
        return "rgba(%s, %s, %s, %s);".formatted(r, g, b, o);
    }

}
