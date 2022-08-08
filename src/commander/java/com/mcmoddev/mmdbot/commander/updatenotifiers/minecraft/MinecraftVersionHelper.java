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
package com.mcmoddev.mmdbot.commander.updatenotifiers.minecraft;

import com.mcmoddev.mmdbot.core.util.Constants;
import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

/**
 * The type Minecraft version helper.
 *
 * @author unknown
 * @author matyrobbrt
 */
@UtilityClass
public final class MinecraftVersionHelper {
    public static final String API_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    /**
     * Gets the latest MC version.
     */
    @Nullable
    public static String getLatest() {
        final var meta = getMeta();
        if (meta == null) return null;
        return meta.latest.snapshot;
    }

    /**
     * Gets the latest stable MC version.
     */
    @Nullable
    public static String getLatestStable() {
        final var meta = getMeta();
        if (meta == null) return null;
        return meta.latest.release;
    }

    @Nullable
    public static PistonMeta getMeta() {
        try (final var reader = new InputStreamReader(new URL(API_URL).openStream())) {
            return Constants.Gsons.GSON.fromJson(reader, PistonMeta.class);
        } catch (IOException ignored) {
            return null;
        }
    }

    public static class PistonMeta {
        public VersionsInfo latest;
        public List<VersionInfo> versions;
    }

    public record VersionsInfo(String release, String snapshot) {
    }

    public record VersionInfo(String id, String type) {
    }
}
