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
package com.mcmoddev.mmdbot.commander.updatenotifiers.fabric;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mcmoddev.mmdbot.commander.TheCommander;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The type Fabric version helper.
 *
 * @author williambl
 */
public final class FabricVersionHelper {

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
     * The constant LATEST_YARNS.
     */
    private static final Map<String, String> LATEST_YARNS = new HashMap<>();

    /**
     * The constant TIME_UNTIL_OUTDATED.
     */
    private static final Duration TIME_UNTIL_OUTDATED = Duration.ofMinutes(20);

    /**
     * The constant latestLoader.
     */
    private static String latestLoader;

    /**
     * The constant latestApi.
     */
    private static String latestApi;

    /**
     * The constant lastUpdated.
     */
    private static Instant lastUpdated;

    static {
        update();
    }

    /**
     * Instantiates a new Fabric version helper.
     */
    private FabricVersionHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Gets latest yarn.
     *
     * @param mcVersion the mc version
     * @return String. latest yarn
     */
    public static String getLatestYarn(final String mcVersion) {
        if (LATEST_YARNS.isEmpty() || isOutdated()) {
            update();
        }
        return LATEST_YARNS.get(mcVersion);
    }

    /**
     * Gets latest loader.
     *
     * @return String. latest loader
     */
    public static String getLatestLoader() {
        if (latestLoader == null || isOutdated()) {
            update();
        }
        return latestLoader;
    }

    /**
     * Gets latest api.
     *
     * @return String. latest api
     */
    public static String getLatestApi() {
        if (latestApi == null || isOutdated()) {
            update();
        }
        return latestApi;
    }

    /**
     * Is outdated boolean.
     *
     * @return boolean. boolean
     */
    private static boolean isOutdated() {
        return lastUpdated.plus(TIME_UNTIL_OUTDATED).isBefore(Instant.now());
    }

    /**
     * Update.
     */
    public static void update() {
        updateYarn();
        updateLoader();
        updateApi();
        lastUpdated = Instant.now();
    }

    /**
     * Update yarn.
     */
    private static void updateYarn() {
        final InputStreamReader reader = getReader(YARN_URL);
        if (reader == null) {
            return;
        }
        final TypeToken<List<YarnVersionInfo>> token = new TypeToken<>() {
        };
        final List<YarnVersionInfo> versions = new Gson().fromJson(reader, token.getType());

        LATEST_YARNS.clear();
        final Map<String, List<YarnVersionInfo>> map = versions.stream()
            .distinct()
            .collect(Collectors.groupingBy(it -> it.gameVersion));
        map.keySet().forEach(it -> LATEST_YARNS.put(it, map.get(it).get(0).version));
    }

    /**
     * Update loader.
     */
    private static void updateLoader() {
        final InputStreamReader reader = getReader(LOADER_URL);
        if (reader == null) {
            return;
        }
        final TypeToken<List<LoaderVersionInfo>> token = new TypeToken<>() {
        };
        final List<LoaderVersionInfo> versions = new Gson().fromJson(reader, token.getType());

        latestLoader = versions.get(0).version;
    }

    /**
     * Update api.
     */
    private static void updateApi() {
        final InputStream stream = getStream(API_URL);
        if (stream == null) {
            return;
        }
        try {
            final var doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(stream);
            /*
             final var factory = DocumentBuilderFactory.newInstance();
             factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
             factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
             factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
             final var builder = factory.newDocumentBuilder();
             final var doc = builder.parse(stream);
            */
            final XPathExpression expr = XPathFactory.newInstance()
                .newXPath()
                .compile("/metadata/versioning/latest/text()");
            latestApi = expr.evaluate(doc);
        } catch (SAXException | XPathExpressionException | ParserConfigurationException | IOException ex) {
            TheCommander.LOGGER.error("Failed to resolve latest loader version", ex);
        }
    }

    /**
     * Gets reader.
     *
     * @param urlString the url string
     * @return InputStreamReader. reader
     */
    private static InputStreamReader getReader(final String urlString) {
        final InputStream stream = getStream(urlString);
        if (stream == null) {
            return null;
        } else {
            return new InputStreamReader(stream, StandardCharsets.UTF_8);
        }
    }

    /**
     * Gets stream.
     *
     * @param urlString the url string
     * @return InputStream. stream
     */
    private static InputStream getStream(final String urlString) {
        try {
            final var url = new URL(urlString);
            return url.openStream();
        } catch (IOException ex) {
            TheCommander.LOGGER.error("Failed to get minecraft version", ex);
            return null;
        }
    }

    /**
     * The type Yarn version info.
     *
     * @author williambl
     */
    private static class YarnVersionInfo {

        /**
         * The Game version.
         */
        public String gameVersion;

        /**
         * The Build.
         */
        public int build;

        /**
         * The Version.
         */
        public String version;
    }

    /**
     * The type Loader version info.
     *
     * @author williambl
     */
    private static class LoaderVersionInfo {

        /**
         * The Build.
         */
        public int build;

        /**
         * The Version.
         */
        public String version;
    }
}
