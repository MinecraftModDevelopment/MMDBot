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
