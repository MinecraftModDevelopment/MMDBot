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
package com.mcmoddev.mmdbot.commander.quotes;

import com.mcmoddev.mmdbot.core.annotation.ExposeScripting;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.time.Instant;

/**
 * A Quote implementation that encodes a String as the thing being quoted.
 * <p>
 * A StringQuote is serialized as such:
 * {
 * DATA
 * }
 *
 * @author Curle
 */
public final class StringQuote extends Quote {

    /**
     * The String that is the message or content being quoted.
     */
    private String data;

    /**
     * Set the String encoding the Quote data.
     *
     * @param pData The message or content being quoted.
     */
    private void setData(final String pData) {
        this.data = pData;
    }

    /**
     * @return The message or content being quoted.
     */
    private String getData() {
        return data;
    }

    @Override
    public MessageEmbed getQuoteMessage() {
        return new EmbedBuilder()
            .setTitle("Quote " + this.getID())
            .setColor(Color.GREEN)
            .addField("Content", this.getData(), false)
            .addField("Author", quotee.resolveReference(), false)
            .setFooter("Quoted by " + creator.resolveReference())
            .setTimestamp(Instant.now())
            .build();
    }

    @Override
    @ExposeScripting
    public String getQuoteText() {
        return data;
    }

    /**
     * Construct a new StringQuote with all the necessary data.
     *
     * @param quotee  A Reference to the User being Quoted.
     * @param quote   The message or content being Quoted.
     * @param creator A Reference to the User that created the Quote.
     */
    public StringQuote(final UserReference quotee, final String quote, final UserReference creator) {
        this.setQuotee(quotee);
        this.setData(quote);
        this.setQuoteAuthor(creator);
    }
}
