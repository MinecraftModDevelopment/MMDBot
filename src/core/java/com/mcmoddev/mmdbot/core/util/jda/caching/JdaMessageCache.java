package com.mcmoddev.mmdbot.core.util.jda.caching;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface JdaMessageCache {

    void put(Long id, MessageData data);
    MessageData update(Long id, MessageData data);
    void remove(Long id);

    @Nullable
    MessageData get(Long id);

    static MessageCacheBuilder builder() {
        return new MessageCacheBuilder();
    }
}
