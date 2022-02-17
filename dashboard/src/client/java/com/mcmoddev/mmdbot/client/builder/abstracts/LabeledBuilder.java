package com.mcmoddev.mmdbot.client.builder.abstracts;

import javafx.scene.control.Labeled;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

public class LabeledBuilder<L extends Labeled, B extends LabeledBuilder<L, B>> extends ControlBuilder<L, B> {

    protected LabeledBuilder(final L node) {
        super(node);
    }

    public B setFont(final Font font) {
        return doAndCast(n -> n.setFont(font));
    }

    public B setTextFill(final Paint fill) {
        return doAndCast(n -> n.setTextFill(fill));
    }
}
