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
package com.mcmoddev.mmdbot.commander.updatenotifiers.forge;

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
public final class ForgeVersionHelper extends SharedVersionHelpers {

    private static final String METADATA_URL = "https://maven.minecraftforge.net/releases/net/minecraftforge/forge/maven-metadata.xml";

    public static Map<String, String> getForgeVersions() {
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

                final String mcVersion = version.split("-")[0];
                versions.put(mcVersion, version);
            }
        } catch (SAXException | XPathExpressionException | ParserConfigurationException | IOException ex) {
            TheCommander.LOGGER.error("Failed to resolve latest version from Forge metadata URL", ex);
        }

        return versions;
    }
}
