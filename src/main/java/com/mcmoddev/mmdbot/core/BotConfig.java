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
	 * Returns the snowflake ID of the bot's owner, or {@code null} is none is configured.
	 * <p>
	 * The bot owner has access to special owner-only commands.
	 *
	 * @return The snowflake ID of the bot owner, or {@code null}
	 */
	@Nullable
	public String getOwnerID() {
		return config.get("bot.owner");
	}

	/**
	 * Returns the snowflake ID of the guild where this bot resides, or {@code 0L} is none is configured.
	 *
	 * @return The snowflake ID of the guild
	 */
	public long getGuildID() {
		return SafeIdUtil.safeConvert(config.get("bot.guildId"));
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
	 * Returns the table of channel aliases, in the form of an {@link UnmodifiableConfig}.
	 * <p>
	 * Channel aliases are used in channel snowflakes lists to substitute the actual channel snowflake ID with a human-readable
	 * identifier.
	 *
	 * @return The table of channel aliases
	 */
	public Optional<UnmodifiableConfig> getChannelAliases() {
		return config.getOptional("channels.aliases");
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
		return SafeIdUtil.safeConvert(getAliased("channels." + channelKey, getChannelAliases()));
	}

	/**
	 * Returns whether the given command is enabled.
	 *
	 * @param commandName The command name
	 * @return If the command is enabled
	 */
	public boolean isEnabled(String commandName) {
		return config.<Boolean>getOrElse("commands." + commandName + ".enabled", true);
	}

	/**
	 * Returns the list of snowflake IDs of the channels where the given command is allowed to run in.
	 *
	 * @param commandName The command name
	 * @return The list of snowflake IDs of allowed channels
	 */
	public List<Long> getAllowedChannels(String commandName) {
		return getAliasedSnowflakeList("commands." + commandName + ".allowed_channels", getChannelAliases())
			.orElseGet(Collections::emptyList);
	}

	/**
	 * Returns whether the given command is allowed to run in the given channel.
	 * <p>
	 * If the allowed channels list for the command does not exist, then the command defaults to being allowed to run.
	 * Otherwise, the command is only allowed to run if the channel ID is in the list of allowed channels for the command.
	 *
	 * @param commandName The command name
	 * @param channelID   The snowflake ID of the channel
	 * @return If the command is allowed to run in the channel
	 */
	public boolean isAllowed(String commandName, long channelID) {
		final List<Long> allowedChannels = getAllowedChannels(commandName);
		return allowedChannels.isEmpty() || allowedChannels.contains(channelID);
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
		return SafeIdUtil.safeConvert(config.getOrElse("roles." + roleKey, ""));
	}

	/**
	 * Returns the list of snowflake IDs of the reaction emotes for bad requests.
	 *
	 * @return The list of snowflake IDs of bad request reaction emotes
	 */
	public List<Long> getBadRequestsReactions() {
		return getSnowflakeList("requests.emotes.bad").orElseGet(Collections::emptyList);
	}

	/**
	 * Returns the list of snowflake IDs of the reaction emotes for requests that need improvement.
	 * <p>
	 * These reaction emotes carry half the weight of the {@link #getBadRequestsReactions() bad requests reactions}.
	 *
	 * @return The list of snowflake IDs of needs improvement request reaction emotes
	 */
	public List<Long> getRequestsNeedsImprovementReactions() {
		return getSnowflakeList("requests.emotes.needs_improvement").orElseGet(Collections::emptyList);
	}

	/**
	 * Returns the list of snowflake IDs of the reaction emotes for good requests.
	 *
	 * @return The list of snowflake IDs of good request reaction emotes
	 */
	public List<Long> getGoodRequestsReactions() {
		return getSnowflakeList("requests.emotes.good").orElseGet(Collections::emptyList);
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
