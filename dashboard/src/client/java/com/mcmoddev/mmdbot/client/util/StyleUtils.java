package com.mcmoddev.mmdbot.client.util;

import com.mcmoddev.mmdbot.client.builder.ButtonBuilder;
import com.mcmoddev.mmdbot.client.builder.TextFieldBuilder;
import com.mcmoddev.mmdbot.client.builder.abstracts.NodeBuilder;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import lombok.experimental.UtilityClass;

import java.util.Objects;
import java.util.stream.Stream;

@UtilityClass
public class StyleUtils {

    public static void applyStyle(Node... nodes) {
        final var nodeBuilders = Stream.of(nodes)
            .<NodeBuilder<?, ?>>map(node -> {
                if (node instanceof Button btn) {
                    return new ButtonBuilder(btn);
                } else if (node instanceof TextField txt) {
                    return new TextFieldBuilder(txt);
                }
                return null;
            })
            .filter(Objects::nonNull)
            .toList().toArray(NodeBuilder[]::new);
        applyStyle(nodeBuilders);
    }

    public static void applyStyle(NodeBuilder<?, ?>... nodes) {
        for (final var node : nodes) {
            if (node instanceof ButtonBuilder button) {
                button.setBackgroundColour(Color.GRAY.brighter())
                    .setRoundedCorners(6);
            } else if (node instanceof TextFieldBuilder textField) {
                textField.setRoundedCorners(5);
            }
        }
    }

    public static void setRoundedCorners(Node node, double radius) {
        node.setStyle("-fx-background-radius: " + radius + ";" + System.lineSeparator() + node.getStyle());
    }

    public static void setBackgroundColour(Node node, Color colour) {
        node.setStyle("-fx-background-color: " + ColourUtils.toRGBAString(colour) + System.lineSeparator() + node.getStyle());
    }
}
