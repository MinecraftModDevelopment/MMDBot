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
import com.mcmoddev.mmdbot.core.dfu.ExtendedCodec;
import com.mcmoddev.mmdbot.core.dfu.ExtendedDynamicOps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * Used in place of null in the serialized list of quotes.
 *
 * @author Curle
 */
public final class NullQuote extends Quote {

    public static final Codec<NullQuote> CODEC = new ExtendedCodec<>() {
        @Override
        public <T> DataResult<Pair<NullQuote, T>> decode(final ExtendedDynamicOps<T> ops, final T input) {
            return ops.getOpsMap(input).map(map -> {
                final var quote = new NullQuote();
                quote.setID(map.getAsNumber("id").get().orThrow().intValue());
                return Pair.of(quote, input);
            });
        }

        @Override
        public <T> DataResult<T> encode(final NullQuote input, final ExtendedDynamicOps<T> ops, final T prefix) {
            return ops.mergeToMap(prefix, ops.createOpsMap()
                .put("id", ops.createInt(input.getID()))
            );
        }
    };

    public static final IQuote.QuoteType<NullQuote> TYPE = () -> CODEC;

    @Override
    public MessageEmbed getQuoteMessage() {
        return Quotes.getQuoteNotPresent();
    }

    @Override
    @ExposeScripting
    public String getQuoteText() {
        return "Quote not found.";
    }

    /**
     * Create a new NullQuote.
     */
    public NullQuote() {
        this.setQuotee(null);
        this.setQuoteAuthor(null);
    }

    @Override
    public QuoteType<?> getType() {
        return TYPE;
    }
}
