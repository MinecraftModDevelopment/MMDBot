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

import com.jagrosh.jdautilities.command.GuildSettingsManager;
import com.mcmoddev.mmdbot.core.util.config.SnowflakeValue;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

@ConfigSerializable
public final class GuildConfiguration {
    public static final GuildConfiguration EMPTY = new GuildConfiguration();

    @Required
    @Setting("channels")
    private Channels channels = new Channels();
    public Channels channels() {
        return channels;
    }

    @ConfigSerializable
    public static final class Channels {
        @Required
        @Setting("community")
        @Comment("Community channels related configuration")
        private Community community = new Community();
        public Community community() {
            return community;
        }

        @ConfigSerializable
        public static final class Community {
            @Required
            @Setting("category")
            @Comment("The snowflake ID of the channel category where new community channels are created under")
            private SnowflakeValue category = SnowflakeValue.EMPTY;
            public SnowflakeValue category() {
                return category;
            }

            @Required
            @Setting("owner_permissions")
            @Comment("""
                The default permissions of channel owners in their new community channels
                This can be either bitfield, or a list of strings of the names of permissions (see the Permission enum)
                Note: the bot can only assign permissions to channel owners if the bot has those permissions
                Example: ["Manage Messages", "MANAGE_PERMISSIONS"]""")
            private PermissionList ownerPermissions = new PermissionList(Set.of(Permission.MESSAGE_MANAGE));
            public PermissionList ownerPermissions() {
                return ownerPermissions;
            }

            @Required
            @Setting("channel_created_message")
            @Comment("The message to send in the channel after it is created.")
            private String channelCreatedMessage = "Welcome {user} to your new channel ({channel}) here at {guild}. Enjoy your stay!";
            public String getChannelCreatedMessage() {
                return channelCreatedMessage;
            }
        }
    }

    @Required
    @Setting("features")
    @Comment("Configuration for features.")
    private Features features = new Features();

    public Features features() {
        return features;
    }

    @ConfigSerializable
    public static final class Features {

        @Required
        @Setting("custom_pings")
        @Comment("Custom pings configuration.")
        private CustomPings customPings = new CustomPings();

        public CustomPings customPings() {
            return customPings;
        }

        @ConfigSerializable
        public static final class CustomPings {
            @Required
            @Setting("enabled")
            @Comment("If custom pings should be enabled.")
            private boolean enabled = true;

            public boolean areEnabled() {
                return enabled;
            }

            @Required
            @Setting("limit_per_user")
            @Comment("The limit of custom pings per user, per guild.")
            private int limitPerUser = 10;

            public int getLimitPerUser() {
                return limitPerUser;
            }
        }
    }

    public record SettingManager(Long2ObjectFunction<GuildConfiguration> getter) implements GuildSettingsManager<GuildConfiguration> {

        @Nullable
        @Override
        public GuildConfiguration getSettings(final Guild guild) {
            return getter.get(guild.getIdLong());
        }
    }
}
