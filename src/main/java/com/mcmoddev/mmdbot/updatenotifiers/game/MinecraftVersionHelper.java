package com.mcmoddev.mmdbot.updatenotifiers.game;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mcmoddev.mmdbot.MMDBot;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public final class MinecraftVersionHelper {

    private static final String API_URL = "https://meta.fabricmc.net/v2/versions/game";
    private static final Duration timeUntilOutdated = Duration.ofMinutes(20);
    private static String latest;
    private static String latestStable;
    private static Instant lastUpdated;

    static {
        update();
    }

    public static String getLatest() {
        if (latest == null)
            update();
        return latest;
    }

    public static String getLatestStable() {
        if (latestStable == null)
            update();
        return latestStable;
    }

    public static void update() {
        InputStreamReader reader = openUrl();
        if (reader == null)
            return;
        TypeToken<List<MinecraftVersionInfo>> token = new TypeToken<List<MinecraftVersionInfo>>() {
        };
        List<MinecraftVersionInfo> versions = new Gson().fromJson(reader, token.getType());

        latest = versions.get(0).version;
        latestStable = versions.stream().filter(it -> it.stable).findFirst().map(it -> it.version).orElse(latest);
        lastUpdated = Instant.now();
    }

    private static InputStreamReader openUrl() {
        try {
            URL url = new URL(API_URL);
            return new InputStreamReader(url.openStream());
        } catch (IOException e) {
            MMDBot.LOGGER.error("Failed to get minecraft version", e);
            return null;
        }
    }

    private static class MinecraftVersionInfo {
        public String version;
        public boolean stable;
    }
}
