package com.mcmoddev.mmdbot.client.util;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StyleUtils {

    public static void applyStyle(Node... nodes) {
        for (final var node : nodes) {
            if (node instanceof Button button) {
                setBackgroundColour(button, Color.GRAY.brighter());
                setRoundedCorners(button, 6);
            }
        }
    }

    public static void setRoundedCorners(Node node, double radius) {
        node.setStyle("-fx-background-radius: " + radius);
    }

    public static void setBackgroundColour(Node node, Color colour) {
        int r = (int) Math.round(colour.getRed() * 255.0);
        int g = (int) Math.round(colour.getGreen() * 255.0);
        int b = (int) Math.round(colour.getBlue() * 255.0);
        int o = (int) Math.round(colour.getOpacity() * 255.0);
        node.setStyle("-fx-background-color: " + "rgba(%s, %s, %s, %s);".formatted(r, g, b, o));
    }
}
