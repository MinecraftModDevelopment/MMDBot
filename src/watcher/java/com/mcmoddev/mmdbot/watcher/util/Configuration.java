package com.mcmoddev.mmdbot.watcher.util;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class Configuration {
    public static final Configuration EMPTY = new Configuration();

    @Required
    @Setting("bot")
    private Bot bot = new Bot();

    public Bot bot() {
        return bot;
    }

    @ConfigSerializable
    public static final class Bot {

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
        private String guild = "";

        public String guild() {
            return guild;
        }

        @Required
        @Setting("prefixes")
        @Comment("The prefixes the bot should use.")
        private List<String> prefixes = new ArrayList<>();

        public List<String> getPrefixes() {
            return prefixes;
        }

        @Required
        @Setting("force_commands_guild_only")
        @Comment("If true, commands will be forced to register to the main guild.")
        private boolean forceCommandsGuildOnly = false;

        public boolean areCommandsForcedGuildOnly() {
            return forceCommandsGuildOnly;
        }
    }
}
