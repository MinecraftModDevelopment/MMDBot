package com.mcmoddev.mmdbot.core;

import com.mcmoddev.mmdbot.core.bot.BotRegistry;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Configuration for state and token of bots.
 */
public class BotsConfig {
    private final ConfigurationReference<CommentedConfigurationNode> config;
    private final Map<String, BotConfigEntry> configEntries = new LinkedHashMap<>();

    BotsConfig(final Path configPath, final Map<String, BotRegistry.BotRegistryEntry<?>> registeredBots) {
        try {
            final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .emitComments(true)
                .prettyPrinting(true)
                .path(configPath)
                .build();
            this.config = loader.loadToReference();
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to load bots configuration", e);
        }

        checkAndLoad(registeredBots.keySet());
    }

    private void checkAndLoad(final Collection<String> registeredBots) {
        for (String name : registeredBots) {
            try {
                final ValueReference<Boolean, CommentedConfigurationNode> enabled = config.referenceTo(boolean.class, name, "enabled");
                final ValueReference<String, CommentedConfigurationNode> token = config.referenceTo(String.class, name, "token");
                final ValueReference<Path, CommentedConfigurationNode> runPath = config.referenceTo(Path.class, name, "runPath");

                configEntries.put(name, new BotConfigEntry(enabled, token, runPath));

                if (enabled.node().empty()) {
                    enabled.setAndSave(Boolean.FALSE);
                }
                if (token.node().isNull()) {
                    token.setAndSave("");
                }
                if (runPath.node().empty()) {
                    runPath.setAndSave(Path.of(name));
                }
            } catch (ConfigurateException e) {
                throw new UncheckedIOException("Failed to load configuration nodes for bot " + name, e);
            }
        }
    }

    public Optional<Boolean> getEnabled(String botName) {
        return Optional.ofNullable(configEntries.get(botName))
            .map(BotConfigEntry::enabledNode)
            .map(ValueReference::get);
    }

    public Optional<String> getToken(String botName) {
        return Optional.ofNullable(configEntries.get(botName))
            .map(BotConfigEntry::tokenNode)
            .map(ValueReference::get);
    }

    public Optional<Path> getRunPath(String botName) {
        return Optional.ofNullable(configEntries.get(botName))
            .map(BotConfigEntry::runPathNode)
            .map(ValueReference::get);
    }

    private record BotConfigEntry(ValueReference<Boolean, CommentedConfigurationNode> enabledNode,
                                  ValueReference<String, CommentedConfigurationNode> tokenNode,
                                  ValueReference<Path, CommentedConfigurationNode> runPathNode) {
    }
}
