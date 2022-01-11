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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mcmoddev.mmdbot.MMDBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * The storage container and manager for Quotes.
 * <p>
 * Contains the logic needed to add, remove, read and write the quotes contained within.
 * <p>
 * Most of this code is taken from willbl's Tricks.class file.
 * <p>
 * TODO: Migrate to SQLite.
 * TODO: Fix the mess i made trying to get subclasses to serialize and deserialize.
 *
 * @author Curle
 */
public final class QuoteList {

    private QuoteList() {
    }

    /**
     * Location to the quote storage file.
     */
    private static final String QUOTE_STORAGE = "quotes.json";

    /**
     * The Gson instance used for serialization and deserialization.
     */
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapterFactory(new QuoteSerializer())
        .create();

    /**
     * The static instance of NullQuote used as the reference for nulls.
     * Stops the GSON parser from entering an infinite loop.
     */
    public static final NullQuote NULL = new NullQuote();

    /**
     * The list of Quote instances held by this container.
     * Quote metadata ID should sync with the index in this list.
     * This greatly simplifies access operations.
     */
    private static List<Quote> quotes = null;

    /**
     * The message used for when quotes are null, or do not exist.
     */
    private static final MessageEmbed QUOTE_NOT_PRESENT = new EmbedBuilder()
        .setTitle("Quote")
        .setColor(Color.GREEN)
        .addField("Warning", "Specified quote does not exist.", false)
        .setTimestamp(Instant.now())
        .build();

    /**
     * Given a numeric ID, fetch the quote at that index, or null.
     *
     * @param id The index to fetch the quote from.
     * @return The Quote object at that index.
     */
    @Nullable
    public static Quote getQuote(final int id) {
        if (quotes == null)
            loadQuotes();

        return quotes.get(id);
    }

    /**
     * Load quotes from file.
     * <p>
     * Has minimal error handling.
     */
    public static void loadQuotes() {
        if (quotes != null)
            return;

        final File quoteFile = new File(QUOTE_STORAGE);
        if (!quoteFile.exists())
            quotes = new ArrayList<>();

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(quoteFile), StandardCharsets.UTF_8)) {

            Type listType = new TypeToken<List<Quote>>() {
            }.getType();
            quotes = GSON.fromJson(reader, listType);

        } catch (final IOException exception) {
            MMDBot.LOGGER.trace("Failed to read quote file...", exception);
            quotes = new ArrayList<>();
        }
    }

    /**
     * Write quotes to disk.
     * <p>
     * Has minimal error handling.
     */
    private static void syncQuotes() {
        final File quoteFile = new File(QUOTE_STORAGE);
        try (OutputStreamWriter writer =
                 new OutputStreamWriter(new FileOutputStream(quoteFile), StandardCharsets.UTF_8)) {
            GSON.toJson(quotes, writer);
        } catch (Exception exception) {
            MMDBot.LOGGER.trace("Failed to write quote file...", exception);
        }
    }

    /**
     * Add the specified quote to the list at the specified index.
     *
     * @param quote The quote to add.
     */
    public static void addQuote(final Quote quote) {
        if (quotes == null)
            loadQuotes();

        quotes.add(quote.getID(), quote);
        syncQuotes();
    }

    /**
     * Removes the specified quote from the list.
     * If the quote is at the end of the list, it will be replaced with the next addition.
     * If the quote is at any point before the start of the list, it will cause a null gap.
     * If the quote is at the start of a null gap and the null gap is at the end of the list, it will close the gap.
     * If the quote is after the end of the list, it will have no effect.
     *
     * @param id the ID of the item to remove.
     */
    public static void removeQuote(final int id) {
        if (quotes == null)
            loadQuotes();

        quotes.set(id, NULL);

        // Count the number of nulls at the end of the list
        int index = 0;
        int nullElements = 0;
        while (index < quotes.size()) {
            if (quotes.get(index) != NULL) {
                nullElements = 0;
                index++;
                continue;
            }

            nullElements++;
            index++;
        }

        // Remove extras if necessary
        if (nullElements > 0)
            quotes.subList(quotes.size() - nullElements, quotes.size()).clear();

        // Write to disk
        syncQuotes();
    }

    /**
     * Get the index of the end of the list.
     * Does NOT find any holes before the end of the list.
     * TODO: Make this fill holes
     * For generating the next Quote ID.
     *
     * @return The index of the next empty slot in the list.
     */
    public static int getQuoteSlot() {
        if (quotes == null)
            loadQuotes();

        return quotes.size();
    }


    /**
     * @return The message used for when quotes are null, or do not exist.
     */
    public static MessageEmbed getQuoteNotPresent() {
        return QUOTE_NOT_PRESENT;
    }


    static final class QuoteSerializer implements TypeAdapterFactory {

        public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
            if (!Quote.class.isAssignableFrom(type.getRawType()))
                return null;

            // Fetch the Gson adapter that'd otherwise be used to serialize/deserialize this object
            final TypeAdapter<T> adapter = gson.getDelegateAdapter(this, type);

            // Create a new TypeAdapter, that.
            return new TypeAdapter<>() {

                // When asked to serialize.
                @Override
                public void write(final JsonWriter out, final T value) throws IOException {
                    out.beginObject();
                    // Writes { "type": "StringQuote" }
                    out.name("type").value(type.toString());
                    // Writes { "value": DATA }
                    out.name("value");
                    // And passes the serialization onto Gson's internal handlers.
                    adapter.write(out, value);
                    out.endObject();
                }

                // And when asked to read...
                @Override
                public T read(final JsonReader in) throws IOException {
                    if (in.peek() == JsonToken.NULL)
                        return null;

                    in.beginObject();


                    // Makes sure the type we wrote is there,
                    if (!"type".equals(in.nextName()))
                        return null;

                    try {
                        // Retrieves the class name we wrote,
                        Class<T> clazz = (Class<T>) Class.forName(in.nextString());
                        // Retrieves the TypeToken for the class,
                        TypeToken<T> readerType = TypeToken.get(clazz);

                        in.nextName(); // Skip value
                        // And passes on reading to Gson's internal handlers.
                        T result = gson.getDelegateAdapter(QuoteSerializer.this, readerType).read(in);
                        in.endObject();
                        return result;
                        // Except, when the class doesn't exist, it passes on the RuntimeError.
                    } catch (ClassNotFoundException exception) {
                        throw new RuntimeException(exception);
                    }
                }
            };
        }
    }

}
