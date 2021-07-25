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

    /**
     * Create a new NullQuote.
     */
    public NullQuote() {
        this.setQuotee(null);
        this.setQuoteAuthor(null);
    }
}
