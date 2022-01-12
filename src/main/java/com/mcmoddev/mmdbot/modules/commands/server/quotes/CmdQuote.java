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
package com.mcmoddev.mmdbot.modules.commands.server.quotes;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.modules.commands.general.PaginatedCommand;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.quotes.Quote;
import com.mcmoddev.mmdbot.utilities.quotes.QuoteList;
import com.mcmoddev.mmdbot.utilities.quotes.StringQuote;
import com.mcmoddev.mmdbot.utilities.quotes.UserReference;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Handles all of the quote related commands.
 * Contains four subcommands;
 * - Add
 * - Remove
 * - Get
 * - List
 * <p>
 * Each is documented more thoroughly in the appropriate subclass.
 * <p>
 * TODO: Database migration in progress.
 * This will change a lot.
 *
 * @author Curle
 */
public class CmdQuote extends SlashCommand {

    public CmdQuote() {
        name = "quote";
        help = "Manage or view quotes.";

        guildOnly = true;

        children = new SlashCommand[]{
            new AddQuote(),
            new GetQuote(),
            new RemoveQuote(),
            new ListQuotes()
        };
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
    }

    /**
     * Add a quote to the list.
     * <p>
     * Possible forms:
     * /quote add I said something funny @Curle
     * /quote add I said something funny 462617385157787648
     * /quote add I said something funny
     * /quote add I said something funny The best bot developer
     * <p>
     * Can be used by anyone.
     * <p>
     * TODO: Prepare for more Quote implementations.
     *
     * @author Curle
     */
    private static final class AddQuote extends SlashCommand {

        /**
         * Create the command.
         * Sets all the usual flags.
         */
        public AddQuote() {
            super();
            name = "add";
            help = "Adds a new Quote to the list.";
            category = new Category("Fun");
            arguments = "<the text of the thing being quoted> <authorID/mention>";
            aliases = new String[]{"add-quote", "quoteadd", "quote-add"};
            guildOnly = true;

            options = List.of(
                new OptionData(OptionType.STRING, "quote", "The text of the quote.").setRequired(true),
                new OptionData(OptionType.USER, "quotee", "The person being quoted. Mutually exclusive with quoteetext.").setRequired(false),
                new OptionData(OptionType.STRING, "quoteetext", "The thing being quoted. Mutually exclusive with quotee.").setRequired(false)
            );
        }


        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!Utils.checkCommand(this, event))
                return;

            String text = event.getOption("quote").getAsString();
            OptionMapping quoteeUser = event.getOption("quotee");
            OptionMapping quoteeText = event.getOption("quoteetext");

            // Verify that there's a message being quoted.
            if (quoteeText != null && quoteeUser != null) {
                event.reply("Cannot add a quote with a quoted user and quoted text. Choose one.").queue();
                return;
            }


            Quote finishedQuote;
            // Fetch the user who created the quote.
            UserReference author = new UserReference(event.getUser().getIdLong());

            // Check if there's any attribution
            if (quoteeUser == null && quoteeText == null) {
                // Anonymous quote.
                var quotee = new UserReference();
                finishedQuote = new StringQuote(quotee, text, author);
            } else {
                if (quoteeUser != null) {
                    var id = quoteeUser.getAsUser().getIdLong();
                    var user = new UserReference(id);
                    finishedQuote = new StringQuote(user, text, author);
                } else {
                    // No user ID. Must be a string assignment.
                    var user = new UserReference(quoteeText.getAsString());
                    finishedQuote = new StringQuote(user, text, author);
                }
            }

            var quoteID = QuoteList.getQuoteSlot();
            finishedQuote.setID(quoteID);

            // All execution leads to here, where finishedQuote is valid.
            QuoteList.addQuote(finishedQuote);

