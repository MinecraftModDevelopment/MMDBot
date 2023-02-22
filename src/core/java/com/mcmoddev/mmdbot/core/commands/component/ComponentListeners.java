/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2023 <MMD - MinecraftModDevelopment>
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

import com.mcmoddev.mmdbot.core.commands.component.context.BaseItemComponentInteractionContext;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.Interaction;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@UtilityClass
public class ComponentListeners {
    public static <I extends GenericComponentInteractionCreateEvent, T extends BaseItemComponentInteractionContext<I>> Consumer<T> checkUser(Consumer<? super T> listener) {
        return context -> {
            if (context.getArgument(0, () -> 0L, Long::parseLong) == context.getUser().getIdLong()) {
                context.getArguments().remove(0);
                listener.accept(context);
            } else {
                context.getEvent().deferEdit().queue();
            }
        };
    }

    public static <I extends Interaction, T extends BaseItemComponentInteractionContext<I>> Consumer<T> multipleTypes(Map<String, Consumer<? super T>> listeners) {
        return context -> {
            final var listener = listeners.get(context.getArgument(0, () -> "", Function.identity()));
            if (listener != null) {
                context.getArguments().remove(0);
                listener.accept(context);
            }
        };
    }
}
