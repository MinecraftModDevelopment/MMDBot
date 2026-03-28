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
package com.mcmoddev.mmdbot.commander.updatenotifiers.neoforge;

import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.updatenotifiers.SharedVersionHelpers;
import lombok.experimental.UtilityClass;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@UtilityClass
public final class NeoForgeVersionHelper extends SharedVersionHelpers {
    private static final String METADATA_URL = "https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml";

    public static Map<String, String> getNeoForgeVersions() {
        final LinkedHashMap<String, String> versions = new LinkedHashMap<>();

        final InputStream stream = getStream(METADATA_URL);
        if (stream == null) {
            return versions;
        }
        try {
            final var doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(stream);
            final XPathExpression expr = XPathFactory.newInstance()
                .newXPath()
                .compile("/metadata/versioning/versions/version");
            final NodeList versionsNode = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < versionsNode.getLength(); i++) {
                final String version = versionsNode.item(i).getTextContent();

                final String[] parts = version.split("-")[0].split("\\.");
                if (parts.length < 2) {
                    continue; // Skip malformed versions
                }

                final String majorStr = parts[0];
                final String minorStr = parts[1];
                final int major;
                try {
                    major = Integer.parseInt(majorStr);
                } catch (NumberFormatException e) {
                    continue; // Skip malformed versions
                }

                // Mojang's new versioning scheme (e.g., '26.1') replaces the legacy '1.x.y' scheme.
                // We can assume the legacy scheme will not reach '1.25', so we use a threshold
                // to determine whether to prepend the '1.'.
                final String mcVersion;
                if (major <= 26) {
                    // Legacy format: NeoForge '20.4.x' maps to Minecraft '1.20.4'
                    mcVersion = "1." + majorStr + "." + minorStr;
                } else {
                    // New format: NeoForge '26.1.x' maps to Minecraft '26.1'
                    mcVersion = majorStr + "." + minorStr;
                }
                versions.put(mcVersion, version);
            }
        } catch (SAXException | XPathExpressionException | ParserConfigurationException | IOException ex) {
            TheCommander.LOGGER.error("Failed to resolve latest version from NeoForge metadata URL", ex);
        }

        return versions;
    }
}
