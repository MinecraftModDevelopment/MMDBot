package com.mcmoddev.mmdbot.dashboard;

import javax.annotation.Nullable;

public enum BotTypeEnum {
    THE_COMMANDER("thecommander"),
    THE_LISTENER("thelistener"),
    THE_WATCHER("thewatcher"),

    /**
     * @deprecated The bot split
     */
    @Deprecated(forRemoval = true)
    MMDBOT("mmdbot");

    private final String name;

    BotTypeEnum(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public static BotTypeEnum byName(String name) {
        for (var type : values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }
}
