package com.mcmoddev.mmdbot.modules.commands.server;

/**
 * A command which can be disabled on delete.
 */
public interface DeletableCommand {
    /**
     * Mark this command as deleted.
     */
    void delete();

    /**
     * Unmark this command as deleted.
     */
    void restore();

    /**
     * Gets whether this command is marked as deleted or not.
     *
     * @return whether the command is marked as deleted
     */
    boolean isDeleted();
}
