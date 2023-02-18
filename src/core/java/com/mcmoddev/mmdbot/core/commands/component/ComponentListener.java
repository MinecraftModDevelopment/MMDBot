/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mcmoddev.mmdbot.core.commands.component.context.ButtonInteractionContext;
import com.mcmoddev.mmdbot.core.commands.component.context.ModalInteractionContext;
import com.mcmoddev.mmdbot.core.commands.component.context.SelectMenuInteractionContext;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectInteraction;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.modals.Modal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
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

    @NonNull
    public StringSelectMenu.Builder createMenu(@NonNull Component.Lifespan lifespan, final String... args) {
        final var comp = new Component(name, UUID.randomUUID(), Arrays.asList(args), lifespan);
        insertComponent(comp);
        return StringSelectMenu.create(comp.uuid().toString());
    }

    @NonNull
    public Modal.Builder createModal(@NonNull final String label, @NonNull final Component.Lifespan lifespan, final String... args) {
        return createModal(label, lifespan, Arrays.asList(args));
    }

    @NonNull
    public Modal.Builder createModal(@NonNull final String label, @NonNull final Component.Lifespan lifespan, final List<String> args) {
        final var comp = new Component(name, UUID.randomUUID(), args, lifespan);
        insertComponent(comp);
        return Modal.create(comp.uuid().toString(), label);
    }

    public void insertComponent(final Component component) {
        manager.getStorage().insertComponent(component);
    }

    @CanIgnoreReturnValue
    public Component insertComponent(final UUID id, final Component.Lifespan lifespan, final String... args) {
        final var comp = new Component(getName(), id, Arrays.asList(args), lifespan);
        insertComponent(comp);
        return comp;
    }

    void setManager(final ComponentManager manager) {
        this.manager = manager;
    }

    public abstract void onButtonInteraction(final ButtonInteractionContext context);

    public abstract void onSelectMenuInteraction(final SelectMenuInteractionContext context);

    public abstract void onModalInteraction(final ModalInteractionContext context);

    public static Builder builder(String featureId, Consumer<? super ComponentListener> whenBuilt) {
        return new Builder(featureId, whenBuilt);
    }

    public static final class Builder {
        private final String name;
        private final Consumer<? super ComponentListener> whenBuilt;
        private Consumer<? super ButtonInteractionContext> onButton;
        private Consumer<? super SelectMenuInteractionContext> onSelectMenu;
        private Consumer<? super ModalInteractionContext> onModal;

        Builder(final String name, final Consumer<? super ComponentListener> whenBuilt) {
            this.name = name;
            this.whenBuilt = whenBuilt;
        }

        /**
         * Sets the action that should be executed on {@link Button} interaction.
         *
         * @param onButton the action that should be executed on button interaction
         * @return the builder instance
         */
        public Builder onButtonInteraction(final Consumer<? super ButtonInteractionContext> onButton) {
            this.onButton = onButton;
            return this;
        }

        /**
         * Sets the action that should be executed on {@link net.dv8tion.jda.api.interactions.components.selections.SelectMenu} interaction.
         *
         * @param onSelectMenu the action that should be executed on select menu interaction
         * @return the builder instance
         */
        public Builder onSelectMenuInteraction(final Consumer<? super SelectMenuInteractionContext> onSelectMenu) {
            this.onSelectMenu = onSelectMenu;
            return this;
        }

        /**
         * Sets the action that should be executed on {@link Modal} interaction.
         *
         * @param onModal the action that should be executed on modal interaction
         * @return the builder instance
         */
        public Builder onModalInteraction(final Consumer<? super ModalInteractionContext> onModal) {
            this.onModal = onModal;
            return this;
        }

        /**
         * Builds the listener.
         *
         * @return the built listener
         */
        public ComponentListener build() {
            final Consumer<? super ButtonInteractionContext> onButton = this.onButton == null ? b -> {
            } : this.onButton;
            final Consumer<? super SelectMenuInteractionContext> onSelectMenu = this.onSelectMenu == null ? b -> {
            } : this.onSelectMenu;
            final Consumer<? super ModalInteractionContext> onModal = this.onModal == null ? b -> {
            } : this.onModal;
            final var lis = new ComponentListener(name) {
                @Override
                public void onButtonInteraction(final ButtonInteractionContext context) {
                    onButton.accept(context);
                }

                @Override
                public void onSelectMenuInteraction(final SelectMenuInteractionContext context) {
                    onSelectMenu.accept(context);
                }

                @Override
                public void onModalInteraction(final ModalInteractionContext context) {
                    onModal.accept(context);
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
