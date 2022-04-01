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
package com.mcmoddev.mmdbot.core.commands.paginate;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.component.ComponentListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.util.List;

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

    protected final Paginator paginator;

    public PaginatedCommand(final PaginatorBuilder paginator) {
        this.paginator = paginator
            .message((startingIndex, maximum, arguments1) -> new MessageBuilder().setEmbeds(getEmbed(startingIndex, maximum, arguments1).build()))
            .build();
    }

    public PaginatedCommand(ComponentListener.Builder componentListenerBuilder, final Component.Lifespan lifespan, int itemsPerPage, boolean ownerOnlyButton) {
        this(Paginator.builder(componentListenerBuilder)
            .buttonsOwnerOnly(ownerOnlyButton)
            .itemsPerPage(itemsPerPage)
            .lifespan(lifespan));
    }

    public PaginatedCommand(ComponentListener.Builder componentListenerBuilder, final Component.Lifespan lifespan, int itemsPerPage) {
        this(componentListenerBuilder, lifespan, itemsPerPage, false);
    }

    /**
     * Gets the paginator of the command.
     * @return the paginator
     */
    public final Paginator getPaginator() {
        return paginator;
    }

    /**
     * Gets the component listener for handling the pagination. <br>
     * <strong>The listener has to be registered to a {@link com.mcmoddev.mmdbot.core.commands.component.ComponentManager}
     * before the command can be used.</strong>
     *
     * @return the component listener.
     */
    public final ComponentListener getListener() {
        return paginator.getListener();
    }

    /**
     * Given the index of the start of the embed, get the next ITEMS_PER_PAGE items.
     * This is where the implementation of the paginated command steps in.
     *
     * @param startingIndex the index of the first item in the list.
     * @param maximum       the maximum amount of items to be displayed
     * @param arguments     the arguments of the button
     * @return an unbuilt embed that can be sent.
     */
    protected abstract EmbedBuilder getEmbed(int startingIndex, int maximum, final List<String> arguments);

    /**
     * Create and queue a {@link ReplyCallbackAction} which, if the number of items requires, also contains buttons for scrolling.
     *
     * @param event     the active SlashCommandEvent.
     * @param maximum   the maximum amount of items
     * @param arguments the arguments that will be saved in the database, bound to the button's component ID
     */
    protected void sendPaginatedMessage(SlashCommandEvent event, final int maximum, final String... arguments) {
        createPaginatedMessage(event, maximum, arguments).queue();
    }

    /**
     * Create and queue a {@link ReplyCallbackAction} which, if the number of items requires, also contains buttons for scrolling.
     *
     * @param event         the active SlashCommandEvent.
     * @param startingIndex the index of the first item to display
     * @param maximum       the maximum amount of items
     * @param arguments     the arguments that will be saved in the database, bound to the button's component ID
     */
    protected void sendPaginatedMessage(SlashCommandEvent event, final int startingIndex, final int maximum, final String... arguments) {
        createPaginatedMessage(event, startingIndex, maximum, arguments).queue();
    }

    /**
     * Create a {@link ReplyCallbackAction} with the starting index of 0, which, if the number of items requires, also contains buttons for scrolling.
     *
     * @param event   the active SlashCommandEvent.
     * @param maximum the maximum amount of items
     * @param args    the arguments that will be saved in the database, bound to the button's component ID
     * @return the ReplyAction
     */
    protected ReplyCallbackAction createPaginatedMessage(SlashCommandEvent event, final int maximum, final String... args) {
        return createPaginatedMessage(event, 0, maximum, args);
    }

    /**
     * Create a {@link ReplyCallbackAction} which, if the number of items requires, also contains buttons for scrolling.
     *
     * @param event         the active SlashCommandEvent.
     * @param startingIndex the index of the first item to display
     * @param maximum       the maximum of items
     * @param args          arguments the arguments that will be saved in the database, bound to the button's component ID
     * @return the ReplyAction
     */
    protected ReplyCallbackAction createPaginatedMessage(SlashCommandEvent event, final int startingIndex, final int maximum, final String... args) {
        final var message = paginator.createPaginatedMessage(startingIndex, maximum, event.getIdLong(), args);
        return event.deferReply()
            .setContent(message.getContentRaw())
            .addActionRows(message.getActionRows())
            .addEmbeds(message.getEmbeds());
    }

    protected int getItemsPerPage() {
        return paginator.getItemsPerPage();
    }
}
