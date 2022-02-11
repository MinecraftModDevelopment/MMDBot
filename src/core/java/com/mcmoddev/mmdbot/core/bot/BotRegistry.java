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
package com.mcmoddev.mmdbot.core.bot;

import com.mcmoddev.mmdbot.core.util.ReflectionsUtils;
import javassist.Modifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BotRegistry {

    // Names
    public static final String THE_COMMANDER_NAME = "thecommander";
    public static final String THE_LISTENER_NAME = "thelistener";
    public static final String THE_WATCHER_NAME = "thewatcher";

    public static final Logger LOG = LoggerFactory.getLogger(BotRegistry.class);

    private static final Map<String, BotRegistryEntry<?>> BOT_TYPES;

    static {
        BOT_TYPES = new ConcurrentHashMap<>();

        ReflectionsUtils.getFieldsAnnotatedWith(RegisterBotType.class)
            .stream().filter(f -> Modifier.isStatic(f.getModifiers())).forEach(field -> {
                if (!field.canAccess(null)) {
                    field.setAccessible(true);
                }
                try {
                    final var val = field.get(null);
                    if (val instanceof BotType<?> botType) {
                        final var ann = field.getAnnotation(RegisterBotType.class);
                        registerType(ann.name(), botType, ann.priority());
                    } else {
                        LOG.warn("Found a field annotated with RegisterBotType ({}) but the underlying object is not a bot type!", field);
                    }
                } catch (IllegalAccessException e) {
                    LOG.error("Exception while trying to collect bot types!", e);
                }
            });
    }

    /**
     * Registers a {@link BotType}
     * @param name the name of the bot
     * @param type the bot type to register
     * @param priority the priority. See {@link RegisterBotType#priority()}
     * @param <B> the type of the bot
     * @return the bot type ({@code type)}
     */
    public static <B extends Bot> BotType<B> registerType(String name, BotType<B> type, int priority) {
        BOT_TYPES.put(name, new BotRegistryEntry<>(type, priority));
        return type;
    }

    public static Map<String, BotRegistryEntry<?>> getBotTypes() {
        return Map.copyOf(BOT_TYPES);
    }

    public record BotRegistryEntry<B extends Bot>(BotType<B> botType, Integer priority) {}
}
