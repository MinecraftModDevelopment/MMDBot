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
package com.mcmoddev.mmdbot.core.commands.paginate;

import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.component.ComponentListener;
import com.mcmoddev.mmdbot.core.commands.component.context.ButtonInteractionContext;
import lombok.NonNull;
import net.dv8tion.jda.api.MessageBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class PaginatorBuilder {
    private final ComponentListener.Builder componentListener;
    private Consumer<? super ButtonInteractionContext> buttonInteractionHandler;
    private Component.Lifespan lifespan = Component.Lifespan.TEMPORARY;
    private int itemsPerPage = 25;
    private boolean dismissible = true;
    private boolean buttonsOwnerOnly;
    private Paginator.MessageGetter messageGetter = (startingIndex, maximum, arguments) -> new MessageBuilder("Unknown page.");
    private Paginator.ButtonFactory buttonFactory = Paginator.DEFAULT_BUTTON_FACTORY;
    private List<Paginator.ButtonType> buttonOrder = Paginator.DEFAULT_BUTTON_ORDER;

    PaginatorBuilder(final ComponentListener.Builder componentListener) {
        this.componentListener = componentListener;
    }

    /**
     * Sets the lifespan of the pagination buttons.
     *
     * @param lifespan the lifespan of the pagination buttons
     * @return the builder instance
     */
    public PaginatorBuilder lifespan(@NonNull final Component.Lifespan lifespan) {
        this.lifespan = lifespan;
        return this;
    }

    /**
     * Sets the amount of items a page has.
     *
     * @param itemsPerPage the amount of items a page has
     * @return the builder instance
     */
    public PaginatorBuilder itemsPerPage(final int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
        return this;
    }

    /**
     * Sets if the paginated messages are dismissible.
     *
     * @param dismissible if the paginated messages are dismissible
     * @return the builder instance
     */
    public PaginatorBuilder dismissible(final boolean dismissible) {
        this.dismissible = dismissible;
        return this;
    }

    /**
     * Sets if the "scroll" buttons can only be used by the owner of the sent message. <br>
     * This is mainly used for commands that display user-specific information.
     *
     * @param buttonsOwnerOnly if the "scroll" buttons can only be used by their owner
     * @return the builder instance
     */
    public PaginatorBuilder buttonsOwnerOnly(final boolean buttonsOwnerOnly) {
        this.buttonsOwnerOnly = buttonsOwnerOnly;
        return this;
    }

    /**
     * Sets the getter used for building the message "pages".
     *
     * @param messageGetter the getter used for building the message "pages"
     * @return the builder instance
     */
    public PaginatorBuilder message(@NonNull final Paginator.MessageGetter messageGetter) {
        this.messageGetter = messageGetter;
        return this;
    }

    /**
     * Sets the {@link com.mcmoddev.mmdbot.core.commands.paginate.Paginator.ButtonFactory factory} used for creating the pagination buttons.
     *
     * @param factory the factory used for the pagination buttons
     * @return the builder instance
     */
    public PaginatorBuilder buttonFactory(@NonNull final Paginator.ButtonFactory factory) {
        this.buttonFactory = factory;
        return this;
    }

    /**
     * Sets the order of the buttons. Not including a button type in the list will make it not generate.
     *
     * @param order the button order
     * @return the builder instance
     * @throws IllegalArgumentException if the order contains duplicate elements
     */
    public PaginatorBuilder buttonOrder(@NonNull final List<Paginator.ButtonType> order) {
        this.buttonOrder = order;
        if (buttonOrder.size() != buttonOrder.stream().distinct().count()) {
            throw new IllegalArgumentException("Cannot have duplicate elements in the button order!");
        }
        return this;
    }

    /**
     * Sets the order of the buttons. Not including a button type in the list will make it not generate.
     *
     * @param order the button order
     * @return the builder instance
     * @throws IllegalArgumentException if the order contains duplicate elements
     */
    public PaginatorBuilder buttonOrder(@NonNull final Paginator.ButtonType... order) {
        return buttonOrder(Arrays.asList(order));
    }

    /**
     * Sets the handler that will paginate messages. <br>
     * If {@code null} or not set, the handler will be the default one. <br>
     * Most implementations don't need to mess with it.
     *
     * @param buttonInteractionHandler the handler that will paginate messages
     * @return the builder instance
     */
    public PaginatorBuilder buttonInteractionHandler(@Nullable final Consumer<? super ButtonInteractionContext> buttonInteractionHandler) {
        this.buttonInteractionHandler = buttonInteractionHandler;
        return this;
    }

    /**
     * Builds the {@link Paginator}.
     *
     * @return the built paginator
     */
    public Paginator build() {
        return new PaginatorImpl(componentListener, buttonInteractionHandler, lifespan, itemsPerPage, dismissible, buttonsOwnerOnly, messageGetter, buttonFactory, buttonOrder);
    }
}
