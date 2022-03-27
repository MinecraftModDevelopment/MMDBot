package com.mcmoddev.mmdbot.core.event.customlog;

import io.github.matyrobbrt.eventdispatcher.Event;

/**
 * The base class for custom audit log events. <br>
 * These events are fired on the {@link com.mcmoddev.mmdbot.core.event.Events#CUSTOM_AUDIT_LOG_BUS}.
 */
public abstract class CustomAuditLogEvent implements Event {
    protected final long guildId;
    protected final long responsibleUserId;

    protected CustomAuditLogEvent(final long guildId, final long responsibleUserId) {
        this.guildId = guildId;
        this.responsibleUserId = responsibleUserId;
    }

    /**
     * Gets the ID of the guild in which this event happened.
     * @return the ID of the guild in which this event happened
     */
    public long getGuildId() {
        return guildId;
    }

    /**
     * Gets the ID of the user responsible for this event.
     * @return the ID of the user responsible for this event
     */
    public long getResponsibleUserId() {
        return responsibleUserId;
    }

}
