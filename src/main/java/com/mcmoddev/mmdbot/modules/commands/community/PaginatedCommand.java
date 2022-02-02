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
package com.mcmoddev.mmdbot.modules.commands.community;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A wrapper for Slash Commands which require a paginated embed.
 * It handles the buttons and interactions for you.
 * <p>
 * To use this, the developer needs to:
 * - implement {@link #getEmbed(int)} as per the javadoc.
 * - implement {@link ButtonListener} in the child class, along with the {@link ButtonListener#getButtonID()} method.
 * - register their implementation of {@link ButtonListener} to the {@link com.jagrosh.jdautilities.command.CommandClient}.
 * - call {@link #updateMaximum(int)} as required - usually once per invocation
 * - call {@link #sendPaginatedMessage(SlashCommandEvent)} in the execute method when a paginated embed is wanted.
 *
 * @author Curle
 */
public abstract class PaginatedCommand extends SlashCommand {

    // How many items should be sent per individual page. Defaults to the maximum field count for an Embed, 25.
    protected int items_per_page = 25;
    // The maximum number of items in the list. Update with #updateMaximum
    protected int maximum = 0;
    protected PaginatedCommand.ButtonListener listener = new ButtonListener();

    public PaginatedCommand(String name, String help, boolean guildOnly, List<OptionData> options, int items) {
        super();
        this.name = name;
        this.help = help;

        this.guildOnly = guildOnly;

        items_per_page = items;

        this.options = options;
    }

    /**
     * Given the index of the start of the embed, get the next ITEMS_PER_PAGE items.
     * This is where the implementation of the paginated command steps in.
     *
     * @param startingIndex the index of the first item in the list.
     * @return an unbuilt embed that can be sent.
     */
    protected abstract EmbedBuilder getEmbed(int startingIndex);

    /**
     * Set a new maximum index into the paginated list.
     * Updates the point at which buttons are created in new queries.
     */
    protected void updateMaximum(int newMaxmimum) {
        maximum = newMaxmimum;
    }

    /**
     * Create and queue a ReplyAction which, if the number of items requires, also contains buttons for scrolling.
     *
     * @param event the active SlashCommandEvent.
     */
    protected void sendPaginatedMessage(SlashCommandEvent event) {
        var reply = event.replyEmbeds(getEmbed(0).build());
        var buttons = createScrollButtons(0);
        if (buttons.length > 0) {
            reply.addActionRow(buttons);
        }
        reply.queue();
    }

    /**
     * Create the row of Component interaction buttons.
     * <p>
     * Currently, this just creates a left and right arrow.
     * Left arrow scrolls back a page. Right arrow scrolls forward a page.
     *
     * @param start The quote number at the start of the current page.
     * @return A row of buttons to go back and forth by one page.
     */
    private ItemComponent[] createScrollButtons(int start) {
        Button backward = Button.primary(listener.getButtonID() + "-" + start + "-prev",
            Emoji.fromUnicode("◀️")).asDisabled();
        Button forward = Button.primary(listener.getButtonID() + "-" + start + "-next",
            Emoji.fromUnicode("▶️")).asDisabled();

        if (start != 0) {
            backward = backward.asEnabled();
        }

        if (start + items_per_page < maximum) {
            forward = forward.asEnabled();
        }

        return new ItemComponent[]{backward, forward};
    }


    /**
     * Listens for interactions with the scroll buttons on the paginated message.
     * Extend and implement as a child class of the Paginated Message.
     * <p>
     * Implement the {@link #getButtonID()} function in any way you like.
     * Make sure that this listener is registered to the {@link com.jagrosh.jdautilities.command.CommandClient}.
     */
    public class ButtonListener extends ListenerAdapter {

        public String getButtonID() {
            return PaginatedCommand.this.getName();
        }

        @Override
        public void onButtonInteraction(@NotNull final ButtonInteractionEvent event) {
            var button = event.getButton();
            if (button.getId() == null) {
                return;
            }

            String[] idParts = button.getId().split("-");
            if (idParts.length != 3) {
                return;
            }

            if (!idParts[0].equals(getButtonID())) {
                return;
            }

            int current = Integer.parseInt(idParts[1]);

            if (idParts[2].equals("next")) {
                event
                    .editMessageEmbeds(getEmbed(current + items_per_page).build())
                    .setActionRow(createScrollButtons(current + items_per_page))
                    .queue();
            } else {
                if (idParts[2].equals("prev")) {
                    event
                        .editMessageEmbeds(getEmbed(current - items_per_page).build())
                        .setActionRow(createScrollButtons(current - items_per_page))
                        .queue();
                }
            }
        }
    }
}
