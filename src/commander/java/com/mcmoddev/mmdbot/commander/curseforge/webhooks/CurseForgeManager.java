package com.mcmoddev.mmdbot.commander.curseforge.webhooks;

import io.github.matyrobbrt.curseforgeapi.CurseForgeAPI;

public record CurseForgeManager(CurseForgeAPI api, CFProjects projects) {
}
