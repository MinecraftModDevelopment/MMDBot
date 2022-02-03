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
package com.mcmoddev.mmdbot.core.util;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.file.FileWatcher;
import com.electronwill.nightconfig.toml.TomlFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public abstract class Config<T extends Config<T>> {

    public static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    protected final CommentedFileConfig config;
    private final T defaultConfig;
    private boolean newlyGenerated;

    /**
     * @deprecated
     * This version is only supposed to be used for default configs!
     */
    @SuppressWarnings("unchecked")
    @Deprecated(forRemoval = false)
    protected Config() {
        this.config = null;
        this.newlyGenerated = false;
        this.defaultConfig = (T) this;
    }

    protected Config(final Path configFile, T defaultConfig) {
        this(configFile, TomlFormat.instance(), defaultConfig);
    }

    protected Config(final Path path, final ConfigFormat<? extends CommentedConfig> configFormat, T defaultConfig) {
        this.newlyGenerated = false;
        this.defaultConfig = defaultConfig;
        this.config = CommentedFileConfig.builder(path, configFormat).onFileNotFound((file, format) -> {
            this.newlyGenerated = true;
            return FileNotFoundAction.CREATE_EMPTY.run(path, configFormat);
        }).preserveInsertionOrder().build();
        config.load();
        readConfig();
        write();
        try {
            FileWatcher.defaultInstance().addWatch(path, () -> {
                LOGGER.info("Config file changed! Updating values...");
                readConfig();
            });
        } catch (IOException e) {
            LOGGER.error("Config file cannot be watched! The bot will be stopped!", e);
            System.exit(1);
        }

    }

    public boolean isNewlyGenerated() {
        return newlyGenerated;
    }

    @Documented
    @Retention(RUNTIME)
    @Target(ElementType.FIELD)
    protected @interface ConfigEntry {

        String name();

        String category() default "";

        String[] comments() default {};

        boolean commentDefaultValue() default true;
    }

    public static <T extends Config<?>> void write(final T config, final CommentedFileConfig configFile) {
        config.getFields().forEach(field -> {
            try {
                final var path = resolvePath(field.getAnnotation(ConfigEntry.class));
                if (!configFile.contains(path)) {
                    final var thisValue = field.get(config);
                    configFile.set(path, thisValue);
                    configFile.setComment(path, config.resolveComment(field));
                }
            } catch (IllegalAccessException e) {
                LOGGER.error("Error while trying to write config!", e);
            }
        });
        config.addExtraComments(configFile);
        configFile.save();
    }

    public final void write() {
        write(this, config);
    }

    public final void readConfig() {
        getFields().forEach(field -> {
            try {
                final var path = resolvePath(field.getAnnotation(ConfigEntry.class));
                if (config.contains(path)) {
                    field.set(this, config.get(path));
                } else {
                    field.set(this, field.get(defaultConfig));
                }
            } catch (IllegalAccessException e) {
                LOGGER.error("Error while trying to write config!", e);
            }
        });
    }

    public void addExtraComments(CommentedFileConfig config) {}

    public static String resolvePath(ConfigEntry entry) {
        String category = entry.category().isEmpty() ? "" : entry.category() + ".";
        return category + entry.name();
    }

    public String resolveComment(Field field) {
        ConfigEntry entry = field.getAnnotation(ConfigEntry.class);
        StringBuilder comment = new StringBuilder();
        if (entry.comments().length > 0 && !entry.comments()[0].isEmpty()) {
            comment.append(Constants.LINE_JOINER.join(entry.comments()));
        }
        if (entry.commentDefaultValue()) {
            if (!comment.isEmpty()) {
                comment.append(System.lineSeparator());
            }
            try {
                comment.append("default: ").append(field.get(defaultConfig));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                LOGGER.error("Error while trying to set a default value comment!", e);
            }
        }
        return comment.toString();
    }

    private List<Field> fields;

    public List<Field> getFields() {
        if (fields == null) {
            fields = Stream.of(getClass().getDeclaredFields()).filter(f ->
                !Modifier.isStatic(f.getModifiers()) && f.isAnnotationPresent(ConfigEntry.class))
                .peek(f -> f.setAccessible(true)).toList();
        }
        return fields;
    }

}
