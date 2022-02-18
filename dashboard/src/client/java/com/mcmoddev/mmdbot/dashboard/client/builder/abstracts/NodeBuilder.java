/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
package com.mcmoddev.mmdbot.dashboard.client.builder.abstracts;

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

    public B setStyle(String value) {
        return doAndCast(n -> n.setStyle(value + System.lineSeparator() + n.getStyle()));
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
