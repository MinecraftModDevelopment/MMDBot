package com.mcmoddev.mmdbot.client.util;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StyleUtils {

    public static void setRoundedCorners(Node node, double radius) {
        node.setStyle("-fx-background-radius: " + radius + ";" + System.lineSeparator() + node.getStyle());
    }

    public static void setBackgroundColour(Node node, Color colour) {
        node.setStyle("-fx-background-color: " + ColourUtils.toRGBAString(colour) + System.lineSeparator() + node.getStyle());
    }
}
