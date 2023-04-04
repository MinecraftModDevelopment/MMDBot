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
package com.mcmoddev.mmdbot.commander.updatenotifiers.blockbench;

import com.google.gson.reflect.TypeToken;
import com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifiers;
import com.mcmoddev.mmdbot.commander.util.StringSerializer;
import com.mcmoddev.mmdbot.core.util.Constants;
import com.mcmoddev.mmdbot.core.util.Utils;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class BlockbenchVersionHelper {
    /**
     * {@return the latest Blockbench version}
     */
    @Nullable
    @SneakyThrows(InterruptedException.class)
    public static GithubRelease getLatest(final Marker loggingMarker) throws IOException {
        final HttpResponse<List<GithubRelease>> response = Constants.HTTP_CLIENT.send(HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/repos/JannisX11/blockbench/releases"))
            .header("Accept", "application/json")
            .build(), Utils.ofGson(StringSerializer.RECORD_GSON, new TypeToken<>() {}));

        if (response.statusCode() != 200) {
            UpdateNotifiers.LOGGER.error(loggingMarker, "Server replied with non-200 status code {}.", response.statusCode());
            return null;
        }

        final List<GithubRelease> releases = response.body();
        return releases.isEmpty() ? null : releases.get(0); // First is the latest
    }
}
