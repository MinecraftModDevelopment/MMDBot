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
package com.mcmoddev.mmdbot.core.util.event;

import io.github.matyrobbrt.eventdispatcher.LazySupplier;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.function.Supplier;

/**
 * A listener for dismissing.
 *
 * @author matyrobbrt
 */
public final class DismissListener extends ListenerAdapter {

    public static final Supplier<Button> BUTTON_FACTORY = LazySupplier.of(() -> Button.primary("dismiss", "✖ Dismiss"));

    @Override
    public void onButtonInteraction(@javax.annotation.Nonnull final ButtonInteractionEvent event) {
        var button = event.getButton();
        if (button.getId() == null || button.getId().isBlank()) {
            return;
        }

        String[] idParts = button.getId().split("-");

        if (!idParts[0].equals("dismiss")) {
            return;
        }

        if (idParts.length < 2) {
            if (event.getMessage().getInteraction() != null) {
                final var owner = event.getMessage().getInteraction().getUser();
                deleteIf(owner.getIdLong(), event);
            }
        } else {
            final long buttonOwner = Long.parseLong(idParts[1]);
            deleteIf(buttonOwner, event);
        }
    }

    private static void deleteIf(final long targetId, final ButtonInteractionEvent event) {
        if (targetId == event.getUser().getIdLong() && !event.getMessage().isEphemeral()) {
            event.getMessage().delete().reason("User dismissed the message").queue();
        } else {
            event.deferEdit().queue();
        }
    }

    /**
     * Creates a dismission {@link Button} which <strong>only</strong> works
     * for interactions, and whose owner is the user who triggered the interaction.
     *
     * @return the button.
     */
    public static Button createDismissButton() {
        return BUTTON_FACTORY.get();
    }

    public static Button createDismissButton(final long buttonOwner) {
        final var btn = BUTTON_FACTORY.get();
        return btn.withId("dismiss-" + buttonOwner);
    }

    public static Button createDismissButton(final Member buttonOwner) {
        return createDismissButton(buttonOwner.getIdLong());
    }

    public static Button createDismissButton(final User buttonOwner) {
        return createDismissButton(buttonOwner.getIdLong());
    }

    public static Button createDismissButton(final Interaction interaction) {
        return createDismissButton(interaction.getUser());
    }
}
