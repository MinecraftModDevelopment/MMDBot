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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.migrate.TricksMigrator;
import com.mcmoddev.mmdbot.commander.tricks.Trick;
import com.mcmoddev.mmdbot.core.database.MigratorCluster;
import com.mcmoddev.mmdbot.core.database.VersionedDataMigrator;
import com.mcmoddev.mmdbot.core.database.VersionedDatabase;
import com.mcmoddev.mmdbot.core.util.Constants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The storage container and manager for Quotes.
 * <p>
 * Contains the logic needed to add, remove, read and write the quotes contained within.
 * <p>
 * Most of this code is taken from willbl's Tricks file.
 * <p>
 * TODO: Fix the mess i made trying to get subclasses to serialize and deserialize.
 *
 * @author Curle
 */
public final class Quotes {

    private Quotes() {
    }

    /**
     * Location to the quote storage file.
     */
    private static final Supplier<Path> QUOTE_STORAGE = () -> TheCommander.getInstance().getRunPath().resolve("quotes.json");

    /**
     * The current version of the database schema.
     */
    public static final int CURRENT_SCHEMA_VERSION = 2;

    /**
     * The migrator used for migrating data.
     */
    public static final VersionedDataMigrator MIGRATOR = VersionedDataMigrator.builder()
        .addCluster(2, MigratorCluster.builder()
            .addMigrator(1, (current, target, data) -> {
                // Move to a Map
                final JsonObject newData = new JsonObject();
                newData.add(String.valueOf(0L), data);
                return newData;
            })
            .build())
        .build();

    /**
     * The codec used for serializing quotes.
     */
    public static final Codec<Map<Long, List<IQuote>>> CODEC = Codec.unboundedMap(
        Codec.LONG,
        Codec.list(new QuoteCodec())
            .<List<IQuote>>xmap(ArrayList::new, Function.identity()) // make the list mutable
    ).xmap(HashMap::new, Function.identity()); // make the map mutable

    /**
     * The static instance of NullQuote used as the reference for nulls.
     * Stops the GSON parser from entering an infinite loop.
     */
    public static final NullQuote NULL = new NullQuote();

    /**
     * A map of guild IDs to a list of Quote instances held by this container.
     * Quote metadata ID should sync with the index in this list.
     * This greatly simplifies access operations.
     */
    private static Map<Long, List<IQuote>> quotes = null;

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
     * All registered {@link com.mcmoddev.mmdbot.commander.quotes.IQuote.QuoteType}s
     */
    private static final BiMap<String, IQuote.QuoteType<?>> QUOTE_TYPES = HashBiMap.create();

    /**
     * Register a new {@link Trick.TrickType}.
     *
     * @param name the name to register the type under
     * @param type the type
     */
    public static void registerQuoteType(final String name, final IQuote.QuoteType<?> type) {
        QUOTE_TYPES.put(name, type);
    }

    /**
     * Gets all quote types.
     *
     * @return a map where the values are the quote types and the keys are their names
     */
    public static BiMap<String, IQuote.QuoteType<?>> getQuoteTypes() {
        return HashBiMap.create(QUOTE_TYPES);
    }

    /**
     * Gets a quote type by name.
     *
     * @param name the name
     * @return the quote type, or null if no such type exists
     */
    public static @Nullable IQuote.QuoteType<?> getQuoteType(final String name) {
        return QUOTE_TYPES.get(name);
    }

    /**
     * Gets the name of a quote type.
     *
     * @param type the type whose name to search.
     * @return the name of the quote type, or null if no such type exists
     */
    public static @Nullable String getQuoteTypeName(final IQuote.QuoteType<?> type) {
        return QUOTE_TYPES.inverse().get(type);
    }


    /**
     * Given a numeric ID, fetch the quote at that index, from that guild, or null.
     *
     * @param guildId the id of the guild to fetch the quote for.
     * @param id      the index to fetch the quote from.
     * @return the Quote object at that index.
     */
    @Nullable
    public static IQuote getQuote(final long guildId, final int id) {
        return getQuotesForGuild(guildId).get(id);
    }

