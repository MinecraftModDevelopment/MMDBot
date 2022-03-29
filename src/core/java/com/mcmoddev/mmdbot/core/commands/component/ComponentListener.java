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
package com.mcmoddev.mmdbot.core.commands.component;

import lombok.NonNull;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class ComponentListener {
    private final String name;
    private ComponentManager manager;

    ComponentListener(final String name) {
        this.name = name;
    }

    @NonNull
    public Button createButton(@Nonnull ButtonStyle style, @Nullable String label, @Nullable Emoji emoji, @Nonnull Component.Lifespan lifespan, List<String> args) {
        final var comp = new Component(name, UUID.randomUUID(), args, lifespan);
        insertComponent(comp);
        return Button.of(style, comp.uuid().toString(), label, emoji);
    }

    @NonNull
    public Button createButton(@Nonnull ButtonStyle style, @Nullable Emoji emoji, @Nonnull Component.Lifespan lifespan, List<String> args) {
        return createButton(style, null, emoji, lifespan, args);
    }

    public void insertComponent(final Component component) {
        manager.getStorage().insertComponent(component);
    }

    void setManager(final ComponentManager manager) {
        this.manager = manager;
    }

    public abstract void onButtonInteraction(final ButtonInteractionContext context);

    public static Builder builder(String featureId, Consumer<? super ComponentListener> whenBuilt) {
        return new Builder(featureId, whenBuilt);
    }

    public static final class Builder {
        private final String name;
        private final Consumer<? super ComponentListener> whenBuilt;
        private Consumer<? super ButtonInteractionContext> onButton;

        Builder(final String name, final Consumer<? super ComponentListener> whenBuilt) {
            this.name = name;
            this.whenBuilt = whenBuilt;
        }

        public Builder onButtonInteraction(final Consumer<? super ButtonInteractionContext> onButton) {
            this.onButton = onButton;
            return this;
        }

        public ComponentListener build() {
            final Consumer<? super ButtonInteractionContext> onButton = this.onButton == null ? b -> {} : this.onButton;
            final var lis = new ComponentListener(name) {
                @Override
                public void onButtonInteraction(final ButtonInteractionContext context) {
                    onButton.accept(context);
                }
            };
            if (whenBuilt != null) {
                whenBuilt.accept(lis);
            }
            return lis;
        }
    }

    public String getName() {
        return name;
    }
}
