package com.mcmoddev.mmdbot.core.event.customlog;

import java.util.List;

/**
 * Base class for trick events.
 */
public abstract class TrickEvent extends CustomAuditLogEvent {
    protected final String trickType;
    protected final List<String> trickNames;
    protected final String content;

    protected TrickEvent(final long guildId, final long responsibleUserId, final String trickType, final List<String> trickNames, final String content) {
        super(guildId, responsibleUserId);
        this.trickType = trickType;
        this.trickNames = trickNames;
        this.content = content;
    }

    /**
     * Gets the type of the affected trick.
     * @return the type of the affected trick
     */
    public String getTrickType() {
        return trickType;
    }

    /**
     * Gets the names of the affected trick.
     * @return the names of the affected trick
     */
    public List<String> getTrickNames() {
        return trickNames;
    }

    /**
     * Gets the content of the affected trick.
     * @return the content of the affected trick.
     */
    public String getContent() {
        return content;
    }

    public static final class Add extends TrickEvent {

        public Add(final long guildId, final long responsibleUserId, final String trickType, final List<String> trickNames, final String content) {
            super(guildId, responsibleUserId, trickType, trickNames, content);
        }

        /**
         * Gets the content of the newly added trick.
         * @return the content of the newly added trick
         */
        @Override
        public String getContent() {
            return super.getContent();
        }
    }

    public static final class Remove extends TrickEvent {

        public Remove(final long guildId, final long responsibleUserId, final String trickType, final List<String> trickNames, final String content) {
            super(guildId, responsibleUserId, trickType, trickNames, content);
        }

        /**
         * Gets the content of the old trick.
         * @return the content of the old trick.
         */
        @Override
        public String getContent() {
            return super.getContent();
        }
    }

    public static final class Edit extends TrickEvent {
        private final String newTrickType;
        private final List<String> newNames;
        private final String newContent;

        public Edit(final long guildId, final long responsibleUserId, final String oldType, final List<String> oldNames, final String oldContent, final String newTrickType, final List<String> newNames, final String newContent) {
            super(guildId, responsibleUserId, oldType, oldNames, oldContent);
            this.newTrickType = newTrickType;
            this.newNames = newNames;
            this.newContent = newContent;
        }

        /**
         * @return the new content of the trick
         */
        @Override
        public String getContent() {
            return newContent;
        }

        /**
         * @return the new names of the trick
         */
        @Override
        public List<String> getTrickNames() {
            return newNames;
        }

        /**
         * @return the new type of the trick
         */
        @Override
        public String getTrickType() {
            return newTrickType;
        }

        /**
         * @return the old type of the trick
         */
        public String getOldTrickType() {
            return trickType;
        }

        /**
         * @return the old names of the trick
         */
        public List<String> getOldTrickNames() {
            return trickNames;
        }

        /**
         * @return the old contents of the trick
         */
        public String getOldContent() {
            return content;
        }
    }
}
