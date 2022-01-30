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
package com.mcmoddev.mmdbot.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mcmoddev.mmdbot.MMDBot;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Store settings that don't really change in here.
 *
 * @author ProxyNeko
 */
public class References {

    /**
     * The name of the bot in code.
     */
    public static final String NAME = "MMDBot";

    /**
     * The bot's current version.
     *
     * <p>
     * The version will be taken from the {@code Implementation-Version} attribute of the JAR manifest. If that is
     * unavailable, the version shall be the combination of the string {@code "DEV "} and the current date and time
     * in {@link java.time.format.DateTimeFormatter#ISO_OFFSET_DATE_TIME}.
     */
    public static final String VERSION;

    // Gets the version from the JAR manifest, else defaults to the time the bot was started
    static {
        var version = MMDBot.class.getPackage().getImplementationVersion();
        if (version == null) {
            version = "DEV " + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now(ZoneOffset.UTC));
        }
        VERSION = version;
    }

    /**
     * The issue tracker where bugs and crashes should be reported, and PR's made.
     */
    public static final String ISSUE_TRACKER = "https://github.com/MinecraftModDevelopment/MMDBot/issues/";

    /**
     * The constant COMMUNITY_CHANNEL_OWNER_PERMISSIONS.
     */
    public static final String COMMUNITY_CHANNEL_OWNER_PERMISSIONS = "community_channels.owner_permissions";

    /**
     * The constant COMMANDS.
     */
    public static final String COMMANDS = "commands.";

    /**
     * The constant STARTUP_TIME.
     */
    public static Instant STARTUP_TIME;

    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public static final Random RANDOM = new Random();
}
