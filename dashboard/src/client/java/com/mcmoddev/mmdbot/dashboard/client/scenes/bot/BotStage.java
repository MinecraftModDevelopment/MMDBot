package com.mcmoddev.mmdbot.dashboard.client.scenes.bot;

import com.mcmoddev.mmdbot.dashboard.BotTypeEnum;
import com.mcmoddev.mmdbot.dashboard.util.BotUserData;

import java.util.Map;

public interface BotStage {

    Map<BotTypeEnum, BotStage> STAGES = Map.of(
        BotTypeEnum.MMDBOT, new MMDBotStage(),
        BotTypeEnum.THE_WATCHER, new TheWatcherStage(),
        BotTypeEnum.THE_COMMANDER, new TheCommanderStage(),
        BotTypeEnum.THE_LISTENER, new TheListenerStage()
    );

    void createAndShowStage(BotUserData botUserData);

}
