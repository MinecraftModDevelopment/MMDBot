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

import com.mcmoddev.mmdbot.utilities.scripting.ExposeScripting;
import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * The interface which all Quote types implement.
 * Provides an interface for retrieving quote metadata.
 * <p>
 * Quote metadata consists of:
 * - Quotee; One of:
 * - Discord User ID
 * - String (for users not in MMD, or for external sources)
 * - Author:
 * - Discord User ID
 * - Data; Serialized instance of a Quote
 * - ID; An integer representing the place this quote holds in the list.
 * Allows an arbitrary amount of "types" of quotes.
 *
 * @author Curle
 */
public interface IQuote {

    /**
     * @return The person or object being quoted.
     */
    @ExposeScripting
    UserReference getQuotee();

    /**
     * Set a new quotee.
     *
     * @param quotee The person or object being quoted.
     */
    void setQuotee(UserReference quotee);

    /**
     * @return The person or object that created this quote.
     */
    @ExposeScripting
    UserReference getQuoteAuthor();

    /**
     * Set a new quote author.
     *
     * @param author The person or object that created this quote.
     */
    void setQuoteAuthor(UserReference author);

    /**
     * @return The integer ID of this quote.
     */
    @ExposeScripting
    int getID();

    /**
     * Set a new ID.
     *
     * @param id The integer ID of this quote.
     */
    void setID(int id);

    /**
     * Turn this quote's data into an Embed to be sent in a channel.
     *
     * @return A fully formed Embed ready for sending.
     */
    MessageEmbed getQuoteMessage();

    /**
     * Get the closest text representation of the thing being quoted.
     *
     * @return Depends on the subclass.
     */
    @ExposeScripting
    String getQuoteText();

}
