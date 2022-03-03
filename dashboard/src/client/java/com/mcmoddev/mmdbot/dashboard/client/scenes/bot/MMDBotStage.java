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
package com.mcmoddev.mmdbot.dashboard.client.scenes.bot;

import com.google.common.collect.ImmutableMap;
import com.mcmoddev.mmdbot.dashboard.BotTypeEnum;
import com.mcmoddev.mmdbot.dashboard.util.DashConfigType;

import java.util.List;
import java.util.Map;

public final class MMDBotStage extends DefaultBotStage {

    private static final ConfigEntry THING = new ConfigEntry(DashConfigType.STRING, "prefix", "commands.prefix.main", new String[] {"The prefix of the bot"});

    public static final Map<String, List<ConfigEntry>> CONFIGS = new ImmutableMap.Builder<String, List<ConfigEntry>>()
        .put("someConfig", List.of(
            new ConfigEntry(DashConfigType.STRING, "botOwner", "bot.owner", new String[] {"Something, yes"})
        ))
        .put("anotherConfig", List.of(
            THING, THING, THING, THING,THING, THING, THING, THING, THING, THING, THING, THING, THING
        ))
        .build();

    MMDBotStage() {
        super(CONFIGS, BotTypeEnum.MMDBOT);
    }
}
