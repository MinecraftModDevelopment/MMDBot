/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2023 <MMD - MinecraftModDevelopment>
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

import com.github.benmanes.caffeine.cache.Cache;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;

@ParametersAreNonnullByDefault
@SuppressWarnings("ClassCanBeRecord")
class JdaMessageCacheImpl implements JdaMessageCache, EventListener {

    private final Cache<Long, MessageData> messageCache;
    private final BiConsumer<MessageUpdateEvent, MessageData> onEdit;
    private final BiConsumer<MessageDeleteEvent, MessageData> onDelete;

    JdaMessageCacheImpl(final Cache<Long, MessageData> messageCache, @Nullable final BiConsumer<MessageUpdateEvent, MessageData> onEdit, @Nullable final BiConsumer<MessageDeleteEvent, MessageData> onDelete) {
        this.messageCache = messageCache;
        this.onEdit = onEdit == null ? ($, $$) -> {
        } : onEdit;
        this.onDelete = onDelete == null ? ($, $$) -> {
        } : onDelete;
    }

    @Override
    public void put(final Long id, final MessageData data) {
        messageCache.put(id, data);
    }

    @Override
    public void remove(final Long id) {
        messageCache.invalidate(id);
    }

    @Override
    public MessageData update(final Long id, final MessageData data) {
        final var old = get(id);
        put(id, data);
        return old;
    }

    @Override
    public @Nullable MessageData get(final Long id) {
        return messageCache.getIfPresent(id);
    }

    @Override
    public void onEvent(@NotNull final GenericEvent event) {
        if (event instanceof MessageReceivedEvent receivedEvent) {
            put(receivedEvent.getMessageIdLong(), MessageData.from(receivedEvent.getMessage()));
        } else if (event instanceof MessageUpdateEvent updateEvent) {
            final var old = update(updateEvent.getMessageIdLong(), MessageData.from(updateEvent.getMessage()));
            if (old != null) {
                onEdit.accept(updateEvent, old);
            }
        } else if (event instanceof MessageDeleteEvent deleteEvent) {
            final var old = get(deleteEvent.getMessageIdLong());
            if (old != null) {
                onDelete.accept(deleteEvent, old);
                remove(deleteEvent.getMessageIdLong());
            }
        }
    }
}
