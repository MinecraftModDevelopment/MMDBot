package com.mcmoddev.mmdbot.updatenotifiers.fabric;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mcmoddev.mmdbot.MMDBot;
import org.w3c.dom.Document;
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
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class FabricVersionHelper {

    private static final String YARN_URL = "https://meta.fabricmc.net/v2/versions/yarn";
    private static final String LOADER_URL = "https://meta.fabricmc.net/v2/versions/loader";
    private static final String API_URL = "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml";

    private static final Map<String, String> latestYarns = new HashMap<>();
    private static final Duration timeUntilOutdated = Duration.ofMinutes(20);
    private static String latestLoader;
    private static String latestApi;
    private static Instant lastUpdated;

    static {
        update();
    }

    public static String getLatestYarn(String mcVersion) {
        if (latestYarns.isEmpty() || isOutdated())
            update();
        return latestYarns.get(mcVersion);
    }

    public static String getLatestLoader() {
        if (latestLoader == null || isOutdated())
            update();
        return latestLoader;
    }

    public static String getLatestApi() {
        if (latestApi == null || isOutdated())
            update();
        return latestApi;
    }

    private static boolean isOutdated() {
        return lastUpdated.plus(timeUntilOutdated).isBefore(Instant.now());
    }

    public static void update() {
        updateYarn();
        updateLoader();
        updateApi();
        lastUpdated = Instant.now();
    }

    private static void updateYarn() {
        InputStreamReader reader = getReader(YARN_URL);
        if (reader == null)
            return;
        TypeToken<List<YarnVersionInfo>> token = new TypeToken<List<YarnVersionInfo>>() {
        };
        List<YarnVersionInfo> versions = new Gson().fromJson(reader, token.getType());

        latestYarns.clear();
        Map<String, List<YarnVersionInfo>> map = versions.stream()
            .distinct()
            .collect(Collectors.groupingBy(it -> it.gameVersion));
        map.keySet().forEach(it -> latestYarns.put(it, map.get(it).get(0).version));
    }

    private static void updateLoader() {
        InputStreamReader reader = getReader(LOADER_URL);
        if (reader == null)
            return;
        TypeToken<List<LoaderVersionInfo>> token = new TypeToken<List<LoaderVersionInfo>>() {
        };
        List<LoaderVersionInfo> versions = new Gson().fromJson(reader, token.getType());

        latestLoader = versions.get(0).version;
    }

    private static void updateApi() {
        InputStream stream = getStream(API_URL);
        if (stream == null)
            return;
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(stream);
            XPathExpression expr = XPathFactory.newInstance()
                .newXPath()
                .compile("/metadata/versioning/latest/text()");
            latestApi = expr.evaluate(doc);
        } catch (SAXException | XPathExpressionException | ParserConfigurationException | IOException e) {
            MMDBot.LOGGER.error("Failed to resolve latest loader version", e);
        }
    }

    private static InputStreamReader getReader(String urlString) {
        InputStream stream = getStream(urlString);
        if (stream == null) {
            return null;
        } else {
            return new InputStreamReader(stream);
        }
    }

    private static InputStream getStream(String urlString) {
        try {
            URL url = new URL(urlString);
            return url.openStream();
        } catch (IOException e) {
            MMDBot.LOGGER.error("Failed to get minecraft version", e);
            return null;
        }
    }

    private static class YarnVersionInfo {
        public String gameVersion;
        public String separator;
        public int build;
        public String maven;
        public String version;
        public boolean stable;
    }

    private static class LoaderVersionInfo {
        public String separator;
        public int build;
        public String maven;
        public String version;
        public boolean stable;
    }
}
