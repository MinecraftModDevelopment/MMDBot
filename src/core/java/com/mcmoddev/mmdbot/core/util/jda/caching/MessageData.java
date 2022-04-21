package com.mcmoddev.mmdbot.core.util.jda.caching;

import net.dv8tion.jda.api.entities.Message;

public interface MessageData {

    String getId();
    long getIdLong();
    String getContent();

    long getAuthorId();

    static MessageData from(Message message) {
        return new MessageData() {
            final long id = message.getIdLong();
            final String content = message.getContentRaw();
            final long authorId = message.getAuthor().getIdLong();

            @Override
            public String getId() {
                return Long.toUnsignedString(id);
            }

            @Override
            public long getIdLong() {
                return id;
            }

            @Override
            public String getContent() {
                return content;
            }

            @Override
            public long getAuthorId() {
                return authorId;
            }
        };
    }

}
