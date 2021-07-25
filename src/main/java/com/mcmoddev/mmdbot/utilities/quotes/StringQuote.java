package com.mcmoddev.mmdbot.utilities.quotes;

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.References;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.entities.UserById;

import java.awt.Color;
import java.time.Instant;

/**
 * A Quote implementation that encodes a String as the thing being quoted.
 *
 * A StringQuote is serialized as such:
 *   {
 *       DATA
 *   }
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
     * @param pData The message or content being quoted.
     */
    protected void setData(final String pData) {
        this.data = pData;
    }

    /**
     * @return The message or content being quoted.
     */
    protected String getData() {
        return data;
    }

    @Override
    public MessageEmbed getQuoteMessage() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(References.NAME, MMDBot.getInstance().getSelfUser().getAvatarUrl());
        builder.setTitle("Quote " + this.getID());
        builder.setColor(Color.GREEN);
        builder.addField("Content", this.getData(), false);

        // Resolve the person being quoted.
        UserReference quotee = this.getQuotee();
        switch (quotee.getReferenceType()) {
            case SNOWFLAKE:
                UserById user = new UserById(quotee.getSnowflakeData());
                builder.addField("Author", user.getAsMention(), false);
                break;
            case STRING:
                builder.addField("Author", quotee.getStringData(), false);
                break;

            case ANONYMOUS: // Intentional fallthrough.
            default:
                builder.addField("Author", quotee.getAnonymousData(), false);
                break;
        }

        // Resolve the person that made the quote..
        UserReference author = this.getQuoteAuthor();
        switch (author.getReferenceType()) {
            case SNOWFLAKE:
                // Try to find the user's data in a server
                User user = MMDBot.getInstance().getUserById(author.getSnowflakeData());
                // If we have it...
                if (user != null) {
                    // Use it
                    builder.setFooter("Quoted by " + user.getAsTag());
                } else {
                    // Otherwise, fall back to the snowflake.
                    builder.setFooter("Quoted by " + author.getSnowflakeData());
                }
                break;
            case STRING:
                builder.setFooter("Quoted by " + author.getStringData());
                break;

            case ANONYMOUS: // Intentional fallthrough.
            default:
                builder.setFooter("Quoted by " + author.getAnonymousData());
                break;
        }

        builder.setTimestamp(Instant.now());
        return builder.build();
    }

    /**
     * Construct a new StringQuote with all the necessary data.
     * @param quotee A Reference to the User being Quoted.
     * @param quote The message or content being Quoted.
     * @param creator A Reference to the User that created the Quote.
     */
    public StringQuote(final UserReference quotee, final String quote, final UserReference creator) {
        this.setQuotee(quotee);
        this.setData(quote);
        this.setQuoteAuthor(creator);
    }
}
