package com.mcmoddev.mmdbot.commander.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class Configuration {
    public static final Configuration EMPTY = new Configuration();
    public static final java.lang.reflect.Type TYPE = io.leangen.geantyref.TypeToken.get(Configuration.class).getType();

    @Required
    @Setting("bot")
    private Bot bot = new Bot();

    public Bot bot() {
        return bot;
    }

    @ConfigSerializable
    public static class Bot {

        @Required
        @Setting("owners")
        @Comment("The Snowflake IDs of the owners of the bot.")
        private List<String> owners = new ArrayList<>();

        public List<String> getOwners() {
            return owners;
        }

        @Required
        @Setting("guild")
        @Comment("The main guild of the bot.")
        private String guild = null;

        public String guild() {
            return guild;
        }
    }
}
