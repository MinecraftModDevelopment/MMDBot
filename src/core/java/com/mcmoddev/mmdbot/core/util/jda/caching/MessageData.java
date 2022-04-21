package com.mcmoddev.mmdbot.core.util.jda.caching;

import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MessageData {

    String getId();
    long getIdLong();
    String getContent();

    long getAuthorId();
    long getChannelId();
    List<String> getAttachments();
    @Nullable
    InteractionData getInteraction();

    static MessageData from(Message message) {
        return new MessageData() {
            final long id = message.getIdLong();
            final String content = message.getContentRaw();
            final long authorId = message.getAuthor().getIdLong();
            final long channelId = message.getChannel().getIdLong();
            final List<String> attachments = message.getAttachments().isEmpty() ? List.of() : message
                .getAttachments()
                .stream()
                .map(Message.Attachment::getUrl)
                .toList();
            final InteractionData interactionData = message.getInteraction() == null ? null :
                new InteractionData() {
                    final long authorId = message.getInteraction().getUser().getIdLong();
                    @Override
                    public long getAuthorId() {
                        return authorId;
                    }
                };

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

            @Override
            public long getChannelId() {
                return channelId;
            }

            @Override
            public List<String> getAttachments() {
                return attachments;
            }

            @Override
            public @Nullable InteractionData getInteraction() {
                return interactionData;
            }
        };
    }

}
