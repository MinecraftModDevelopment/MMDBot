package com.mcmoddev.mmdbot.logging.util;

import discord4j.common.util.Snowflake;

import java.util.List;

public enum LoggingType {

    // TODO yes, config
    MESSAGE_EVENTS("message_events", s -> List.of(Snowflake.of(0L))),
    LEAVE_JOIN_EVENTS("leave_join_events", s -> List.of(Snowflake.of(0L)));

    private final String name;
    private final ChannelGetter channelGetter;

    LoggingType(final String name, final ChannelGetter channelGetter) {
        this.name = name;
        this.channelGetter = channelGetter;
    }

    public List<Snowflake> getChannels(Snowflake guild) {
        return channelGetter.getChannels(guild);
    }

    public String getName() {
        return name;
    }

    @FunctionalInterface
    public interface ChannelGetter {

        List<Snowflake> getChannels(Snowflake guild);

    }
}
