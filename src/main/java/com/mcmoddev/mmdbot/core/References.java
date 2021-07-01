package com.mcmoddev.mmdbot.core;

import com.mcmoddev.mmdbot.MMDBot;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

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
     * unavailable, the version shall be the combination of the string {@code "DEV "} and the the current date and time
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
     * The constant STICKY_ROLES_FILE_PATH.
     */
    public static final String STICKY_ROLES_FILE_PATH = "mmdbot_sticky_roles.json";

    /**
     * The constant USER_JOIN_TIMES_FILE_PATH.
     */
    public static final String USER_JOIN_TIMES_FILE_PATH = "mmdbot_user_join_times.json";

    public static Instant STARTUP_TIME;
}
