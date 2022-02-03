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
package com.mcmoddev.mmdbot.modules.commands.community.contextmenu.message;

import com.jagrosh.jdautilities.command.MessageContextMenu;
import com.jagrosh.jdautilities.command.MessageContextMenuEvent;
import com.mcmoddev.mmdbot.modules.commands.community.contextmenu.GuildOnlyMenu;
import com.mcmoddev.mmdbot.utilities.quotes.Quote;
import com.mcmoddev.mmdbot.utilities.quotes.QuoteList;
import com.mcmoddev.mmdbot.utilities.quotes.StringQuote;
import com.mcmoddev.mmdbot.utilities.quotes.UserReference;
import net.dv8tion.jda.api.EmbedBuilder;

public class ContextMenuAddQuote extends MessageContextMenu implements GuildOnlyMenu {

    public ContextMenuAddQuote() {
        name = "Add Quote";
    }

    @Override
    protected void execute(final MessageContextMenuEvent event) {
        String text = event.getTarget().getContentRaw();

        // Fetch the user who created the quote.
        UserReference author = new UserReference(event.getUser().getIdLong());
        // Fetch the user who owns the message quoted.
        UserReference quotee = new UserReference(event.getTarget().getAuthor().getIdLong());
        Quote finishedQuote = new StringQuote(quotee, text, author);

        var quoteID = QuoteList.getQuoteSlot();
        finishedQuote.setID(quoteID);

        // All execution leads to here, where finishedQuote is valid.
        QuoteList.addQuote(finishedQuote);

        event.replyEmbeds(new EmbedBuilder(finishedQuote.getQuoteMessage()).setTitle("Added quote " + quoteID).build()).mentionRepliedUser(false).queue();
    }

}
