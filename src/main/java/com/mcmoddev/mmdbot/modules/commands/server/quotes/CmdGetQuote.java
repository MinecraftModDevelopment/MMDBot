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
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Random;

/**
 * Get a quote from the list.
 * Allows an int argument, otherwise random is chosen.
 *
 * Possible forms:
 * !quote
 * !quote 111
 * Can be used by anyone.
 *
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
        aliases = new String[]{"quote", "get-quote", "quoteget", "viewquote"};
        help = "Get a quote. Specify a number if you like, otherwise a random is chosen.";
    }

    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event))
            return;

        final TextChannel channel = event.getTextChannel();
        String argsFull = event.getArgs();

        // Check whether any parameters given.
        if (argsFull.length() > 0) {
            // We have something to parse.
            try {
                int index = Integer.parseInt(argsFull.trim());
                if (index >= QuoteList.getQuoteSlot()) {
                    channel.sendMessageEmbeds(QuoteList.getQuoteNotPresent()).queue();
                    return;
                }

                Quote fetched = QuoteList.getQuote(index);

                // Check if the quote exists.
                if (fetched == QuoteList.NULL) {
                    // Send the standard message
                    channel.sendMessageEmbeds(QuoteList.getQuoteNotPresent()).queue();
                    return;
                }

                // It exists, so get the content and send it.
                assert fetched != null;
                channel.sendMessageEmbeds(fetched.getQuoteMessage()).queue();
                return;
            } catch (NumberFormatException ignored) {
                // Fall through to the code below. No number found, so pick a random one.
            }
        }

        Quote fetched = null;
        // Find a valid quote.
        while (fetched == null) {
            // Get a random quote.
            int limit = QuoteList.getQuoteSlot();
            int index = new Random().nextInt(limit);
            // If the next quote is null, we loop back and try again.
            fetched = QuoteList.getQuote(index);
        }

        // It exists, so get the content and send it.
        channel.sendMessageEmbeds(fetched.getQuoteMessage()).queue();

    }
}
