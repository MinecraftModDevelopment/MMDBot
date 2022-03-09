package com.mcmoddev.mmdbot.core.event.moderation;

import io.github.matyrobbrt.eventdispatcher.Event;

public class ModerationEvent implements Event {
    private final long guildId;
    private final long moderatorId;
    private final long targetId;

    public ModerationEvent(final long guildId, final long moderatorId, final long targetId) {
        this.guildId = guildId;
        this.moderatorId = moderatorId;
        this.targetId = targetId;
    }

    public long getModeratorId() {
        return moderatorId;
    }

    public long getTargetId() {
        return targetId;
    }

    public long getGuildId() {
        return guildId;
    }
}
