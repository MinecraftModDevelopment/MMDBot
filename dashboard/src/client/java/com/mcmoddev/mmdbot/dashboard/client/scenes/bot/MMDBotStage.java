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
