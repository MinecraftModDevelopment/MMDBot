/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2023 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.core.util.config;

import com.mcmoddev.mmdbot.core.util.Constants;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Class containing helpers for Configurate
 */
@Slf4j
@UtilityClass
public class ConfigurateUtils {

    /**
     * Loads a config, and sets any new values that are not present.
     *
     * @param loader        the loader to use
     * @param configPath    the path of the config
     * @param configSetter  a consumer which will set the new config in the case of reloads
     * @param configClass   the class of the config
     * @param defaultConfig the default config
     * @param <T>           the type of the config
     * @return a reference to the configuration node and the configuration reference
     */
    public static <T> Configuration<T, CommentedConfigurationNode> loadConfig(HoconConfigurationLoader loader, Path configPath, Consumer<T> configSetter, Class<T> configClass, T defaultConfig) throws ConfigurateException {
        final var configSerializer = Objects.requireNonNull(loader.defaultOptions().serializers().get(configClass));
        final var type = io.leangen.geantyref.TypeToken.get(configClass).getType();

        if (!Files.exists(configPath)) {
            try {
                final var node = loader.loadToReference();
                Files.createDirectories(configPath.getParent());
                Files.createFile(configPath);
                configSerializer.serialize(type, defaultConfig, node.node());
                node.save();
            } catch (Exception e) {
                throw new ConfigurateException(e);
            }
        }

        final var configRef = loader.loadToReference();

        { // Add new values to the config
            final var inMemoryNode = CommentedConfigurationNode
                .root(loader.defaultOptions());
            configSerializer.serialize(type, defaultConfig, inMemoryNode);
            configRef.node().mergeFrom(inMemoryNode);
            configRef.save();
        }

        class ConfigLoader {
            void reload(WatchEvent<?> event) {
                try {
                    configRef.load();
                    configSetter.accept(configRef.referenceTo(configClass).get());
                    log.warn("Reloaded config {}!", configPath);
                } catch (ConfigurateException e) {
                    log.error("Exception while trying to reload config {}!", configPath, e);
                }
            }
        }

        final var configLoader = new ConfigLoader();

        Constants.CONFIG_WATCH_SERVICE.listenToFile(configPath, configLoader::reload);

        return new Configuration<>(configRef, configRef.referenceTo(configClass));
    }

    public record Configuration<T, C extends ConfigurationNode>(ConfigurationReference<C> config,
                                                                ValueReference<T, C> value) {
    }
}
