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

import com.google.common.base.Splitter;
import com.google.common.io.Resources;
import com.jagrosh.jdautilities.commons.utils.SafeIdUtil;
import com.mcmoddev.mmdbot.MMDBot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.WatchServiceListener;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The configuration holder for the bot.
 *
 * @author sciwhiz12
 */
public final class BotConfig {
    private static final Splitter DOT_SPLITTER = Splitter.on('.');

    /**
     * The path to the configuration file.
     */
    private final Path configPath;

    /**
     * The reference to the loaded configuration node.
     */
    private final ConfigurationReference<CommentedConfigurationNode> configNode;

    /**
     * The Newly generated.
     */
    private boolean newlyGenerated = false;

    /**
     * Instantiates a new Bot config.
     *
     * @param configFile the config file
     */
    public BotConfig(final Path configFile) {
        this.configPath = configFile;

        if (!Files.exists(configFile)) {
            this.newlyGenerated = true;
            //noinspection UnstableApiUsage
            try (InputStream defaultConfigStream = Resources.getResource("default-config.toml").openStream()) {
                Files.copy(defaultConfigStream, configFile);
            } catch (IOException e) {
                throw new UncheckedIOException("Error while trying to prepare default config file", e);
            }
        }

        try { // Normal configuration
            final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .emitComments(true)
                .prettyPrinting(true)
                .path(configPath)
                .build();
            this.configNode = loader.loadToReference();
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }

        try {
            final WatchServiceListener watch = WatchServiceListener.builder().fileSystem(configFile.getFileSystem()).build();
            watch.listenToFile(configFile, this::onWatch);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create watch service for configs", e);
        }
    }

    private void onWatch(WatchEvent<?> event) {
        try {
            this.configNode.load();
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to reload configuration after file change", e);
        }
    }

    /**
     * Returns whether the configuration file was newly generated (e.g. the bot was run for the first time).
     *
     * @return If the config file was newly generated
     */
    public boolean isNewlyGenerated() {
        return newlyGenerated;
    }

    /**
     * {@return the path to the configuration file}
     */
    public Path getConfigPath() {
        return configPath;
    }

    /**
     * {@return the reference to the root configuration node}
     */
    public ConfigurationReference<CommentedConfigurationNode> getConfigNode() {
        return configNode;
    }

    /**
     * Returns the configured bot token, or {@code null} if there is none configured.
     *
     * @return The configured bot token, or {@code ""}
     */
    @NotNull
    public String getToken() {
        final String token = configNode.get("bot", "token").getString();
        if (token == null || token.isEmpty() || token.indexOf('!') != -1) {
            return "";
        }
        return token;
    }

    @NotNull
    public String getGithubToken() {
        final String token = configNode.get("bot", "githubToken").getString();
        if (token == null || token.isEmpty() || token.indexOf('!') != -1) {
            return "";
        }
        return token;
    }

    @NotNull
    public String getOwlbotToken() {
        final String token = configNode.get("bot", "owlbotToken").getString();
        if (token == null || token.isEmpty() || token.indexOf('!') != -1) {
            return "";
        }
        return token;
    }

    /**
     * Returns the snowflake ID of the bot's owner.
     * <p>
     * The bot owner has access to special owner-only commands.
     *
     * @return The snowflake ID of the bot owner
     */
    public String getOwnerID() {
        return getAliased("bot.owner", getAliases());
    }

    /**
     * Returns the snowflake ID of the guild where this bot resides, or {@code 0L} is none is configured.
     *
     * @return The snowflake ID of the guild
     */
    public long getGuildID() {
        return SafeIdUtil.safeConvert(getAliased("bot.guildId", getAliases()));
    }

    /**
     * Returns the main commands prefix for bot commands.
     *
     * @return The main commands prefix
     */
    public String getMainPrefix() {
        return configNode.get("commands", "prefix", "alternative").getString("!mmd-");
    }

    /**
     * Returns the alternative commands prefix for bot commands.
     * <p>
     * This will usually be a shorter version of the {@linkplain #getMainPrefix() main commands prefix}, to be easier
     * and quicker to type out commands.
     *
     * @return The alternative commands prefix
     */
    public String getAlternativePrefix() {
        return configNode.get("commands", "prefix", "alternative").getString("!");
    }

