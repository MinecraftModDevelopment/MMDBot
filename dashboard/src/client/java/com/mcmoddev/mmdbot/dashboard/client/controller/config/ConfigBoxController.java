package com.mcmoddev.mmdbot.dashboard.client.controller.config;

import com.mcmoddev.mmdbot.dashboard.BotTypeEnum;
import com.mcmoddev.mmdbot.dashboard.util.DashConfigType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class ConfigBoxController {

    protected final BotTypeEnum botType;
    protected final DashConfigType type;
    protected final String configName;
    protected final String path;
    protected final String[] comments;

    public abstract void init();

}
