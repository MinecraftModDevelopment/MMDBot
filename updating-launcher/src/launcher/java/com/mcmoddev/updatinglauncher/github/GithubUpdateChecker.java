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
import com.mcmoddev.updatinglauncher.api.Release;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;

public class GithubUpdateChecker implements com.mcmoddev.updatinglauncher.api.UpdateChecker {
    /**
     * owner, repo
     */
    public static final String REQUEST_URL = "https://api.github.com/repos/%s/%s/releases/latest";

    private final String owner;
    private final String repo;
    private final HttpClient httpClient;
    private final Pattern jarNamePattern;

    private GithubRelease latestFound;

    public GithubUpdateChecker(final String owner, final String repo, final HttpClient httpClient, final Pattern jarNamePattern) {
        this.owner = owner;
        this.repo = repo;
        this.httpClient = httpClient;
        this.jarNamePattern = jarNamePattern;
    }

    @Override
    public boolean findNew() throws IOException, InterruptedException {
       final var old = latestFound;
       final var newRel = resolveLatestReleaseAsGithub();
        if (newRel == null) {
            return false;
        }
       if (old == null) {
           return true;
       }
       return newRel.id > old.id;
    }

    @Nullable
    public GithubRelease resolveLatestReleaseAsGithub() throws IOException, InterruptedException {
        final var uri = URI.create(REQUEST_URL.formatted(owner, repo));
        final var request = HttpRequest.newBuilder(uri)
            .GET()
            .header("accept", "application/vnd.github.v3+json")
            .build();

        final var res = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return latestFound = Constants.GSON.fromJson(res.body(), GithubRelease.class);
    }

    @Nullable
    public GithubRelease getLatestGithubFound() {
        return latestFound;
    }

    @Override
    public @org.jetbrains.annotations.Nullable Release resolveLatestRelease() throws IOException, InterruptedException {
        return resolveReleaseFromGh(resolveLatestReleaseAsGithub());
    }

    @javax.annotation.Nullable
    @Override
    public Release getLatestFound() {
        return resolveReleaseFromGh(getLatestGithubFound());
    }

    @javax.annotation.Nullable
    public GithubRelease getGhReleaseByTagName(final String tag) throws InterruptedException, IOException {
        final var uri = URI.create("https://api.github.com/repos/%s/%s/releases/tags/%s".formatted(owner, repo, tag));
        final var request = HttpRequest.newBuilder(uri)
            .GET()
            .header("accept", "application/vnd.github.v3+json")
            .build();

        final var res = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() == 404) {
            return null;
        }
        return Constants.GSON.fromJson(res.body(), GithubRelease.class);
    }

    @Override
    public @org.jetbrains.annotations.Nullable Release getReleaseByTagName(final String tagName) throws IOException, InterruptedException {
        return resolveReleaseFromGh(getGhReleaseByTagName(tagName));
    }

    public Release resolveReleaseFromGh(@Nullable GithubRelease release) {
        if (release == null) return null;
        return release.assets.stream()
            .filter(asst -> asst.name.endsWith(".jar"))
            .filter(p -> jarNamePattern.matcher(p.name).find())
            .findFirst()
            .map(a -> new Release(release.name, a.browserDownloadUrl))
            .orElse(null);
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }
}
