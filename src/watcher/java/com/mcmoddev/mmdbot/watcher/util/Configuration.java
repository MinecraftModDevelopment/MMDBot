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
package com.mcmoddev.mmdbot.watcher.util;

import com.jagrosh.jdautilities.commons.utils.SafeIdUtil;
import com.mcmoddev.mmdbot.core.util.config.SnowflakeValue;
import com.mcmoddev.mmdbot.watcher.TheWatcher;
import com.mcmoddev.mmdbot.watcher.punishments.Punishment;
import net.dv8tion.jda.api.Permission;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.NodeKey;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
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

    @Required
    @Setting("roles")
    private Roles roles = new Roles();
    public Roles roles() {
        return roles;
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

    @Required
    @Setting("punishments")
    @Comment("""
    Punishments that will be applied to members when they do certain actions.
    The punishment format is: action [duration].
    Example:
        "KICK" -> kicks the member
        "BAN 5d" -> bans the member for 5 days
        "MUTE 12m" -> times the member out for 12 minutes
    A punishment can be "None" to prevent the member from being punished.""")
    private Punishments punishments = new Punishments();
    public Punishments punishments() {
        return punishments;
    }

    @ConfigSerializable
    public static final class Punishments {

        @Required
        @Setting("spam_pinging")
        public Punishment spamPing = new Punishment(Punishment.ActionType.BAN, Duration.ofDays(2));

        @Required
        @Setting("scam_link")
        public Punishment scamLink = new Punishment(Punishment.ActionType.MUTE, Duration.ofDays(1));

    }

    @Required
    @Setting("channels")
    @Comment("Channels configuration")
    private Channels channels = new Channels();
    public Channels channels() {
        return channels;
    }

    @ConfigSerializable
    public static final class Channels {



    }
}
