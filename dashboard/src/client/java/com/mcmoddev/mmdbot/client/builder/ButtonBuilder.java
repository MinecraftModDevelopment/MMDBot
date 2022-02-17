package com.mcmoddev.mmdbot.client.builder;

import com.mcmoddev.mmdbot.client.builder.abstracts.LabeledBuilder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

public final class ButtonBuilder extends LabeledBuilder<Button, ButtonBuilder> {

    public ButtonBuilder(final Button button) {
        super(button);
    }

    public ButtonBuilder(final String text) {
        this(new Button(text));
    }

    public ButtonBuilder() {
        this(new Button());
    }

    public ButtonBuilder setOnAction(EventHandler<ActionEvent> action) {
        return doAndCast(b -> b.setOnAction(action));
    }
}
