package com.mcmoddev.mmdbot.core;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.common.io.Resources;
import com.jagrosh.jdautilities.commons.utils.SafeIdUtil;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The configuration holder for the bot.
 */
public final class BotConfig {
    private final CommentedFileConfig config;
    private boolean newlyGenerated = false;

    public BotConfig(Path configFile) {
        this(configFile, TomlFormat.instance());
    }

    public BotConfig(Path configFile, ConfigFormat<? extends CommentedConfig> configFormat) {
        this.config = CommentedFileConfig.builder(configFile, configFormat)
                .autoreload()
                .onFileNotFound((file, format) -> {
                    this.newlyGenerated = true;
                    //noinspection UnstableApiUsage
                    return FileNotFoundAction.copyData(Resources.getResource("default-config.toml")).run(file, format);
                })
                .preserveInsertionOrder()
                .build();
        config.load();
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
     * Returns the raw {@link CommentedFileConfig} object.
     *
     * @return The raw config object
     */
    public CommentedFileConfig getConfig() {
        return config;
    }

    /**
     * Returns the configured bot token, or {@code null} if there is none configured.
     *
     * @return The configured bot token, or {@code null}
     */
    @Nullable
    public String getToken() {
        return config.<String>getOptional("bot.token")
                .filter(string -> string.indexOf('!') == -1 || string.isEmpty())
                .orElse(null);
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
        return config.getOrElse("commands.prefix.main", "!mmd-");
    }

    /**
     * Returns the alternative commands prefix for bot commands.
     * <p>
     * This will usually be a shorter version of the {@linkplain #getMainPrefix() main commands prefix}, to be easier and
     * quicker to type out commands.
     *
     * @return The alternative commands prefix
     */
    public String getAlternativePrefix() {
        return config.getOrElse("commands.prefix.alternative", "!");
    }

    /**
     * Returns the table of aliases, in the form of an {@link UnmodifiableConfig}.
     * <p>
     * Aliases are used in entries that require snowflake IDs to substitute the ID with a human-readable
     * identifier.
     *
     * @return The table of aliases
     */
    public Optional<UnmodifiableConfig> getAliases() {
        return config.getOptional("aliases");
    }

    /**
     * Returns the configured alias for the given snowflake ID, if any.
     *
     * @param snowflake The snowflake ID
     * @return The alias for the given snowflake
     */
    public Optional<String> getAlias(long snowflake) {
        return getAliases()
                .flatMap(aliases -> aliases.entrySet().stream()
                        .filter(entry -> entry.getValue() instanceof String)
                        .filter(entry -> SafeIdUtil.safeConvert(entry.getValue()) == snowflake)
                        .findFirst()
                )
                .map(UnmodifiableConfig.Entry::getKey);
    }

    /**
     * Returns whether the given command is globally enabled for all guilds.
     * <p>
     * If there is no config entry for the command, this will default to returning {@code true}.
     *
     * @param commandName The command name
     * @return If the command is globally enabled, or {@code true} if not configured
     * @see #isEnabled(String, long)
     */
    public boolean isEnabled(String commandName) {
        return config.<Boolean>getOrElse("commands." + commandName + ".enabled", true);
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
    public boolean isEnabled(String commandName, long guildID) {
        return config.<Boolean>getOptional(
                "commands." + commandName + "."
                        + getAlias(guildID).orElseGet(() -> String.valueOf(guildID))
                        + ".enabled")
                .orElseGet(() -> isEnabled(commandName));
    }

    /**
     * Returns the list of blocked channels for the command in the given guild.
     *
     * @param commandName The command name
     * @param guildID     The guild's snowflake ID
     * @return The list of blocked channels for the command
     */
    public List<Long> getBlockedChannels(String commandName, long guildID) {
        return getAliasedSnowflakeList(
                "commands." + commandName + "."
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
    public List<Long> getAllowedChannels(String commandName, long guildID) {
        return getAliasedSnowflakeList(
                "commands." + commandName + "."
                        + getAlias(guildID).orElseGet(() -> String.valueOf(guildID))
                        + ".allowed_channels", getAliases())
                .orElseGet(Collections::emptyList);
    }

    /**
     * Returns the list of hidden channels.
     * <p>
     * Hidden channels are channels which are not printed / hidden from the message when a command is run in a non-allowed channel.
     *
     * @return The list of hidden channels
     */
    public List<Long> getHiddenChannels() {
        return getAliasedSnowflakeList("commands.hidden_channels", getAliases())
            .orElseGet(Collections::emptyList);
    }

    /**
     * Returns the list of roles exempt from the blocklists and allowlists of commands.
     * <p>
     * Users with these roles bypass the block and allow lists of commands, allowing them to run (enabled) commands in any channel.
     *
     * @return The roles exempt from channel checking
     */
    public List<Long> getChannelExemptRoles() {
        return getAliasedSnowflakeList("commands.exempt_roles", getAliases())
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
    public long getRole(String roleKey) {
        return SafeIdUtil.safeConvert(getAliased("roles." + roleKey, getAliases()));
    }

    /**
     * Returns the snowflake ID of the given channel key based on the configuration, or {@code 0L} if none is configured.
     * <p>
     * The channel key consists of ASCII letters, optionally separated by periods/full stops ({@code .}) for connoting
     * categories.
     *
     * @param channelKey The channel key
     * @return The snowflake ID of the given channel key, or {@code 0L}
     */
    public long getChannel(String channelKey) {
        return SafeIdUtil.safeConvert(getAliased("channels." + channelKey, getAliases()));
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
        return config.<Number>getOrElse("requests.thresholds.warning", 0.0d).doubleValue();
    }

    /**
     * Returns the request reaction threshold before a request is removed.
     *
     * @return The request removal threshold
     */
    public double getRequestsRemovalThreshold() {
        return config.<Number>getOrElse("requests.thresholds.removal", 0.0d).doubleValue();
    }

    private Optional<List<Long>> getSnowflakeList(String path) {
        return config.<List<String>>getOptional(path)
                .map(strings -> strings.stream()
                        .map(SafeIdUtil::safeConvert)
                        .filter(snowflake -> snowflake != 0)
                        .collect(Collectors.toList()));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<List<Long>> getAliasedSnowflakeList(String path, Optional<UnmodifiableConfig> aliases) {
        return config.<List<String>>getOptional(path)
                .filter(list -> !list.isEmpty())
                .map(strings -> strings.stream()
                        .map(str -> aliases.flatMap(cfg -> cfg.<String>getOptional(str)).orElse(str))
                        .map(SafeIdUtil::safeConvert)
                        .filter(snowflake -> snowflake != 0)
                        .collect(Collectors.toList()));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private String getAliased(String key, Optional<UnmodifiableConfig> aliases) {
        return config.<String>getOptional(key)
                .map(str -> aliases.flatMap(cfg -> cfg.<String>getOptional(str)).orElse(str))
                .orElse("");
    }
}
