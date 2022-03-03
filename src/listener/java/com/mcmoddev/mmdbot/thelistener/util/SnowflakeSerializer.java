package com.mcmoddev.mmdbot.thelistener.util;

import discord4j.common.util.Snowflake;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class SnowflakeSerializer implements TypeSerializer<Snowflake> {
    @Override
    public Snowflake deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
        final var str = node.getString();
        return str == null ? null : Snowflake.of(str);
    }

    @Override
    public void serialize(final Type type, @Nullable final Snowflake obj, final ConfigurationNode node) throws SerializationException {
        if (obj != null) {
            node.set(obj.asString());
        }
    }
}