    /**
     * Returns the table of aliases, in the form of an {@link CommentedConfigurationNode}.
     * <p>
     * Aliases are used in entries that require snowflake IDs to substitute the ID with a human-readable
     * identifier.
     *
     * @return The table of aliases
     */
    public CommentedConfigurationNode getAliases() {
        return configNode.get("aliases");
    }

    /**
     * Returns the configured alias for the given snowflake ID, if any.
     *
     * @param snowflake The snowflake ID
     * @return The alias for the given snowflake
     */
    public Optional<String> getAlias(final long snowflake) {
        return getAliases()
            .childrenMap().entrySet().stream()
            .filter(entry -> SafeIdUtil.safeConvert(entry.getValue().getString("")) == snowflake)
            .map(s -> String.valueOf(s.getKey()))
            .findFirst();
    }

    /**
     * Returns whether the given command is globally enabled for all guilds.
     * <p>
     * If there is no config entry for the command, this will default to returning {@code true}.
     *
     * @param commandName The command name
     * @return If the command is globally enabled, or {@code true} if not configured
     * @see #isEnabled(String, long) #isEnabled(String, long)#isEnabled(String, long)
     */
    public boolean isEnabled(final String commandName) {
        return configNode.get("commands", commandName, "enabled").getBoolean(true);
    }

    /**
     * @return If tricks should be run using prefix commands
     */
    public boolean prefixTricksEnabled() {
        return configNode.get("commands", "prefix_tricks_enabled").getBoolean(false);
    }

    /**
     * Returns whether the given command is enabled for the given guild.
     * <p>
     * If there is no config entry for the command and guild, this will default to returning the result of
     * {@link #isEnabled(String)}.
     *
     * @param commandName The command name
     * @param guildID     The guild's snowflake ID
     * @return If the command is enabled for the guild, or the value of {@link #isEnabled(String)}
     */
    public boolean isEnabled(final String commandName, final long guildID) {
        return configNode.get("commands",
            commandName,
            getAlias(guildID).orElseGet(() -> String.valueOf(guildID)),
            "enabled").getBoolean(isEnabled(commandName));
    }

    /**
     * Returns the list of blocked channels for the command in the given guild.
     *
     * @param commandName The command name
     * @param guildID     The guild's snowflake ID
     * @return The list of blocked channels for the command
     */
    public List<Long> getBlockedChannels(final String commandName, final long guildID) {
        return getAliasedSnowflakeList(
            References.COMMANDS + commandName + "."
                + getAlias(guildID).orElseGet(() -> String.valueOf(guildID))
                + ".blocked_channels", getAliases())
            .orElseGet(Collections::emptyList);
    }

    /**
     * Returns the list of allowed channels for the command in the given guild.
     *
     * @param commandName The command name
     * @param guildID     The guild's snowflake ID
     * @return The list of allowed channels for the command
     */
    public List<Long> getAllowedChannels(final String commandName, final long guildID) {
        return getAliasedSnowflakeList(
            References.COMMANDS + commandName + "."
                + getAlias(guildID).orElseGet(() -> String.valueOf(guildID))
                + ".allowed_channels", getAliases())
            .orElseGet(Collections::emptyList);
    }

    /**
     * Returns the list of hidden channels.
     * <p>
     * Hidden channels are channels which are not printed / hidden from the message when a command is run
     * in a non-allowed channel.
     *
     * @return The list of hidden channels
     */
    public List<Long> getHiddenChannels() {
        return getAliasedSnowflakeList(References.COMMANDS + "hidden_channels", getAliases())
            .orElseGet(Collections::emptyList);
    }

    /**
     * Returns the list of roles exempt from the blocklists and allowlists of commands.
     * <p>
     * Users with these roles bypass the block and allow lists of commands, allowing them to run (enabled) commands
     * in any channel.
     *
     * @return The roles exempt from channel checking
     */
    public List<Long> getChannelExemptRoles() {
        return getAliasedSnowflakeList(References.COMMANDS + "exempt_roles", getAliases())
            .orElseGet(Collections::emptyList);
    }

    /**
     * Returns the snowflake ID of the given role key based on the configuration, or {@code 0L} if none is configured.
     * <p>
     * The role key consists of ASCII letters, optionally separated by periods/full stops ({@code .}) for connoting
     * categories.
     *
     * @param roleKey The role key
     * @return The snowflake ID of the given role key, or {@code 0L}
     */
    public long getRole(final String roleKey) {
        return SafeIdUtil.safeConvert(getAliased("roles." + roleKey, getAliases()));
    }

