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
package com.mcmoddev.mmdbot.core.util.jda.caching;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.NonNull;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

public class MessageCacheBuilder {

    private Caffeine<Object, Object> messageCache = Caffeine.newBuilder()
        .maximumSize(100_100)
        .expireAfterWrite(Duration.of(2, ChronoUnit.HOURS));
    private BiConsumer<MessageUpdateEvent, MessageData> onEdit;
    private BiConsumer<MessageDeleteEvent, MessageData> onDelete;

    public MessageCacheBuilder onEdit(final BiConsumer<MessageUpdateEvent, MessageData> onEdit) {
        this.onEdit = onEdit;
        return this;
    }

    public MessageCacheBuilder onDelete(final BiConsumer<MessageDeleteEvent, MessageData> onDelete) {
        this.onDelete = onDelete;
        return this;
    }

    public MessageCacheBuilder caffeine(@NonNull final UnaryOperator<Caffeine<Object, Object>> operator) {
        this.messageCache = operator.apply(messageCache);
        return this;
    }

    public JdaMessageCache build() {
        return new JdaMessageCacheImpl(messageCache.build(), onEdit, onDelete);
    }
}