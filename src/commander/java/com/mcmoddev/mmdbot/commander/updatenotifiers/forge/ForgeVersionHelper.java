/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
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
package com.mcmoddev.mmdbot.commander.updatenotifiers.forge;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.mcmoddev.mmdbot.core.util.SemVer;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The type Forge version helper.
 *
 * @author Antoine Gagnon
 * @author matyrobbrt
 */
@UtilityClass
public final class ForgeVersionHelper {

    private static final String VERSION_URL
        = "https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json";
    private static final Pattern VERSION_REGEX = Pattern.compile("(.+?)-(.+)");
    private static final Gson GSON = new Gson();

    /**
     * Gets latest version from a list of versions, using sem ver.
     *
     * @return the newest version
     */
    public static String getLatestVersion(final List<String> versions) {
        var latest = SemVer.from(versions.get(0));

        for (final String version : versions) {
            final var ver = SemVer.from(version);
            if (latest.compareTo(ver) < 0) {
                latest = ver;
            }
        }

        return latest.toString();
    }

    /**
     * Gets the latest forge version for an MC version.
     */
    public static ForgeVersion getForgeVersionsForMcVersion(final String mcVersion) throws IOException,
        NullPointerException {
        return getForgeVersions().get(mcVersion);
    }

    public static MinecraftForgeVersion getLatestMcVersionForgeVersions() throws IOException,
        JsonSyntaxException, JsonIOException {
        final Map<String, ForgeVersion> versions = getForgeVersions();

        final String latest = getLatestVersion(new ArrayList<>(versions.keySet()));

        return new MinecraftForgeVersion(latest, versions.get(latest));
    }

    private static InputStreamReader openUrl() throws IOException {
        final var urlObj = new URL(VERSION_URL);
        return new InputStreamReader(urlObj.openStream(), StandardCharsets.UTF_8);
    }

    /**
     * Gets all forge versions, grouped as mcVersion -> latest forge version.
     */
    public static Map<String, ForgeVersion> getForgeVersions() throws IOException,
        JsonSyntaxException, JsonIOException {
        final InputStreamReader reader = openUrl();

        final ForgePromoData data = GSON.fromJson(reader, ForgePromoData.class);

        // Remove this specific entry (differs from others with having the `_pre4` version)
        data.promos.remove("1.7.10_pre4-latest");

        // Collect version data
        final Map<String, ForgeVersion> versions = new HashMap<>();

        for (final Map.Entry<String, String> entry : data.promos.entrySet()) {
            final String mc = entry.getKey();
            final String forge = entry.getValue();

            final VersionMeta meta = getMCVersion(mc);

            if (meta != null) {
                if (versions.containsKey(meta.version())) {
                    final ForgeVersion version = versions.get(meta.version());
                    if (meta.state().equals("recommended")) {
                        version.setRecommended(forge);
                    } else {
                        version.setLatest(forge);
                    }
                } else {
                    final var version = new ForgeVersion();
                    if (meta.state().equals("recommended")) {
                        version.setRecommended(forge);
                    } else {
                        version.setLatest(forge);
                    }
                    versions.put(meta.version(), version);
                }
            }
        }

        return versions;
    }

    public static VersionMeta getMCVersion(final String version) {
        final var matcher = VERSION_REGEX.matcher(version);

        if (matcher.find()) {
            return new VersionMeta(matcher.group(1), matcher.group(2));
        } else {
            return null;
        }
    }

    public static class ForgePromoData {
        public Map<String, String> promos;
    }

    /**
     * @author Antoine Gagnon
     * @author matyrobbrt
     */
    record VersionMeta(String version, String state) {
    }
}
