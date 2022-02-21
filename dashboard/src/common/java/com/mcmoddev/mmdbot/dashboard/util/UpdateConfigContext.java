package com.mcmoddev.mmdbot.dashboard.util;

import com.mcmoddev.mmdbot.dashboard.BotTypeEnum;

public record UpdateConfigContext(BotTypeEnum botType, DashConfigType configType, String configName, String path,
                                  Object newValue) {

}
