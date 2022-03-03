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
package com.mcmoddev.mmdbot.thelistener.util;

import discord4j.common.util.Snowflake;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.WatchServiceListener;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A guild-specific config for logging channels.
 * @author matyrobbrt
 */
@Slf4j
public class GuildConfig {
    private static WatchServiceListener watchService;

    private final long guildId;
    /**
     * The reference to the loaded configuration node.
     */
    private final ConfigurationReference<CommentedConfigurationNode> configNode;
    private final Path configPath;

    public GuildConfig(final long guildId, @NonNull final Path folderPath) {
        this.guildId = guildId;

        configPath = folderPath.resolve(guildId + ".conf");

        var isNew = false;

        try {
            if (!Files.exists(configPath)) {
                if (!Files.exists(folderPath)) {
                    Files.createDirectories(folderPath);
                }
                Files.createFile(configPath);
                isNew = true;
            }
        } catch (IOException exception) {
            throw new RuntimeException("Exception while trying to generate config for guild " + guildId, exception);
        }

        try { // Normal configuration
            final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .emitComments(true)
                .prettyPrinting(true)
                .path(configPath)
                .build();
            this.configNode = loader.loadToReference();

            if (isNew) {
                for (final LoggingType type : LoggingType.values()) {
                    createNodeForLogging(type);
                }
                configNode.save();
            }
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }

        try {
            if (watchService == null) {
                watchService = WatchServiceListener
                    .builder()
                    .fileSystem(configPath.getFileSystem())
                    .threadFactory(r -> Utils.setThreadDaemon(new Thread(r, "ConfigLoader"), true))
                    .build();
            }
            watchService.listenToFile(configPath, this::onWatch);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create watch service for guild config " + guildId, e);
        }
    }

    private void onWatch(WatchEvent<?> event) {
        try {
            this.configNode.load();
            log.info("Reloading config {}", configPath);
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to reload configuration for guild " + guildId + " after file change", e);
        }
    }

    public Set<Snowflake> getChannelsForLogging(LoggingType type) {
        return catchException(() -> {
            final var path = new Object[]{
                "channels", type.getName()
            };
            final var node = configNode.get(path);
            if (node.empty()) {
                createNodeForLogging(type);
                configNode.save();
            }
            return Objects.requireNonNull(configNode.get("channels", type.getName()).getList(String.class))
                .stream()
                .map(Snowflake::of)
                .collect(Collectors.toUnmodifiableSet());
        }, Set::of);
    }

    private void createNodeForLogging(LoggingType type) throws SerializationException {
        final var path = new Object[]{
            "channels", type.getName()
        };
        final var node = configNode.get(path);
        if (!node.empty()) {
            return;
        }
        configNode.set(node.path(), List.of());
        configNode.get(path).comment("A list of snowflakes representing the channel(s) in which to send logging events of the type \"" + type + "\"");
    }

    private static <T> T catchException(ExceptionSupplier<T> sup, Supplier<T> onException) {
        try {
            return sup.get();
        } catch (ConfigurateException e) {
            log.error("Exception while handling configs.", e);
            return onException.get();
        }
    }

    private interface ExceptionSupplier<R> {
        R get() throws ConfigurateException;
    }
}
