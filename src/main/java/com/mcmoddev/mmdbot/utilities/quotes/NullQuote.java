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
package com.mcmoddev.mmdbot.utilities.quotes;

import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * Used in place of null in the serialized list of quotes.
 *
 * @author Curle
 */
public final class NullQuote extends Quote {

    @Override
    public MessageEmbed getQuoteMessage() {
        return QuoteList.getQuoteNotPresent();
    }

    @Override
    public String getQuoteText() {
        return "Quote not found.";
    }

    /**
     * Create a new NullQuote.
     */
    public NullQuote() {
        this.setQuotee(null);
        this.setQuoteAuthor(null);
    }
}
