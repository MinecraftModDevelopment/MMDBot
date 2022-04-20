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
package com.mcmoddev.mmdbot.commander.util.mc;

import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.core.util.Constants;
import com.mcmoddev.mmdbot.core.util.TaskScheduler;
import com.mcmoddev.mmdbot.core.util.event.OneTimeEventListener;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@UtilityClass
public class MCVersions {

    private static Map<String, MinecraftVersion> versionsById = new HashMap<>();

    private static final HttpRequest REQUEST = HttpRequest.newBuilder(URI.create("https://launchermeta.mojang.com/mc/game/version_manifest.json"))
        .GET()
        .build();
    public static final OneTimeEventListener<TaskScheduler.CollectTasksEvent> REFRESHER_TASK = new OneTimeEventListener<>(event -> {
        event.addTask(() -> {
            try {
                final var response = Constants.HTTP_CLIENT.send(REQUEST, HttpResponse.BodyHandlers.ofString());
                final var manifest = Constants.Gsons.GSON.fromJson(response.body(), VersionManifest.class);
                versionsById = manifest.versions().stream()
                    .collect(Collectors.toMap(MinecraftVersion::id, m -> m));
            } catch (IOException e) {
                TheCommander.LOGGER.error("Exception refreshing minecraft versions: ", e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 0, 1, TimeUnit.HOURS);
    });

    @Nullable
    public static MinecraftVersion getVersionInfo(@NonNull final String version) {
        return versionsById.get(version);
    }

    public static Set<String> getKnownVersions() {
        return versionsById.keySet();
    }
}
