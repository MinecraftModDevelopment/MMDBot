/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2021 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.modules.commands.server.quotes;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.quotes.Quote;
import com.mcmoddev.mmdbot.utilities.quotes.QuoteList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Retrieve all quotes from the database.
 * Allows an int argument, otherwise starts from 0.
 * The int represents the first quote to start with, and will increase from there.
 *
 * Uses pagination and Interaction buttons.
 * Implementation shamelessly stolen from willbl
 *
 * Possible forms:
 *
 * !quotes
 * !quotes 155
 * Can be used by anyone.
 *
 * @author Curle
 */
public class CmdListQuotes extends Command {

    private static final int QUOTE_PER_PAGE = 10;

    /**
     * Create the command.
     * Sets all the usual flags.
     */
    public CmdListQuotes() {
        super();
        name = "listquotes";
        aliases = new String[]{"quotes", "list-quotes", "quoteslist"};
        help = "Get all quotes. Specify a starting number if you like, otherwise starts from 0.";
    }


    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        final TextChannel channel = event.getTextChannel();
        String argsFull = event.getArgs();
        String[] args = argsFull.split(" ");

        int start = 0;
        if (args.length > 1) {
            start = Integer.parseInt(args[0]);
        }

        MessageAction sentMessage = channel.sendMessageEmbeds(getQuotes(start).build());
        Component[] buttons = createButtons(start);
        if (buttons.length > 0) {
            sentMessage.setActionRow(buttons);
        }

        sentMessage.queue();
    }

    /**
     * Create the row of Component interaction buttons.
     * <p>
     * Currently, this just creates a left and right arrow.
     * Left arrow scrolls back a page. Right arrow scrolls forward a page.
     *
     * @param start The quote number at the start of the current page.
     * @return A row of buttons to go back and forth by one page in a quote list.
     */
    private static Component[] createButtons(int start) {
        List<Component> components = new ArrayList<>();
        if (start != 0) {
            components.add(Button.secondary(ButtonListener.BUTTON_ID_PREFIX + "-" + start + "-prev",
                Emoji.fromUnicode("◀️")));
        }
        if (start + QUOTE_PER_PAGE < QuoteList.getQuoteSlot()) {
            components.add(Button.primary(ButtonListener.BUTTON_ID_PREFIX + "-" + start + "-next",
                Emoji.fromUnicode("▶️")));
        }
        return components.toArray(new Component[0]);
    }

    /**
     * Gather a page of quotes, as an embed.
     *
     * @param start The quote number at the start of the current page.
     * @return An EmbedBuilder which is ready to be sent.
     */
    private static EmbedBuilder getQuotes(int start) {
        EmbedBuilder embed;
        // We have to make sure that this doesn't crash if we list a fresh bot.
        if (QuoteList.getQuoteSlot() == 0) {
            embed = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setDescription("There are no quotes loaded currently.")
                .setTimestamp(Instant.now());
        } else {
            embed = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Quote Page " + ((start / QUOTE_PER_PAGE) + 1))
                .setTimestamp(Instant.now());
        }

        // From the specified starting point until the end of the page.
        for (int x = start; x < start + QUOTE_PER_PAGE; x++) {
            // But stop early if we hit the end of the list,
            if (x >= QuoteList.getQuoteSlot()) {
                break;
            }

            // Get the current Quote
            Quote fetchedQuote = QuoteList.getQuote(x);

            // Put it in the description.
            // message - author
            embed.addField(String.valueOf(fetchedQuote.getID()), fetchedQuote == null ? "Quote does not exist." :
                fetchedQuote.getQuoteText() + " - " + fetchedQuote.getQuotee().resolveReference(), false);
        }

        return embed;

    }

    public static class ButtonListener extends ListenerAdapter {
        private static final String BUTTON_ID_PREFIX = "quotelist";

        @Override
        public void onButtonClick(@NotNull final ButtonClickEvent event) {
            var button = event.getButton();
            if (button == null || button.getId() == null) {
                return;
            }

            String[] idParts = button.getId().split("-");
            if (idParts.length != 3) {
                return;
            }

            if (!idParts[0].equals(BUTTON_ID_PREFIX)) {
                return;
            }

            int current = Integer.parseInt(idParts[1]);

            if (idParts[2].equals("next")) {
                event
                    .editMessageEmbeds(getQuotes(current + QUOTE_PER_PAGE).build())
                    .setActionRow(createButtons(current + QUOTE_PER_PAGE))
                    .queue();
            } else {
                if (idParts[2].equals("prev")) {
                    event
                        .editMessageEmbeds(getQuotes(current - QUOTE_PER_PAGE).build())
                        .setActionRow(createButtons(current - QUOTE_PER_PAGE))
                        .queue();
                }
            }
        }
    }
}
