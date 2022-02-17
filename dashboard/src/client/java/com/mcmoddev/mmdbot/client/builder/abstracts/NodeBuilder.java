package com.mcmoddev.mmdbot.client.builder.abstracts;

import javafx.scene.Node;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class NodeBuilder<N extends Node, B extends NodeBuilder<N, B>> implements Supplier<N> {

    private final N node;

    protected NodeBuilder(final N node) {
        this.node = node;
    }

    public N build() {
        return node;
    }

    @Override
    public final N get() {
        return build();
    }

    @SuppressWarnings("unchecked")
    protected final B castThis() {
        return (B) this;
    }

    protected final B doAndCast(Consumer<N> toDo) {
        toDo.accept(node);
        return castThis();
    }
}
