/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
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

import com.jagrosh.jdautilities.commons.utils.SafeIdUtil;
import com.mcmoddev.mmdbot.core.util.config.SnowflakeValue;
import net.dv8tion.jda.api.Permission;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.EnumSet;
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
        private List<SnowflakeValue> owners = new ArrayList<>();

        public List<SnowflakeValue> getOwners() {
            return owners;
        }

        @Required
        @Setting("guild")
        @Comment("The main guild of the bot.")
        private SnowflakeValue guild = SnowflakeValue.EMPTY;

        public SnowflakeValue guild() {
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

    @ConfigSerializable
    public static final class Roles {
        @Required
        @Setting("bot_maintainers")
        @Comment("A list of Snowflake IDs representing the roles which are bot maintainers.")
        private List<SnowflakeValue> botMaintainers = new ArrayList<>();

        public List<SnowflakeValue> getBotMaintainers() {
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
        private SnowflakeValue requests = SnowflakeValue.EMPTY;

        public SnowflakeValue requests() {
            return requests;
        }

        @Required
        @Setting("free_mod_ideas")
        @Comment("The Free Mod Ideas channel.")
        private SnowflakeValue freeModIdeas = SnowflakeValue.EMPTY;

        public SnowflakeValue freeModIdeas() {
            return freeModIdeas;
        }

        @ConfigSerializable
        public static final class UpdateNotifiers {

            @Required
            @Setting("quilt")
            @Comment("A list of Snowflake IDs of channels in which to send Quilt update notifiers.")
            private List<SnowflakeValue> quilt = new ArrayList<>();

            public List<SnowflakeValue> quilt() {
                return quilt;
            }

            @Required
            @Setting("fabric")
            @Comment("A list of Snowflake IDs of channels in which to send Fabric update notifiers.")
            private List<SnowflakeValue> fabric = new ArrayList<>();

            public List<SnowflakeValue> fabric() {
                return fabric;
            }

            @Required
            @Setting("forge")
            @Comment("A list of Snowflake IDs of channels in which to send Forge update notifiers.")
            private List<SnowflakeValue> forge = new ArrayList<>();

            public List<SnowflakeValue> forge() {
                return forge;
            }

            @Required
            @Setting("minecraft")
            @Comment("A list of Snowflake IDs of channels in which to send Minecraft update notifiers.")
            private List<SnowflakeValue> minecraft = new ArrayList<>();

            public List<SnowflakeValue> minecraft() {
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

        @Required
        @Setting("reminders")
        @Comment("Reminders configuration.")
        private Reminders reminders = new Reminders();

        public Reminders reminders() {
            return reminders;
        }

        @ConfigSerializable
        public static final class Reminders {

            @Required
            @Setting("enabled")
            @Comment("If reminders should be enabled.")
            private boolean enabled = true;

            public boolean areEnabled() {
                return enabled;
            }

            @Required
            @Setting("snoozing_times")
            @Comment("""
                A list of snoozing times available for snoozing reminders.
                The format is: <time><unit>, where <time> is the snoozing time, and <unit> is the unit (`s` for seconds, `m` for minutes, `d` for days, etc.)
                Multiple times can be chained in the same configuration with a `-`. Example:
                12s-48m-2h (12 seconds, 48 minutes and 2 hours)""")
            private List<String> snoozingTimes = List.of(
                "5m", "1h", "1d"
            );

            public List<String> getSnoozingTimes() {
                return snoozingTimes;
            }

            @Required
            @Setting("limit_per_user")
            @Comment("The maximum amount of reminders a user can have.")
            private int limitPerUser = 100;

            public int getLimitPerUser() {
                return limitPerUser;
            }

            @Required
            @Setting("time_limit")
            @Comment("The maximum time (in seconds) that a reminder can be scheduled for.")
            private long timeLimit = 60 * 60 * 24 * 365;

            public long getTimeLimit() {
                return timeLimit;
            }
        }
    }
}
