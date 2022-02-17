package com.mcmoddev.mmdbot.client.builder.abstracts;

import javafx.scene.control.Control;

public class ControlBuilder<C extends Control, B extends ControlBuilder<C, B>> extends RegionBuilder<C, B> {
    protected ControlBuilder(final C node) {
        super(node);
    }
}
