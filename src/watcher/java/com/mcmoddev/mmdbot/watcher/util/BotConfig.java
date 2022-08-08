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

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.common.io.Resources;
import com.jagrosh.jdautilities.commons.utils.SafeIdUtil;
import net.dv8tion.jda.api.entities.Activity;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The configuration holder for the bot.
 *
 * TODO move to {@link Configuration}
 *
 * @author sciwhiz12
 */
@Deprecated
public final class BotConfig {

    /**
     * The Config.
     */
    private final CommentedFileConfig config;

    /**
     * The Newly generated.
     */
    private boolean newlyGenerated;

    /**
     * Instantiates a new Bot config.
     *
     * @param configFile the config file
     */
    public BotConfig(final Path configFile) {
        this(configFile, TomlFormat.instance());
    }

    /**
     * Instantiates a new Bot config.
     *
     * @param configFile   the config file
     * @param configFormat the config format
     */
    public BotConfig(final Path configFile, final ConfigFormat<? extends CommentedConfig> configFormat) {
        this.newlyGenerated = false;
        this.config = CommentedFileConfig.builder(configFile, configFormat)
            .autoreload()
            .onFileNotFound((file, format) -> {
                this.newlyGenerated = true;
                //noinspection UnstableApiUsage
                return FileNotFoundAction.copyData(Resources.getResource("default-config.toml"))
                    .run(file, format);
            })
            .preserveInsertionOrder()
            .build();
        config.load();
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
        return config.getIntOrElse("requests.freshness_duration", 0);
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

    /**
     * Gets aliased snowflake list.
     *
     * @param path    the path
     * @param aliases the aliases
     * @return List. aliased snowflake list
     */
    private Optional<List<Long>> getAliasedSnowflakeList(final String path,
                                                         final Optional<UnmodifiableConfig> aliases) {
        return config.<List<String>>getOptional(path)
            .filter(list -> !list.isEmpty())
            .map(strings -> strings.stream()
                .map(str -> aliases.flatMap(cfg -> cfg.<String>getOptional(str)).orElse(str))
                .map(SafeIdUtil::safeConvert)
                .filter(snowflake -> snowflake != 0)
                .collect(Collectors.toList()));
    }

    /**
     * Gets aliased.
     *
     * @param key     the key
     * @param aliases the aliases
     * @return String. aliased
     */
    private String getAliased(final String key, final Optional<UnmodifiableConfig> aliases) {
        return config.<String>getOptional(key)
            .map(str -> aliases.flatMap(cfg -> cfg.<String>getOptional(str)).orElse(str))
            .orElse("");
    }

    public Activity.ActivityType getActivityType() {
        return Activity.ActivityType.valueOf(config.getOrElse("bot.activity.type", "PLAYING"));
    }

    public String getActivityName() {
        return config.getOrElse("bot.activity.name", "");
    }
}
