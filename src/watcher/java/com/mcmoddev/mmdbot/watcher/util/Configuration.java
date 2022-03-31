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
