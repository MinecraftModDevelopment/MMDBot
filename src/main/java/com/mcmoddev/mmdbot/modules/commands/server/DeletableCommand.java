package com.mcmoddev.mmdbot.modules.commands.server;

public interface DeletableCommand {
    void delete();
    void restore();
    boolean isDeleted();
}
