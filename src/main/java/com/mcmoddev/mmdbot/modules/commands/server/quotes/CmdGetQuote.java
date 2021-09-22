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

import java.util.Random;

/**
 * Get a quote from the list.
 * Allows an int argument, otherwise random is chosen.
 * <p>
 * Possible forms:
 * !quote
 * !quote 111
 * <p>
 * Can be used by anyone.
 * <p>
 * TODO: Prepare for more Quote implementations.
 *
 * @author Curle
 */
public final class CmdGetQuote extends Command {

    /**
     * Create the command.
     * Sets all the usual flags.
     */
    public CmdGetQuote() {
        super();
        name = "getquote";
        help = "Get a quote. Specify a number if you like, otherwise a random is chosen.";
        category = new Category("Fun");
        arguments = "[quote number]";
        guildOnly = true;
        aliases = new String[]{"quote", "get-quote", "quoteget", "viewquote"};
    }

    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        final var channel = event.getMessage();
        var argsFull = event.getArgs();
        // If there are no quotes, exit early.
        if (QuoteList.getQuoteSlot() == 0) {
            channel.replyEmbeds(QuoteList.getQuoteNotPresent()).mentionRepliedUser(false).queue();
            return;
        }

        // Check whether any parameters given.
        if (argsFull.length() > 0) {
            // We have something to parse.
            try {
                int index = Integer.parseInt(argsFull.trim());
                if (index >= QuoteList.getQuoteSlot()) {
                    channel.replyEmbeds(QuoteList.getQuoteNotPresent()).mentionRepliedUser(false).queue();
                    return;
                }

                var fetched = QuoteList.getQuote(index);
                // Check if the quote exists.
                if (fetched == QuoteList.NULL) {
                    // Send the standard message
                    channel.replyEmbeds(QuoteList.getQuoteNotPresent()).mentionRepliedUser(false).queue();
                    return;
                }

                // It exists, so get the content and send it.
                assert fetched != null;
                channel.replyEmbeds(fetched.getQuoteMessage()).mentionRepliedUser(false).queue();
                return;
            } catch (NumberFormatException ignored) {
                // Fall through to the code below. No number found, so pick a random one.
            }
        }

        Quote fetched = null;
        Random rand = new Random();
        do {
            int index = rand.nextInt(QuoteList.getQuoteSlot());
            fetched = QuoteList.getQuote(index);
        } while (fetched == null);

        // It exists, so get the content and send it.
        channel.replyEmbeds(fetched.getQuoteMessage()).mentionRepliedUser(false).queue();
    }
}
