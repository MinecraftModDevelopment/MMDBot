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
    public static final java.lang.reflect.Type TYPE = io.leangen.geantyref.TypeToken.get(Configuration.class).getType();

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
                return fabric;
            }

            @Required
            @Setting("minecraft")
            @Comment("A list of Snowflake IDs of channels in which to send Minecraft update notifiers.")
            private List<String> minecraft = new ArrayList<>();
            public List<String> minecraft() {
                return fabric;
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
    }
}
