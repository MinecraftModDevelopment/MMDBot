package com.mcmoddev.mmdbot.updatenotifiers.minecraft;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mcmoddev.mmdbot.MMDBot;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

/**
 *
 * @author
 *
 */
public final class MinecraftVersionHelper {

    /**
     *
     */
    private static final String API_URL = "https://meta.fabricmc.net/v2/versions/game";
//    private static final Duration timeUntilOutdated = Duration.ofMinutes(20);

    /**
     *
     */
    private static String latest;

    /**
     *
     */
    private static String latestStable;
//    private static Instant lastUpdated;

    static {
        update();
    }

	/**
	 *
	 */
   private MinecraftVersionHelper() {
       throw new IllegalStateException("Utility class");
   }

   /**
     *
     * @return
     */
    public static String getLatest() {
        if (latest == null) {
            update();
        }
        return latest;
    }

    /**
     *
     * @return
     */
    public static String getLatestStable() {
        if (latestStable == null) {
            update();
        }
        return latestStable;
    }

    /**
     *
     */
    public static void update() {
        final InputStreamReader reader = openUrl();
        if (reader == null) {
            return;
        }
        final TypeToken<List<MinecraftVersionInfo>> token = new TypeToken<List<MinecraftVersionInfo>>() {
        };
        final List<MinecraftVersionInfo> versions = new Gson().fromJson(reader, token.getType());

        latest = versions.get(0).version;
        latestStable = versions.stream().filter(it -> it.stable).findFirst().map(it -> it.version).orElse(latest);
//        lastUpdated = Instant.now();
    }

    /**
     *
     * @return
     */
    private static InputStreamReader openUrl() {
        try {
            final URL url = new URL(API_URL);
            return new InputStreamReader(url.openStream(), Charsets.UTF_8);
        } catch (IOException ex) {
            MMDBot.LOGGER.error("Failed to get minecraft version", ex);
            return null;
        }
    }

    /**
     *
     * @author
     *
     */
    private static class MinecraftVersionInfo {

        /**
         *
         */
        public String version;

        /**
         *
         */
        public boolean stable;
    }
}
