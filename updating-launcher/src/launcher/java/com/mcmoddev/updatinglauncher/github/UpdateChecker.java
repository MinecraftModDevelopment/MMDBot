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
package com.mcmoddev.updatinglauncher.github;

import com.mcmoddev.updatinglauncher.Constants;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class UpdateChecker {
    /**
     * owner, repo
     */
    public static final String REQUEST_URL = "https://api.github.com/repos/%s/%s/releases/latest";
    /**
     * owner, repo, releaseId
     */
    public static final String SPECIFIC_REQUEST_URL = "https://api.github.com/repos/%s/%s/releases/%s";

    private final String owner;
    private final String repo;
    private final HttpClient httpClient;

    private Release latestFound;

    public UpdateChecker(final String owner, final String repo, final HttpClient httpClient) {
        this.owner = owner;
        this.repo = repo;
        this.httpClient = httpClient;
    }

    public boolean findNew() throws IOException, InterruptedException {
       final var old = latestFound;
       final var newRel = resolveLatestRelease();
        if (newRel == null) {
            return false;
        }
       if (old == null) {
           return true;
       }
       return newRel.id > old.id;
    }

    /**
     * Resolves the latest release.
     * @return the latest release
     */
    @Nullable
    public Release resolveLatestRelease() throws IOException, InterruptedException {
        final var uri = URI.create(REQUEST_URL.formatted(owner, repo));
        final var request = HttpRequest.newBuilder(uri)
            .GET()
            .header("accept", "application/vnd.github.v3+json")
            .build();

        final var res = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return latestFound = Constants.GSON.fromJson(res.body(), Release.class);
    }

    @Nullable
    public Release resolveRelease(int id) throws IOException, InterruptedException {
        final var uri = URI.create(SPECIFIC_REQUEST_URL.formatted(owner, repo, id));
        final var request = HttpRequest.newBuilder(uri)
            .GET()
            .header("accept", "application/vnd.github.v3+json")
            .build();

        final var res = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(res.statusCode());
        return latestFound = Constants.GSON.fromJson(res.body(), Release.class);
    }

    /**
     * Gets the latest found release. This might not always be up-to-date.
     * @return the latest found release
     */
    @Nullable
    public Release getLatestFound() {
        return latestFound;
    }

    public String getOwner() {
        return owner;
    }

    public String getRepo() {
        return repo;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }
}
