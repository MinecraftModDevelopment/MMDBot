/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
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
import com.mcmoddev.mmdbot.core.dfu.ExtendedCodec;
import com.mcmoddev.mmdbot.core.dfu.ExtendedDynamicOps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
     * The codec used for serializing this quote type.
     */
    public static final Codec<StringQuote> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UserReference.CODEC.fieldOf("quotee").forGetter(StringQuote::getQuotee),
        Codec.STRING.fieldOf("data").forGetter(StringQuote::getData),
        UserReference.CODEC.fieldOf("creator").forGetter(StringQuote::getQuoteAuthor),
        Codec.INT.optionalFieldOf("id", 0).forGetter(StringQuote::getID)
    ).apply(instance, StringQuote::new));

    public static final IQuote.QuoteType<StringQuote> TYPE = () -> CODEC;

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

    /**
     * Construct a new StringQuote with all the necessary data.
     *
     * @param quotee  A Reference to the User being Quoted.
     * @param quote   The message or content being Quoted.
     * @param creator A Reference to the User that created the Quote.
     * @param id      The ID of the quote
     */
    public StringQuote(final UserReference quotee, final String quote, final UserReference creator, final int id) {
        this.setQuotee(quotee);
        this.setData(quote);
        this.setQuoteAuthor(creator);
        this.setID(id);
    }

    @Override
    public QuoteType<?> getType() {
        return TYPE;
    }
}
