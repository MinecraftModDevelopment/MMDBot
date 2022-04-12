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
package com.mcmoddev.mmdbot.commander.updatenotifiers.quilt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.updatenotifiers.SharedVersionHelpers;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Quilt version helper utilities.
 *
 * @author KiriCattus
 */
public class QuiltVersionHelper extends SharedVersionHelpers {

    private static final String QUILT_MAPPINGS_URL = "https://meta.quiltmc.org/v3/versions/quilt-mappings";
    private static final String QUILT_LOADER_URL = "https://meta.quiltmc.org/v3/versions/loader";
    private static final String QUILT_STANDARD_LIBRARIES
        = "https://maven.quiltmc.org/repository/snapshot/org/quiltmc/qsl/qsl/maven-metadata.xml";
    private static final Map<String, String> LATEST_QUILT_MAPPINGS = new HashMap<>();
    private static String latestQuiltLoaderVersion;
    private static String latestQuiltStandardLibrariesVersion;

    static {
        updateAll();
    }

    /**
     * Instantiates a new Quilt version helper.
     */
    private QuiltVersionHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Gets the latest mappings for Quilt.
     *
     * @param mcVersion the Minecraft version used.
     * @return String. The latest mappings version.
     */
    public static String getLatestQuiltMappingsVersion(final String mcVersion) {
        if (LATEST_QUILT_MAPPINGS.isEmpty() || isOutdated()) {
            updateAll();
        }
        return LATEST_QUILT_MAPPINGS.get(mcVersion);
    }

    public static String getLatestLoaderVersion() {
        if (latestQuiltLoaderVersion == null || isOutdated()) {
            updateAll();
        }
        return latestQuiltLoaderVersion;
    }

    public static String getLatestQuiltStandardLibraries() {
        if (latestQuiltStandardLibrariesVersion == null || isOutdated()) {
            updateAll();
        }
        return latestQuiltStandardLibrariesVersion;
    }

    /**
     * Update all version info for Quilt and sub-tools.
     */
    public static void updateAll() {
        updateQuiltMappings();
        updateQuiltLoader();
        updateQuiltStandardLibraries();
        lastUpdatedTime = Instant.now();
    }

    /**
     * Update Quilt Mappings
     */
    private static void updateQuiltMappings() {
        final InputStreamReader reader = getReader(QUILT_MAPPINGS_URL);
        if (reader == null) {
            return;
        }
        final TypeToken<List<SharedVersionInfo>> token = new TypeToken<>() {
        };
        final List<SharedVersionInfo> versions = new Gson().fromJson(reader, token.getType());

        LATEST_QUILT_MAPPINGS.clear();
        final Map<String, List<SharedVersionInfo>> map = versions.stream().distinct().collect(Collectors.groupingBy(
            it -> it.gameVersion));
        map.keySet().forEach(it -> LATEST_QUILT_MAPPINGS.put(it, map.get(it).get(0).version));
    }

    /**
     * Update Quilt Loader version.
     */
    private static void updateQuiltLoader() {
        final InputStreamReader reader = getReader(QUILT_LOADER_URL);
        if (reader == null) {
            return;
        }
        final TypeToken<List<SharedVersionHelpers.LoaderVersionInfo>> token = new TypeToken<>() {
        };
        final List<SharedVersionHelpers.LoaderVersionInfo> versions = new Gson().fromJson(reader, token.getType());

        latestQuiltLoaderVersion = versions.get(0).version;
    }

    /**
     * Update Quilt Standard Libraries version.
     */
    private static void updateQuiltStandardLibraries() {
        final InputStream stream = getStream(QUILT_STANDARD_LIBRARIES);
        if (stream == null) {
            return;
        }
        try {
            final var doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(stream);
            final XPathExpression expr = XPathFactory.newInstance()
                .newXPath()
                .compile("/metadata/versioning/latest/text()");
            latestQuiltStandardLibrariesVersion = expr.evaluate(doc);
        } catch (SAXException | XPathExpressionException | ParserConfigurationException | IOException ex) {
            TheCommander.LOGGER.error("Failed to resolve latest Quilt Standard Libraries version", ex);
        }
    }
}
