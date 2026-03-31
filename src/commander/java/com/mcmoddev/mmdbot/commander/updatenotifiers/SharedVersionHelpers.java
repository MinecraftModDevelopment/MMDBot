/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2026 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.commander.updatenotifiers;

import com.mcmoddev.mmdbot.commander.TheCommander;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class SharedVersionHelpers {
    private static final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10L))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    public static InputStreamReader getReader(final String urlString) {
        final InputStream stream = getStream(urlString);
        if (stream == null) {
            return null;
        } else {
            return new InputStreamReader(stream, StandardCharsets.UTF_8);
        }
    }

    @Nullable
    public static InputStream getStream(final String urlString) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .header("User-Agent", "MMDBot")
                    .timeout(Duration.ofSeconds(30L))
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            return response.body();
        } catch (IOException | InterruptedException ex) {
            TheCommander.LOGGER.error("Failed to open input stream", ex);
            return null;
        }
    }

    public static String getLatestFromMavenMetadata(String url) {
        final InputStream stream = getStream(url);
        if (stream == null) {
            return null;
        }
        try {
            final var doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(stream);
            final XPathExpression expr = XPathFactory.newInstance()
                .newXPath()
                .compile("/metadata/versioning/latest/text()");
            return expr.evaluate(doc);
        } catch (SAXException | XPathExpressionException | ParserConfigurationException | IOException ex) {
            TheCommander.LOGGER.error("Failed to resolve latest version from url '{}'", url, ex);
        }
        return null;
    }

    public static String replaceGitHubReferences(String changelog, String repo) {
        return changelog.replaceAll("\\(#(?<number>\\d+)\\)", "[(#$1)](https://github.com/" + repo + "/pull/$1)")
            .replaceAll("(?m)^ - ", "- ")
            .replaceAll("(?mi)(?<type>(?:close|fix|resolve)(?:s|d|es|ed)?) #(?<number>\\d+)", "$1 [#$2](https://github.com/" + repo + "/issues/$2)");
    }

    /**
     * The type Shared Version Info.
     *
     * @author williambl
     * @author KiriCattus
     */
    public static class SharedVersionInfo {

        /**
         * The game version.
         */
        public String gameVersion;

        /**
         * The build.
         */
        public int build;

        /**
         * The version.
         */
        public String version;
    }

    /**
     * The type Loader version info.
     *
     * @author williambl
     * @author KiriCattus
     */
    public static class LoaderVersionInfo {

        /**
         * The build.
         */
        public int build;

        /**
         * The version.
         */
        public String version;
    }
}
