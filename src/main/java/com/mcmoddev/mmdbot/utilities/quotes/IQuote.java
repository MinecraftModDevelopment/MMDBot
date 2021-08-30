package com.mcmoddev.mmdbot.utilities.quotes;

import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * The interface which all Quote types implement.
 * Provides an interface for retrieving quote metadata.
 *
 * Quote metadata consists of:
 *  - Quotee; One of:
 *    - Discord User ID
 *    - String (for users not in MMD, or for external sources)
 *  - Author:
 *    - Discord User ID
 *  - Data; Serialized instance of a Quote
 *  - ID; An integer representing the place this quote holds in the list.
 *
 * Allows an arbitrary amount of "types" of quotes.
 *
 * @author Curle
 */
public interface IQuote {

    /**
     * @return The person or object being quoted.
     */
    UserReference getQuotee();

    /**
     * Set a new quotee.
     * @param quotee The person or object being quoted.
     */
    void setQuotee(UserReference quotee);

    /**
     * @return The person or object that created this quote.
     */
    UserReference getQuoteAuthor();

    /**
     * Set a new quote author.
     * @param author The person or object that created this quote.
     */
    void setQuoteAuthor(UserReference author);

    /**
     * @return The integer ID of this quote.
     */
    int getID();

    /**
     * Set a new ID.
     * @param id The integer ID of this quote.
     */
    void setID(int id);

    /**
     * Turn this quote's data into an Embed to be sent in a channel.
     * @return A fully formed Embed ready for sending.
     */
    MessageEmbed getQuoteMessage();

}