    public long getRole(final RoleType role) {
        return getRole(role.toString());
    }

    public enum RoleType {
        BOT_MAINTAINER("bot_maintainer");
        private final String name;

        RoleType(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Returns the snowflake ID of the given channel based on the configuration, or {@code 0L} if none is configured.
     * <p>
     * The channel key consists of ASCII letters, optionally separated by periods/full stops ({@code .}) for connoting
     * categories.
     *
     * @param channelKey The channel key
     * @return The snowflake ID of the given channel key, or {@code 0L}
     */
    public long getChannel(final String channelKey) {
        return SafeIdUtil.safeConvert(getAliased("channels." + channelKey, getAliases()));
    }

    /**
     * Returns the amount of time in hours since a request was created for it to be deleted upon the user leaving the
     * server.
     * <p>
     * For example, a value of {@code 5} means all requests made by a user who leaves the server that is less than 5
     * hours old will be deleted.
     * <p>
     * A value of {@code 0} disables this leave deletion functionality.
     *
     * @return The time in hours since request creation by a leaving user to be deleted
     */
    public int getRequestLeaveDeletionTime() {
        return configNode.get("requests", "leave_deletion").getInt(0);
    }

    /**
     * Returns the amount of time in days; where a request is actionable using the requests warning and removal system.
     * <p>
     * A request that has existed for longer that this duration (a "stale request") will not cause the warning or
     * removal threshold to trip when reacted to by users.
     * <p>
     * A value of {@code 0} disables this freshness functionality, and allows any request to be actionable.
     *
     * @return The time in days for a request to be actionable by the warning system
     */
    public int getRequestFreshnessDuration() {
        return configNode.get("requests", "freshness_duration").getInt(0);
    }

    /**
     * Returns the list of snowflake IDs of the reaction emotes for bad requests.
     *
     * @return The list of snowflake IDs of bad request reaction emotes
     */
    public List<Long> getBadRequestsReactions() {
        return getAliasedSnowflakeList("requests.emotes.bad", getAliases())
            .orElseGet(Collections::emptyList);
    }

    /**
     * Returns the list of snowflake IDs of the reaction emotes for requests that need improvement.
     * <p>
     * These reaction emotes carry half the weight of the {@link #getBadRequestsReactions() bad requests reactions}.
     *
     * @return The list of snowflake IDs of needs improvement request reaction emotes
     */
    public List<Long> getRequestsNeedsImprovementReactions() {
        return getAliasedSnowflakeList("requests.emotes.needs_improvement", getAliases())
            .orElseGet(Collections::emptyList);
    }

    /**
     * Returns the list of snowflake IDs of the reaction emotes for good requests.
     *
     * @return The list of snowflake IDs of good request reaction emotes
     */
    public List<Long> getGoodRequestsReactions() {
        return getAliasedSnowflakeList("requests.emotes.good", getAliases())
            .orElseGet(Collections::emptyList);
    }

    /**
     * Returns the request reaction threshold before a user is warned of their request being potentially removed.
     *
     * @return The request warning threshold
     */
    public double getRequestsWarningThreshold() {
        return configNode.get("requests", "thresholds", "warning").getDouble(0.0D);
    }

    /**
     * Returns the request reaction threshold before a request is removed.
     *
     * @return The request removal threshold
     */
    public double getRequestsRemovalThreshold() {
        return configNode.get("requests", "thresholds", "removal").getDouble(0.0D);
    }

    /**
     * Returns the snowflake ID for the community channels category.
     *
     * @return The snowflake ID for the community channels category, or else {@code 0L}
     */
    public long getCommunityChannelCategory() {
        return SafeIdUtil.safeConvert(getAliased("community_channels.category", getAliases()));
    }

    /**
     * Gets community channel owner permissions.
     *
     * @return Set. community channel owner permissions
     */
    public Set<Permission> getCommunityChannelOwnerPermissions() {
        // "community_channels", "owner_permissions"
        if (configNode.get("community_channels", "owner_permissions").empty()) {
            return EnumSet.noneOf(Permission.class);
        }

        final long permissionField = configNode.get("community_channels", "owner_permissions").getLong(0L);
        if (permissionField != 0L) {
            return Permission.getPermissions(permissionField);
        }

        List<String> list = null;
        try {
            list = configNode.get("community_channels", "owner_permissions").getList(String.class);
        } catch (SerializationException e) {
            e.printStackTrace(); // TODO: proper handling
        }
        if (list != null) {
            final EnumSet<Permission> permissions = EnumSet.noneOf(Permission.class);
            outer:
            for (final String perm : list) {
                for (final Permission permission : Permission.values()) {
                    if (permission.getName().equals(perm) || permission.name().equals(perm)) {
                        permissions.add(permission);
                        continue outer;
                    }
                }
                MMDBot.LOGGER.warn("Unknown permission in \"{}\": '{}'",
                    References.COMMUNITY_CHANNEL_OWNER_PERMISSIONS, perm);
            }
            return permissions;
        }
        MMDBot.LOGGER.warn("Unknown format of \"{}\", resetting to blank list",
            References.COMMUNITY_CHANNEL_OWNER_PERMISSIONS);
        try {
            configNode.get("community_channels", "owner_permissions").set(Collections.emptyList());
        } catch (SerializationException e) {
            e.printStackTrace(); // TODO: proper handling
        }
        return EnumSet.noneOf(Permission.class);
    }

    /**
     * Gets aliased snowflake list.
     *
     * @param path    the path
     * @param aliases the aliases
     * @return List. aliased snowflake list
     */
    private Optional<List<Long>> getAliasedSnowflakeList(final String path,
                                                         final CommentedConfigurationNode aliases) {
        try {
            return Optional.ofNullable(configNode.get(DOT_SPLITTER.split(path)).getList(String.class))
                .filter(Predicate.not(Collection::isEmpty))
                .map(strings -> strings.stream()
                    .map(alias -> aliases.node(DOT_SPLITTER.split(alias)).getString(alias))
                    .map(SafeIdUtil::safeConvert)
                    .filter(snowflake -> snowflake != 0)
                    .toList()
                );
        } catch (SerializationException e) {
            e.printStackTrace(); // TODO: proper handling
            return Optional.empty();
        }
    }

    /**
     * Gets aliased.
     *
     * @param key     the key
     * @param aliases the aliases
     * @return String. aliased
     */
    private String getAliased(final String key, final CommentedConfigurationNode aliases) {
        return Optional.ofNullable(configNode.get(key).getString())
            .map(alias -> aliases.node(DOT_SPLITTER.split(alias)).getString(alias))
            .orElse("");
    }

    /**
     * A boolean check to enable or disable commands in production in case of issues or in dev should we not need them.
     *
     * @return true or false.
     */
    public boolean isCommandModuleEnabled() {
        return configNode.get("modules", "command_module_enabled").getBoolean(true);
    }

    /**
     * A boolean check to enable or disable event logging in production in case of issues or in dev should we not need
     * the module enabled to prevent spam.
     *
     * @return true or false.
     */
    public boolean isEventLoggingModuleEnabled() {
        return configNode.get("modules", "event_logging_module_enabled").getBoolean(true);
    }

    /**
     * Gets the role associated with the {@code emote} from the specified role panel
     *
     * @param channelId the channel ID of the role panel
     * @param messageId the message ID of the role panel
     * @param emote     the emote
     */
    public long getRoleForRolePanel(final long channelId, final long messageId, final String emote) {
        return configNode.get("role_panels", "%s-%s".formatted(channelId, messageId), emote).getLong(0L);
    }

    public long getRoleForRolePanel(final Message message, final String emote) {
        return getRoleForRolePanel(message.getChannel().getIdLong(), message.getIdLong(), emote);
    }

    public void addRolePanel(final long channelId, final long messageId, final String emote, final long roleId) {
        try {
            configNode.get("role_panels", "%s-%s".formatted(channelId, messageId), "permanent").set(roleId);
            configNode.save();
        } catch (ConfigurateException e) {
            e.printStackTrace(); // TODO: improve handling
        }
    }

    public boolean isRolePanelPermanent(final long channelId, final long messageId) {
        return configNode.get("role_panels", "%s-%s".formatted(channelId, messageId), "permanent").getBoolean(false);
    }

    public Activity.ActivityType getActivityType() {
        return Activity.ActivityType.valueOf(configNode.get("bot", "activity", "type").getString(""));
    }

    public String getActivityName() {
        return configNode.get("bot", "activity", "name").getString("");
    }
}
