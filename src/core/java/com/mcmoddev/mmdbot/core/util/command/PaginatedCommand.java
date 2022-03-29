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
package com.mcmoddev.mmdbot.core.util.command;

import com.google.common.collect.Lists;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.core.commands.component.ButtonInteractionContext;
import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.component.ComponentListener;
import com.mcmoddev.mmdbot.core.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * A wrapper for Slash Commands which require a paginated embed.
 * It handles the buttons and interactions for you.
 * <p>
 * To use this, the developer needs to:
 * - implement {@link #getEmbed(int, int, List)} as per the javadoc.
 * - make sure that the {@link #getListener()} is registered to a {@link com.mcmoddev.mmdbot.core.commands.component.ComponentManager}.
 * - call {@link #sendPaginatedMessage(SlashCommandEvent, int, String...)} in the execute method when a paginated embed is wanted.
 * <p>
 *
 * @author Curle
 * @author matyrobbrt
 */
public abstract class PaginatedCommand extends SlashCommand {
    public static final String FORWARD_BUTTON_ID = "next";
    public static final String BACKWARD_BUTTON_ID = "prev";

    protected final ComponentListener componentListener;
    protected final Component.Lifespan lifespan;
    protected final boolean ownerOnlyButton;
    // How many items should be sent per individual page.
    protected final int itemsPerPage;

    public PaginatedCommand(ComponentListener.Builder componentListenerBuilder, final Component.Lifespan lifespan, int itemsPerPage, boolean ownerOnlyButton) {
        super();
        this.componentListener = componentListenerBuilder
            .onButtonInteraction(this::onButtonInteraction)
            .build();
        this.lifespan = lifespan;
        this.ownerOnlyButton = ownerOnlyButton;
        this.itemsPerPage = itemsPerPage;
    }

    public PaginatedCommand(ComponentListener.Builder componentListenerBuilder, final Component.Lifespan lifespan, int itemsPerPage) {
        this(componentListenerBuilder, lifespan, itemsPerPage, false);
    }

    /**
     * Gets the component listener for handling the pagination. <br>
     * <strong>The listener has to be registered to a {@link com.mcmoddev.mmdbot.core.commands.component.ComponentManager}
     * before the command can be used.</strong>
     *
     * @return the component listener.
     */
    public final ComponentListener getListener() {
        return componentListener;
    }

    /**
     * Given the index of the start of the embed, get the next ITEMS_PER_PAGE items.
     * This is where the implementation of the paginated command steps in.
     *
     * @param startingIndex the index of the first item in the list.
     * @param maximum the maximum amount of items to be displayed
     * @param arguments the arguments of the button
     * @return an unbuilt embed that can be sent.
     */
    protected abstract EmbedBuilder getEmbed(int startingIndex, int maximum, final List<String> arguments);

    /**
     * Create and queue a {@link ReplyCallbackAction} which, if the number of items requires, also contains buttons for scrolling.
     *
     * @param event the active SlashCommandEvent.
     * @param maximum the maximum amount of items
     * @param arguments the arguments that will be saved in the database, bound to the button's component ID
     */
    protected void sendPaginatedMessage(SlashCommandEvent event, final int maximum, final String... arguments) {
        createPaginatedMessage(event, maximum, arguments).queue();
    }

    /**
     * Create a {@link ReplyCallbackAction} with the starting index of 0, which, if the number of items requires, also contains buttons for scrolling.
     *
     * @param event the active SlashCommandEvent.
     * @param maximum the maximum amount of items
     * @param args the arguments that will be saved in the database, bound to the button's component ID
     * @return the ReplyAction
     */
    protected ReplyCallbackAction createPaginatedMessage(SlashCommandEvent event, final int maximum, final String... args) {
        return createPaginatedMessage(event, 0, maximum, args);
    }

    /**
     * Create a {@link ReplyCallbackAction} which, if the number of items requires, also contains buttons for scrolling.
     *
     * @param event the active SlashCommandEvent.
     * @param startingIndex the index of the first item to display
     * @param maximum the maximum of items
     * @param args arguments the arguments that will be saved in the database, bound to the button's component ID
     * @return the ReplyAction
     */
    protected ReplyCallbackAction createPaginatedMessage(SlashCommandEvent event, final int startingIndex, final int maximum, final String... args) {
        final var id = UUID.randomUUID();
        final var argsList = Lists.newArrayList(args);
        var reply = event.deferReply().addEmbeds(getEmbed(startingIndex, maximum, argsList).build());
        final var startStr = String.valueOf(startingIndex);
        final var maxStr = String.valueOf(maximum);
        if (argsList.size() == 0) {
            argsList.add(startStr);
            argsList.add(maxStr);
        } else {
            argsList.add(0, startStr);
            argsList.add(1, maxStr);
        }
        final var component = new Component(componentListener.getName(), id, argsList, Component.Lifespan.TEMPORARY);
        componentListener.insertComponent(component);
        var buttons = createScrollButtons(id.toString(), startingIndex, maximum);
        if (buttons.length > 0) {
            reply = reply.addActionRow(buttons);
        }
        return reply;
    }

    private static final Emoji NEXT_EMOJI = Emoji.fromUnicode("▶️");
    private static final Emoji PREVIOUS_EMOJI = Emoji.fromUnicode("◀️");

    /**
     * Create the row of Component interaction buttons.
     * <p>
     * Currently, this just creates a left and right arrow.
     * Left arrow scrolls back a page. Right arrow scrolls forward a page.
     *
     * @param id the component ID of the buttons
     * @param start the index of the item at the start of the current page.
     * @param maximum the maximum amount of items
     * @return A row of buttons to go back and forth by one page.
     */
    private ItemComponent[] createScrollButtons(String id, int start, int maximum) {
        Button backward = Button.primary(Component.createIdWithArguments(id, BACKWARD_BUTTON_ID), PREVIOUS_EMOJI).asDisabled();
        Button forward = Button.primary(Component.createIdWithArguments(id, FORWARD_BUTTON_ID), NEXT_EMOJI).asDisabled();

        if (start != 0) {
            backward = backward.asEnabled();
        }

        if (start + itemsPerPage < maximum) {
            forward = forward.asEnabled();
        }

        return new ItemComponent[]{
            backward, forward
        };
    }

    protected void onButtonInteraction(final ButtonInteractionContext context) {
        final var event = context.getEvent();
        final var interaction = event.getMessage().getInteraction();
        if (ownerOnlyButton && interaction != null && event.getUser().getIdLong() != interaction.getUser().getIdLong()) {
            event.deferReply(true).setContent("You are not the author of this interaction!").queue();
            return;
        }
        final int current = context.getArgument(0, () -> 0, Integer::parseInt);
        final int maximum = context.getArgument(1, () -> 0, Integer::parseInt);
        final List<String> newArgs = context.getArguments().size() == 2 ? List.of() : context.getArguments().subList(2, context.getArguments().size());

        // If it has action rows already, don't delete them
        final var oldActionRowsSize = event.getMessage().getActionRows().size();
        final var oldActionRows = oldActionRowsSize < 2 ? new ArrayList<ActionRow>() :
            new ArrayList<>(event.getMessage().getActionRows().subList(1, oldActionRowsSize));

        final var buttonId = context.getComponentId().toString();

        switch (context.getItemComponentArguments().get(0)) {
            case FORWARD_BUTTON_ID -> {
                final var start = current + itemsPerPage;

                oldActionRows.add(0, ActionRow.of(createScrollButtons(buttonId,start, maximum)));
                event.editMessageEmbeds(getEmbed(start, maximum, newArgs).build())
                    .setActionRows(oldActionRows)
                    .queue();

                // Argument 0 == current index
                context.updateArgument(0, String.valueOf(start));
            }
            case BACKWARD_BUTTON_ID -> {
                final var start = current - itemsPerPage;

                oldActionRows.add(0, ActionRow.of(createScrollButtons(buttonId, start, maximum)));
                event.editMessageEmbeds(getEmbed(start, maximum, newArgs).build())
                    .setActionRows(oldActionRows)
                    .queue();

                // Argument 0 == current index
                context.updateArgument(0, String.valueOf(start));
            }
        }
    }

}
