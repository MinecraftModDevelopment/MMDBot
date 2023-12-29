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
package com.mcmoddev.mmdbot.commander.updatenotifiers.fabric;

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
 * The type Fabric version helper.
 *
 * @author williambl
 * @author KiriCattus
 * @author matyrobbrt
 */
@UtilityClass
public final class FabricVersionHelper extends SharedVersionHelpers {

    /**
     * The constant YARN_URL.
     */
    private static final String YARN_URL = "https://meta.fabricmc.net/v2/versions/yarn";

    /**
     * The constant LOADER_URL.
     */
    private static final String LOADER_URL = "https://meta.fabricmc.net/v2/versions/loader";

    /**
     * The constant API_URL.
     */
    private static final String API_URL
        = "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml";

    /**
     * Gets the latest Yarn version for a given {@code mcVersion}.
     *
     * @param mcVersion the MC version to query yarn for
     * @return the latest yarn version for that MC version, or else {@code null}
     */
    public static String getLatestYarn(final String mcVersion) {
        return getYarnVersions().getOrDefault(mcVersion, null);
    }

    /**
     * Gets the latest yarn versions.
     *
     * @return the latest yarn versions as a mcVersion -> yarnVersion map
     */
    public static Map<String, String> getYarnVersions() {
        final InputStreamReader reader = getReader(YARN_URL);
        if (reader == null) {
            return Map.of();
        }
        final TypeToken<List<SharedVersionHelpers.SharedVersionInfo>> token = new TypeToken<>() {
        };
        final List<SharedVersionHelpers.SharedVersionInfo> versions = new Gson().fromJson(reader, token.getType());

        final Map<String, List<SharedVersionHelpers.SharedVersionInfo>> map = versions.stream()
            .distinct()
            .collect(Collectors.groupingBy(it -> it.gameVersion));
        return map.keySet()
            .stream()
            .collect(Collectors.toMap(Function.identity(), it -> map.get(it).get(0).version));
    }

    /**
     * Gets the latest Fabric Loader version.
     */
    @Nullable
    public static String getLatestLoader() {
        final InputStreamReader reader = getReader(LOADER_URL);
        if (reader == null) {
            return null;
        }
        final TypeToken<List<SharedVersionHelpers.LoaderVersionInfo>> token = new TypeToken<>() {
        };
        final List<SharedVersionHelpers.LoaderVersionInfo> versions = new Gson().fromJson(reader, token.getType());

        return versions.get(0).version;
    }

    /**
     * Gets the latest Fabric API version.
     */
    @Nullable
    public static String getLatestApi() {
        return SharedVersionHelpers.getLatestFromMavenMetadata(API_URL);
    }
}
