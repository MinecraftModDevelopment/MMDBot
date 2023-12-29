/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2023 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.commander.updatenotifiers.quilt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mcmoddev.mmdbot.commander.updatenotifiers.SharedVersionHelpers;
import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Quilt version helper utilities.
 *
 * @author KiriCattus
 * @author matyrobbrt
 */
@UtilityClass
public final class QuiltVersionHelper extends SharedVersionHelpers {

    private static final String QUILT_MAPPINGS_URL = "https://meta.quiltmc.org/v3/versions/quilt-mappings";
    private static final String QUILT_LOADER_URL = "https://meta.quiltmc.org/v3/versions/loader";
    private static final String QUILT_STANDARD_LIBRARIES
        = "https://maven.quiltmc.org/repository/release/org/quiltmc/qsl/maven-metadata.xml";

    /**
     * Gets the latest mappings for Quilt.
     *
     * @param mcVersion the Minecraft version used
     * @return the latest mapping version
     */
    @Nullable
    public static String getLatestQuiltMappingsVersion(final String mcVersion) {
        return getMappingsVersions().getOrDefault(mcVersion, null);
    }

    /**
     * Gets the latest quilt mappings versions.
     *
     * @return the latest quilt mappings versions as a mcVersion -> quiltMVersion map
     */
    public static Map<String, String> getMappingsVersions() {
        final InputStreamReader reader = getReader(QUILT_MAPPINGS_URL);
        if (reader == null) {
            return Map.of();
        }
        final TypeToken<List<SharedVersionInfo>> token = new TypeToken<>() {
        };
        final List<SharedVersionInfo> versions = new Gson().fromJson(reader, token.getType());

        final Map<String, List<SharedVersionInfo>> map = versions.stream().distinct().collect(Collectors.groupingBy(
            it -> it.gameVersion));
        return map.keySet()
            .stream()
            .collect(Collectors.toMap(Function.identity(), it -> map.get(it).get(0).version));
    }

    /**
     * Gets the latest Quilt Loader version.
     */
    @Nullable
    public static String getLoaderVersion() {
        final InputStreamReader reader = getReader(QUILT_LOADER_URL);
        if (reader == null) {
            return null;
        }
        final TypeToken<List<SharedVersionHelpers.LoaderVersionInfo>> token = new TypeToken<>() {
        };
        final List<SharedVersionHelpers.LoaderVersionInfo> versions = new Gson().fromJson(reader, token.getType());
        return versions.get(0).version;
    }

    /**
     * Gets the latest QSL version.
     */
    public static String getQSLVersion() {
        return SharedVersionHelpers.getLatestFromMavenMetadata(QUILT_STANDARD_LIBRARIES);
    }
}