    /**
     * Load quotes from file.
     * <p>
     * Has minimal error handling.
     */
    public static void loadQuotes() {
        if (quotes != null) {
            return;
        }

        final var path = QUOTE_STORAGE.get();
        if (!Files.exists(path)) {
            quotes = Collections.synchronizedMap(new HashMap<>());
        }
        final var data = VersionedDatabase.fromFile(path, CODEC, true, CURRENT_SCHEMA_VERSION, new HashMap<>())
            .flatMap(db -> {
                if (db.getSchemaVersion() != CURRENT_SCHEMA_VERSION) {
                    new TricksMigrator(TheCommander.getInstance().getRunPath()).migrate();
                    final var newDb = VersionedDatabase.fromFile(path, CODEC, true, CURRENT_SCHEMA_VERSION, new HashMap<>());
                    return newDb.map(VersionedDatabase::getData);
                } else {
                    return DataResult.success(db.getData());
                }
            });
        if (data.result().isPresent()) {
            quotes = Collections.synchronizedMap(data.result().get());
        } else if (data.error().isPresent()) {
            TheCommander.LOGGER.error("Reading quotes file encountered an error: {}", data.error().get().message());
            quotes = Collections.synchronizedMap(new HashMap<>());
        } else {
            quotes = Collections.synchronizedMap(new HashMap<>()); // this shouldn't be reached
        }

        if (quotes.get(0L) != null) {
            // Migrate to the new guild-specific quotes
            final var guildId = Long.parseLong(TheCommander.getInstance().getGeneralConfig().bot().guild());
            final var oldQuotes = quotes.get(0L);
            getQuotesForGuild(guildId).addAll(oldQuotes);
            quotes.remove(0L);
        }
    }

    /**
     * Write quotes to disk.
     * <p>
     * Has minimal error handling.
     */
    private static void syncQuotes() {
        if (quotes == null) {
            loadQuotes();
        }
        final var db = VersionedDatabase.inMemory(CURRENT_SCHEMA_VERSION, quotes);
        try (final var writer = Files.newBufferedWriter(QUOTE_STORAGE.get(), StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
            final var result = db.toJson(CODEC, true);
            Constants.Gsons.NO_PRETTY_PRINTING.toJson(result.result()
                    .orElseThrow(
                        () -> new IOException(result.error()
                            .orElseThrow() // throw if the message doesn't exist... that would be weird
                            .message())
                    ),
                writer);
        } catch (Exception exception) {
            TheCommander.LOGGER.error("Failed to write quote file...", exception);
        }
    }

    /**
     * Add the specified quote to the list at the specified index.
     *
     * @param guildId the ID of the guild for which to add the quote.
     * @param quote   The quote to add.
     */
    public static void addQuote(final long guildId, final Quote quote) {
        getQuotesForGuild(guildId).add(quote.getID(), quote);
        syncQuotes();
    }

    /**
     * Removes the specified quote from the list.
     * If the quote is at the end of the list, it will be replaced with the next addition.
     * If the quote is at any point before the start of the list, it will cause a null gap.
     * If the quote is at the start of a null gap and the null gap is at the end of the list, it will close the gap.
     * If the quote is after the end of the list, it will have no effect.
     *
     * @param guildId the ID of the guild for which to remove the quote.
     * @param id      the ID of the item to remove.
     */
    public static void removeQuote(final long guildId, final int id) {
        final var quotes = getQuotesForGuild(guildId);
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
     * @param guildId the ID of the guild to get the slot for
     * @return the index of the next empty slot in the list
     */
    public static int getQuoteSlot(final long guildId) {
        return getQuotesForGuild(guildId).size();
    }

    public static List<IQuote> getQuotesForGuild(final long guildId) {
        if (quotes == null) {
            loadQuotes();
        }
        return quotes.computeIfAbsent(guildId, k -> new ArrayList<>());
    }

    static {
        registerQuoteType("string", StringQuote.TYPE);
        registerQuoteType("null", NullQuote.TYPE);
    }

    /**
     * @return The message used for when quotes are null, or do not exist.
     */
    public static MessageEmbed getQuoteNotPresent() {
        return QUOTE_NOT_PRESENT;
    }

}
