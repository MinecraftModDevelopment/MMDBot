package com.mcmoddev.mmdbot.utilities.quotes;

/**
 * Handles the attribution aspect of a Quote.
 *
 * Exists to be the superclass to implementations that hold data.
 * As such, it is abstract so that the data and thus getQuoteMessage() function may be undefined.
 *
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
