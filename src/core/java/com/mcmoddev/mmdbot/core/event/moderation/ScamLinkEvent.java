package com.mcmoddev.mmdbot.core.event.moderation;

public class ScamLinkEvent extends ModerationEvent {
    private final long channelId;
    private final String messageContent;
    private final String targetAvatar;
    private final boolean editedMessage;

    public ScamLinkEvent(final long guildId, final long targetId, final long channelId, final String messageContent,
                         final String targetAvatar, final boolean editedMessage) {
        super(guildId, 0L, targetId);
        this.channelId = channelId;
        this.messageContent = messageContent;
        this.targetAvatar = targetAvatar;
        this.editedMessage = editedMessage;
    }

    @Override
    public long getModeratorId() {
        throw new UnsupportedOperationException();
    }

    public String getTargetAvatar() {
        return targetAvatar;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public long getChannelId() {
        return channelId;
    }

    public boolean isMessageEdited() {
        return editedMessage;
    }
}
