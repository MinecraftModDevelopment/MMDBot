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
    private final ComponentManager manager;

    ComponentListener(final String name, final ComponentManager manager) {
        this.name = name;
        this.manager = manager;
    }

    public Button createButton(@Nonnull ButtonStyle style, @Nullable String label, @Nullable Emoji emoji, @Nonnull Component.Lifespan lifespan, String... args) {
        final var comp = new Component(name, UUID.randomUUID(), List.of(args), lifespan);
        manager.getStorage().insertComponent(comp);
        return Button.of(style, comp.uuid().toString(), label, emoji);
    }

    public abstract void onButtonInteraction(final ButtonInteractionContext context);

    public static final class Builder {
        private final String name;
        private final ComponentManager manager;
        private Consumer<ButtonInteractionContext> onButton;

        Builder(final String name, final ComponentManager manager) {
            this.name = name;
            this.manager = manager;
        }

        public Builder onButtonInteraction(final Consumer<ButtonInteractionContext> onButton) {
            this.onButton = onButton;
            return this;
        }

        public ComponentListener build() {
            final Consumer<ButtonInteractionContext> onButton = this.onButton == null ? b -> {} : this.onButton;
            final var lis = new ComponentListener(name, manager) {
                @Override
                public void onButtonInteraction(final ButtonInteractionContext context) {
                    onButton.accept(context);
                }
            };
            manager.listeners.put(name, lis);
            return lis;
        }
    }
}
