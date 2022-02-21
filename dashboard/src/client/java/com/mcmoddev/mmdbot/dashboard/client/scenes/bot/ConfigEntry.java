package com.mcmoddev.mmdbot.dashboard.client.scenes.bot;

import com.mcmoddev.mmdbot.dashboard.util.DashConfigType;

record ConfigEntry(DashConfigType type, String configName, String path, String[] comments) {
}