            event.reply("Added quote " + quoteID + "!").mentionRepliedUser(false).queue();
        }

    }

    /**
     * Get a quote from the list.
     * Allows an int argument, otherwise random is chosen.
     * <p>
     * Possible forms:
     * /quote get
     * /quote get 111
     * <p>
     * Can be used by anyone.
     * <p>
     * TODO: Prepare for more Quote implementations.
     *
     * @author Curle
     */
    public static final class GetQuote extends SlashCommand {

        /**
         * Create the command.
         * Sets all the usual flags.
         */
        public GetQuote() {
            super();
            name = "get";
            help = "Get a quote. Specify a number if you like, otherwise a random is chosen.";
            category = new Category("Fun");
            arguments = "[quote number]";
            guildOnly = true;

            options = Collections.singletonList(new OptionData(OptionType.INTEGER, "index", "The index of the quote to fetch.").setRequired(false));
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!Utils.checkCommand(this, event)) {
                return;
            }

            OptionMapping index = event.getOption("index");

            // Check whether any parameters given.
            if (index != null) {
                // We have something to parse.
                if (index.getAsLong() >= QuoteList.getQuoteSlot()) {
                    event.replyEmbeds(QuoteList.getQuoteNotPresent()).mentionRepliedUser(false).queue();
                    return;
                }

                var fetched = QuoteList.getQuote((int) index.getAsLong());
                // Check if the quote exists.
                if (fetched == QuoteList.NULL) {
                    // Send the standard message
                    event.replyEmbeds(QuoteList.getQuoteNotPresent()).mentionRepliedUser(false).queue();
                    return;
                }

                // It exists, so get the content and send it.
                assert fetched != null;
                event.replyEmbeds(fetched.getQuoteMessage()).mentionRepliedUser(false).queue();
                return;
            }

            Quote fetched;
            Random rand = new Random();
            do {
                int id = rand.nextInt(QuoteList.getQuoteSlot());
                fetched = QuoteList.getQuote(id);
            } while (fetched == null);

            // It exists, so get the content and send it.
            event.replyEmbeds(fetched.getQuoteMessage()).mentionRepliedUser(false).queue();
        }
    }


    /**
     * Remove a quote from the list.
     * Requires an int argument.
     * <p>
     * Possible forms:
     * <p>
     * /quote remove 5
     * Can only be used by Bot Maintainers and higher.
     *
     * @author Curle
     */
    public final class RemoveQuote extends SlashCommand {

        /**
         * Create the command.
         * Sets all the usual flags.
         */
        public RemoveQuote() {
            super();
            name = "remove";
            help = "Remove a quote from the list.";
            arguments = "<the quotes numerical ID>";
            requiredRole = "bot maintainer";
            guildOnly = true;

            options = Collections.singletonList(new OptionData(OptionType.INTEGER, "index", "The index of the quote to delete.").setRequired(true));
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!Utils.checkCommand(this, event)) {
                return;
            }

            QuoteList.removeQuote((int) event.getOption("index").getAsLong());
            event.reply("Quote " + event.getOption("index").getAsLong() + " removed.").mentionRepliedUser(false).setEphemeral(true).queue();
        }
    }


    /**
     * Retrieve all quotes from the database.
     * Uses pagination and Interaction buttons, and thus no longer provides the integer argument.
     * <p>
     * Possible forms:
     * <p>
     * /quote list
     * Can be used by anyone.
     *
     * @author Curle
     */
    public class ListQuotes extends PaginatedCommand {
        private static QuoteListener listener;

        /**
         * Create the command.
         * Sets all the usual flags.
         */
        public ListQuotes() {
            super("list", "Get all quotes.", true, new ArrayList<>(), 10);
            category = new Category("Fun");
            guildOnly = true;

            listener = new QuoteListener();
        }

        public static QuoteListener getListener() {
            return listener;
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!Utils.checkCommand(this, event)) {
                return;
            }

            updateMaximum(QuoteList.getQuoteSlot() - 1);
            sendPaginatedMessage(event);
        }

        /**
         * Gather a page of quotes, as an embed.
         *
         * @param start The quote number at the start of the current page.
         * @return An EmbedBuilder which is ready to be sent.
         */
        @Override
        protected EmbedBuilder getEmbed(int start) {
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
                    .setTitle("Quote Page " + ((start / items_per_page) + 1))
                    .setTimestamp(Instant.now());
            }

            // From the specified starting point until the end of the page.
            for (int x = start; x < start + items_per_page; x++) {
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

        public class QuoteListener extends PaginatedCommand.ButtonListener {
            @Override
            public String getButtonID() {
                return "list";
            }
        }
    }
}
