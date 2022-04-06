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
package com.mcmoddev.mmdbot.core.commands.component.context;

import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.component.ComponentManager;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.Interaction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Base class for interaction contexts.
 */
public interface BaseItemComponentInteractionContext<T extends Interaction> {

    /**
     * Gets the event that triggered the interaction.
     *
     * @return the event that triggered the interaction
     */
    @NonNull
    T getEvent();

    /**
     * The {@link Guild} this interaction happened in.
     * <br>This is null in direct messages.
     *
     * @return The {@link Guild} or null
     */
    @Nullable
    default Guild getGuild() {
        return getEvent().getGuild();
    }

    /**
     * The {@link Member} who caused this interaction.
     * <br>This is null if the interaction is not from a guild.
     *
     * @return The {@link Member}
     */
    @Nullable
    default Member getMember() {
        return getEvent().getMember();
    }

    /**
     * The {@link User} who caused this interaction.
     *
     * @return The {@link User}
     */
    @NonNull
    default User getUser() {
        return getEvent().getUser();
    }

    /**
     * Gets the component manager which dispatched this context.
     *
     * @return the component manager
     */
    @NonNull
    ComponentManager getManager();

    /**
     * Gets the arguments from the database.
     *
     * @return the arguments
     */
    @NonNull
    List<String> getArguments();

    /**
     * Gets an argument.
     *
     * @param index    the index of the argument to get
     * @param resolver a function that resolves the argument, if present
     * @param <Z>      the type of the resolved argument
     * @return if an argument with the {@code index} exists, that argument resolved, otherwise {@code null}
     */
    @Nullable
    default <Z> Z getArgument(final int index, final Function<? super String, ? extends Z> resolver) {
        if (index >= getArguments().size()) {
            return null;
        } else {
            return resolver.apply(getArguments().get(index));
        }
    }

    /**
     * Gets an argument.
     *
     * @param index        the index of the argument to get
     * @param defaultValue the default value of the argument
     * @param resolver     a function that resolves the argument, if present
     * @param <Z>          the type of the resolved argument
     * @return if an argument with the {@code index} exists, that argument resolved, otherwise the {@code defaultValue}
     */
    @NonNull
    default <Z> Z getArgument(final int index, final Supplier<Z> defaultValue, final Function<? super String, ? extends Z> resolver) {
        if (index >= getArguments().size()) {
            return defaultValue.get();
        } else {
            return resolver.apply(getArguments().get(index));
        }
    }

    /**
     * Gets the arguments from the {@link net.dv8tion.jda.api.interactions.components.ItemComponent}'s id. Those arguments are split from the component id
     * using the {@link Component#ID_SPLITTER}.
     *
     * @return the arguments from the item component id.
     */
    @NonNull
    List<String> getItemComponentArguments();

    /**
     * Gets the {@link Component#uuid() Component ID} of the button which was clicked.
     *
     * @return the Component ID of the clicked button.
     */
    @NonNull
    UUID getComponentId();

    /**
     * Updates the arguments of the {@link Component#uuid() Component ID} associated with the button
     * clicked.
     *
     * @param newArguments the new arguments
     */
    default void updateArguments(@NonNull final List<String> newArguments) {
        getManager().getStorage().updateArguments(getComponentId(), newArguments);
    }

    /**
     * Updates an argument of the {@link Component#uuid() Component ID} associated with the button
     * clicked.
     *
     * @param index    the index of the argument to update
     * @param argument the new argument
     */
    default void updateArgument(final int index, @NonNull final String argument) {
        final var list = new ArrayList<>(getArguments());
        list.set(index, argument);
        updateArguments(list);
    }

    /**
     * Deletes the {@link #getComponentId() Component ID} and all its associated
     * data from the database.
     */
    default void deleteComponent() {
        getManager().getStorage().removeComponent(getComponentId());
    }

}
