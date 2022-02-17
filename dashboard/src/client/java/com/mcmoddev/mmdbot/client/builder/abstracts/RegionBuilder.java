package com.mcmoddev.mmdbot.client.builder.abstracts;

import com.mcmoddev.mmdbot.client.util.StyleUtils;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public abstract class RegionBuilder<R extends Region, B extends RegionBuilder<R, B>> extends NodeBuilder<R, B> {

    protected RegionBuilder(final R node) {
        super(node);
    }

    public B setBackgroundColour(final Color colour) {
        return doAndCast(n -> StyleUtils.setBackgroundColour(n, colour));
    }

    public B setRoundedCorners(final double radius) {
        return doAndCast(n -> StyleUtils.setRoundedCorners(n, radius));
    }

}
