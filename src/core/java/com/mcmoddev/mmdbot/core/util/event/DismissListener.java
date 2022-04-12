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
package com.mcmoddev.mmdbot.core.util.event;

import io.github.matyrobbrt.eventdispatcher.LazySupplier;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.function.Supplier;

/**
 * A listener for dismissing.
 *
 * @author matyrobbrt
 */
public final class DismissListener extends ListenerAdapter {

    public static final ButtonStyle BUTTON_STYLE = ButtonStyle.SECONDARY;
    public static final String LABEL = "\uD83D\uDEAE️ Dismiss";

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

        switch (idParts.length) {
            // dismiss
            case 1 -> {
                if (event.getMessage().getInteraction() != null) {
                    final var owner = event.getMessage().getInteraction().getUser();
                    deleteIf(owner.getId(), event).queue();
                }
            }
            // dismiss-userId
            case 2 -> deleteIf(idParts[1], event).queue();
            // dismiss-userId-commandMessageId
            case 3 -> deleteIf(idParts[1], event)
                .and(event.getChannel().retrieveMessageById(idParts[2])
                    .flatMap(m -> m.delete().reason("User dismissed the command"))
                    .addCheck(() -> canDelete(idParts[1], event))
                )
                .queue($ -> {}, e -> {});
        }
    }

    private static RestAction<?> deleteIf(final String targetId, final ButtonInteractionEvent event) {
        if (canDelete(targetId, event)) {
            return event.getMessage().delete().reason("User dismissed the message");
        } else {
            return event.deferEdit();
        }
    }

    private static boolean canDelete(final String targetId, final ButtonInteractionEvent event) {
        return targetId.equals(event.getUser().getId()) && !event.getMessage().isEphemeral();
    }

    public static final Supplier<Button> NO_OWNER_FACTORY = LazySupplier.of(() -> Button.of(BUTTON_STYLE, "dismiss", LABEL));

    /**
     * Creates a dismission {@link Button} which <strong>only</strong> works
     * for interactions, and whose owner is the user who triggered the interaction.
     *
     * @return the button.
     */
    public static Button createDismissButton() {
        return NO_OWNER_FACTORY.get();
    }

    public static Button createDismissButton(final long buttonOwner, final ButtonStyle style, final String label) {
        return Button.of(style, "dismiss-" + buttonOwner, LABEL);
    }

    public static Button createDismissButton(final long buttonOwner) {
        return createDismissButton(buttonOwner, BUTTON_STYLE, LABEL);
    }

    public static Button createDismissButton(final String buttonOwner) {
        return Button.of(BUTTON_STYLE, "dismiss-" + buttonOwner, LABEL);
    }

    /**
     * Creates a dismiss button which will also delete the message that invoked the command.
     *
     * @param buttonOwner      the owner of the button
     * @param commandMessageId the message that invoked the command
     * @return the button
     */
    public static Button createDismissButton(final long buttonOwner, final long commandMessageId) {
        return Button.of(BUTTON_STYLE, "dismiss-" + buttonOwner + "-" + commandMessageId, LABEL);
    }

    public static Button createDismissButton(final User buttonOwner, final Message commandMessage) {
        return createDismissButton(buttonOwner.getIdLong(), commandMessage.getIdLong());
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
