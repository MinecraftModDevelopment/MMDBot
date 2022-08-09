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
package com.mcmoddev.mmdbot.commander.updatenotifiers.parchment;

import com.mcmoddev.mmdbot.commander.updatenotifiers.minecraft.MinecraftVersionHelper;
import com.mcmoddev.mmdbot.core.util.SemVer;
import lombok.experimental.UtilityClass;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for Parchment versions.
 *
 * @author matyrobbrt
 */
@UtilityClass
public final class ParchmentVersionHelper {
    public static final String METADATA_URL = "https://maven.parchmentmc.org/org/parchmentmc/data/parchment-%s/maven-metadata.xml";
    public static final SemVer INITIAL_VERSION = SemVer.from("1.16.5");

    public static Map<String, String> byMcReleases() {
        final var parser = DocumentBuilderFactory.newInstance();
        final var meta = MinecraftVersionHelper.getMeta();
        if (meta == null) return Map.of();
        final Map<String, String> map = new HashMap<>();
        meta.versions.stream()
            .filter(it -> it.type().equals("release"))
            .map(v -> SemVer.from(v.id()))
            .filter(v -> v.compareTo(INITIAL_VERSION) >= 0)
            .forEach(v -> {
                try (final var is = new URL(METADATA_URL.formatted(v)).openStream()) {
                    final var xml = parser.newDocumentBuilder().parse(is);
                    xml.getDocumentElement().normalize();
                    final var latestVersion = ((Element) ((Element) (xml.getElementsByTagName("metadata").item(0)))
                        .getElementsByTagName("versioning").item(0))
                        .getElementsByTagName("release").item(0)
                        .getTextContent();
                    map.put(v.toString(), latestVersion);
                } catch (Exception ignored) {
                }
            });
        return map;
    }

    public static ParchmentVersion newest(Map<String, String> map) {
        if (map.isEmpty()) return null;
        return map.entrySet().stream()
            .max(Comparator.comparing(it -> dateFromParchment(it.getValue())))
            .map(it -> new ParchmentVersion(it.getKey(), it.getValue()))
            .orElse(null);
    }

    public static Date dateFromParchment(String parchmentRelease) {
        final var split = parchmentRelease.split("\\.");
        // noinspection deprecation,MagicConstant
        return new Date(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    }

    public record ParchmentVersion(String mcVersion, String parchmentVersion) {
        public Date getDate() {
            return dateFromParchment(parchmentVersion());
        }
    }

}
