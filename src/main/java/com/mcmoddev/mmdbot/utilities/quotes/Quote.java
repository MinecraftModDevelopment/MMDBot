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

/**
 * Handles the attribution aspect of a Quote.
 * <p>
 * Exists to be the superclass to implementations that hold data.
 * As such, it is abstract so that the data and thus getQuoteMessage() function may be undefined.
 * <p>
 * A quote is serialized as such:
 * <pre>{@code {
 *      "ID": DATA,
 *      "quotee": {
 *          "type": DATA,
 *          "data": DATA
 *      },
 *      "creator": {
 *          "type": DATA,
 *          "data": DATA
 *      },
 *      "content": {
 *          < HANDLED BY IMPLEMENTATION >
 *      }
 *  }}</pre>
 *
 * @author Curle
 */
public abstract class Quote implements IQuote {

    /**
     * The user or thing being quoted.
     */
    protected UserReference quotee;

    /**
     * The user or thing that created the quote.
     */
    protected UserReference creator;

    /**
     * The ID of this quote; representing where in the list it is.
     */
    protected int id;

    /**
     * Set the creator of this Quote.
     *
     * @param author A Reference to the User that created the Quote.
     */
    @Override
    public void setQuoteAuthor(final UserReference author) {
        this.creator = author;
    }

    /**
     * @return The person or object being quoted.
     */
    @Override
    public UserReference getQuoteAuthor() {
        return creator;
    }

    /**
     * Set the target of this Quote.
     *
     * @param target A Reference to the User being Quoted.
     */
    @Override
    public void setQuotee(final UserReference target) {
        this.quotee = target;
    }

    /**
     * @return The person or object that created this quote.
     */
    @Override
    public UserReference getQuotee() {
        return quotee;
    }

    /**
     * Set the ID of this Quote.
     *
     * @param pId The integer representing where in the list the Quote is.
     */
    @Override
    public void setID(final int pId) {
        this.id = pId;
    }

    /**
     * @return The integer representing where in the list the Quote is.
     */
    @Override
    public int getID() {
        return id;
    }
}
