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
import com.mcmoddev.mmdbot.utilities.quotes.StringQuote;
import com.mcmoddev.mmdbot.utilities.quotes.UserReference;

/**
 * Add a quote to the list.
 * <p>
 * Possible forms:
 * !addquote "I said something funny" - @Curle
 * !addquote "I said something funny" - 462617385157787648
 * !addquote "I said something funny"
 * !addquote "I said something funny - The best bot developer
 * <p>
 * Can be used by anyone.
 * <p>
 * TODO: Prepare for more Quote implementations.
 *
 * @author Curle
 */
public final class CmdAddQuote extends Command {

    /**
     * Create the command.
     * Sets all the usual flags.
     */
    public CmdAddQuote() {
        super();
        name = "addquote";
        help = "Adds a new Quote to the list.";
        category = new Category("Fun");
        arguments = "<the text of the thing being quoted> - <authorID/mention>";
        aliases = new String[]{"add-quote", "quoteadd", "quote-add"};
        guildOnly = true;
    }

    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event))
            return;

        final var channel = event.getMessage();
        var argsFull = event.getArgs();

        String[] args = argsFull.split("-");
        // args = [ "text", <@ID> ]

        // Verify that there were any arguments
        if (!(args.length > 0)) {
            channel.reply("Invalid arguments. See the help for this command.")
                .mentionRepliedUser(false).queue();
            return;
        }

        if (args.length > 2) {
            channel.reply("A quote cannot contain a dash ( - ). This is being worked on.")
                .mentionRepliedUser(false).queue();
            return;
        }

        // Verify that there's a message being quoted.
        if (!(args[0].charAt(0) == '\"') || !(args[0].charAt(args[0].length() - 2) == '\"')) {
            channel.reply("Invalid arguments. See the help for this command.")
                .mentionRepliedUser(false).queue();
            return;
        }

        // Remove the start and end quote.
        args[0] = args[0].trim();
        var quote = args[0].substring(1, args[0].length() - 1);

        Quote finishedQuote;
        // Fetch the user who created the quote
        var author = new UserReference(event.getAuthor().getIdLong());

        // Check if there's any attribution
        if (args.length == 1) {
            // Anonymous quote.
            var quotee = new UserReference();

            finishedQuote = new StringQuote(quotee, quote, author);
        } else {
            // args.length == 2 by logical deduction.

            var quotee = args[1].trim();

            final var mentionUsernameHeader = 3;
            final var mentionStandardHeader = 2;

            // Check if this is a mention
            if (quotee.charAt(0) == '<' && quotee.charAt(1) == '@')
                // Strip the tags from it
                quotee = quotee.substring(quotee.charAt(2) == '!' ? mentionUsernameHeader : mentionStandardHeader,
                    quotee.length() - 1);

            // Check if there's a user ID here
            try {
                var id = Long.parseLong(quotee);
                var quoteeUser = new UserReference(id);
                finishedQuote = new StringQuote(quoteeUser, quote, author);
            } catch (NumberFormatException exception) {
                // No user ID. Must be a string assignment.
                var quoteeUser = new UserReference(quotee);
                finishedQuote = new StringQuote(quoteeUser, quote, author);
            }
        }

        var quoteID = QuoteList.getQuoteSlot();
        finishedQuote.setID(quoteID);

        // All execution leads to here, where finishedQuote is valid.
        QuoteList.addQuote(finishedQuote);

        channel.reply("Added quote " + quoteID + "!").mentionRepliedUser(false).queue();
    }
}
