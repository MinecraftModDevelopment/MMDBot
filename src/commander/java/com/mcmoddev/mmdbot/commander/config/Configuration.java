/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
package com.mcmoddev.mmdbot.commander.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
public final class Configuration {
    public static final Configuration EMPTY = new Configuration();

    @Required
    @Setting("bot")
    private Bot bot = new Bot();

    public Bot bot() {
        return bot;
    }

    @Required
    @Setting("channels")
    private Channels channels = new Channels();

    public Channels channels() {
        return channels;
    }

    @Required
    @Setting("roles")
    private Roles roles = new Roles();

    public Roles roles() {
        return roles;
    }

    @Required
    @Setting("features")
    @Comment("Configuration for features.")
    private Features features = new Features();

    public Features features() {
        return features;
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
        @Setting("forge_commands_guild_only")
        @Comment("If true, commands will be forced to register to the main guild.")
        private boolean forceCommandsGuildOnly;

        public boolean areCommandsForcedGuildOnly() {
            return forceCommandsGuildOnly;
        }
    }

    @ConfigSerializable
    public static final class Roles {
        @Required
        @Setting("bot_maintainers")
        @Comment("A list of Snowflake IDs representing the roles which are bot maintainers.")
        private List<String> botMaintainers = new ArrayList<>();

        public List<String> getBotMaintainers() {
            return botMaintainers;
        }
    }

    @ConfigSerializable
    public static final class Channels {

        @Required
        @Setting("update_notifiers")
        @Comment("Channels used for update notifiers.")
        private UpdateNotifiers updateNotifiers = new UpdateNotifiers();

        public UpdateNotifiers updateNotifiers() {
            return updateNotifiers;
        }

        @Required
        @Setting("requests")
        @Comment("The Requests channel.")
        private String requests = "";

        public String requests() {
            return requests;
        }

        @Required
        @Setting("free_mod_ideas")
        @Comment("The Free Mod Ideas channel.")
        private String freeModIdeas = "";

        public String freeModIdeas() {
            return freeModIdeas;
        }

        @ConfigSerializable
        public static final class UpdateNotifiers {

            @Required
            @Setting("fabric")
            @Comment("A list of Snowflake IDs of channels in which to send Fabric update notifiers.")
            private List<String> fabric = new ArrayList<>();

            public List<String> fabric() {
                return fabric;
            }

            @Required
            @Setting("forge")
            @Comment("A list of Snowflake IDs of channels in which to send Forge update notifiers.")
            private List<String> forge = new ArrayList<>();

            public List<String> forge() {
                return forge;
            }

            @Required
            @Setting("minecraft")
            @Comment("A list of Snowflake IDs of channels in which to send Minecraft update notifiers.")
            private List<String> minecraft = new ArrayList<>();

            public List<String> minecraft() {
                return minecraft;
            }
        }
    }

    @ConfigSerializable
    public static final class Features {

        @Required
        @Setting("referencing_enabled")
        @Comment("If message referencing should be enabled.")
        private boolean referencingEnabled = true;

        public boolean isReferencingEnabled() {
            return referencingEnabled;
        }

        @Required
        @Setting("tricks")
        @Comment("Tricks configuration.")
        private Tricks tricks = new Tricks();

        public Tricks tricks() {
            return tricks;
        }

        @ConfigSerializable
        public static final class Tricks {

            @Required
            @Setting("enabled")
            @Comment("If tricks should be enabled.")
            private boolean tricksEnabled = true;

            public boolean tricksEnabled() {
                return tricksEnabled;
            }

            @Required
            @Setting("prefix_enabled")
            @Comment("""
                Only if tricks are enabled!
                If tricks should be able to work with a prefixes.""")
            private boolean prefixEnabled = true;

            public boolean prefixEnabled() {
                return prefixEnabled;
            }
        }

        @Required
        @Setting("evaluation")
        @Comment("If evaluation is enabled.")
        private boolean evaluationEnabled = true;

        public boolean isEvaluationEnabled() {
            return evaluationEnabled;
        }

        @Required
        @Setting("quotes_enabled")
        @Comment("If quotes are enabled.")
        private boolean quotesEnabled = true;

        public boolean areQuotesEnabled() {
            return quotesEnabled;
        }
    }
}
