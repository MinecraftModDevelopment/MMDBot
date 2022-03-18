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
package com.mcmoddev.mmdbot.commander.updatenotifiers.forge;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

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
 */
public final class ForgeVersionHelper {

    /**
     * The constant VERSION_URL.
     */
    private static final String VERSION_URL
        = "https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json";

    /**
     * The constant VERSION_REGEX.
     */
    private static final Pattern VERSION_REGEX = Pattern.compile("(.+?)-(.+)");

    /**
     * The constant GSON.
     */
    private static final Gson GSON = new Gson();


    /**
     * Instantiates a new Forge version helper.
     */
    private ForgeVersionHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Gets latest version.
     *
     * @param versions the versions
     * @return String. latest version
     */
    public static String getLatestVersion(final List<String> versions) {
        var latest = new SemVer(versions.get(0));

        for (final String version : versions) {
            final var ver = new SemVer(version);
            if (latest.compareTo(ver) < 0) {
                latest = ver;
            }
        }

        return latest.toString();
    }

    /**
     * Gets forge versions for mc version.
     *
     * @param mcVersion the mc version
     * @return ForgeVersion. forge versions for mc version
     * @throws IOException          the io exception
     * @throws ClassCastException   the class cast exception
     * @throws NullPointerException the null pointer exception
     */
    public static ForgeVersion getForgeVersionsForMcVersion(final String mcVersion) throws IOException,
        ClassCastException, NullPointerException {
        return getForgeVersions().get(mcVersion);
    }

    /**
     * Gets latest mc version forge versions.
     *
     * @return MinecraftForgeVersion. latest mc version forge versions
     * @throws IOException         the io exception
     * @throws JsonSyntaxException the json syntax exception
     * @throws JsonIOException     the json io exception
     */
    public static MinecraftForgeVersion getLatestMcVersionForgeVersions() throws IOException,
        JsonSyntaxException, JsonIOException {
        final Map<String, ForgeVersion> versions = getForgeVersions();

        final String latest = getLatestVersion(new ArrayList<>(versions.keySet()));

        return new MinecraftForgeVersion(latest, versions.get(latest));
    }

    /**
     * Open url input stream reader.
     *
     * @return InputStreamReader. input stream reader
     * @throws IOException the io exception
     */
    private static InputStreamReader openUrl() throws IOException {
        final var urlObj = new URL(VERSION_URL);

        return new InputStreamReader(urlObj.openStream(), StandardCharsets.UTF_8);
    }

    /**
     * Gets forge versions.
     *
     * @return Map. forge versions
     * @throws IOException         the io exception
     * @throws JsonSyntaxException the json syntax exception
     * @throws JsonIOException     the json io exception
     */
    public static Map<String, ForgeVersion> getForgeVersions() throws IOException,
        JsonSyntaxException, JsonIOException {
        final InputStreamReader reader = openUrl();

        final ForgePromoData data = GSON.fromJson(reader, ForgePromoData.class);

        // Remove this specific entry (differs from others with having the `_pre4` version)
        data.getPromos().remove("1.7.10_pre4-latest");

        // Collect version data
        final Map<String, ForgeVersion> versions = new HashMap<>();

        for (final Map.Entry<String, String> entry : data.getPromos().entrySet()) {
            final String mc = entry.getKey();
            final String forge = entry.getValue();

            final VersionMeta meta = getMCVersion(mc);

            if (meta != null) {
                if (versions.containsKey(meta.getVersion())) {
                    final ForgeVersion version = versions.get(meta.getVersion());
                    if (meta.getState().equals("recommended")) {
                        version.setRecommended(forge);
                    } else {
                        version.setLatest(forge);
                    }
                } else {
                    final var version = new ForgeVersion();
                    if (meta.getState().equals("recommended")) {
                        version.setRecommended(forge);
                    } else {
                        version.setLatest(forge);
                    }
                    versions.put(meta.getVersion(), version);
                }
            }
        }

        return versions;
    }

    /**
     * Gets mc version.
     *
     * @param version the version
     * @return VersionMeta. mc version
     */
    public static VersionMeta getMCVersion(final String version) {
        final var matcher = VERSION_REGEX.matcher(version);

        if (matcher.find()) {
            return new VersionMeta(matcher.group(1), matcher.group(2));
        } else {
            return null;
        }
    }
}
