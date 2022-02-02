/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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
package com.mcmoddev.mmdbot.gist;

import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a gist which was got from an HTTP request
 *
 * @author matyrobbrt
 */
public record ExistingGist(JsonObject json, String url, String forksUrl, String commitsUrl,
                           String id, boolean isPublic, String commentsUrl, int comments,
                           String htmlUrl, String gitPullUrl, String gitPushUrl, String createdAt,
                           String updatedAt, Map<String, File> files, @Nullable String description) {

    public static ExistingGist fromJson(final JsonObject json) {
        var url = getFromJson(json, "url");
        var id = getFromJson(json, "id");
        var isPublic = json.get("public").getAsBoolean();
        var commentsUrl = getFromJson(json, "comments_url");
        var comments = json.get("comments").getAsInt();
        var htmlUrl = getFromJson(json, "html_url");
        var gitPullUrl = getFromJson(json, "git_pull_url");
        var gitPushUrl = getFromJson(json, "git_push_url");
        var createdAt = getFromJson(json, "created_at");
        var updatedAt = getFromJson(json, "updated_at");
        var forksUrl = getFromJson(json, "forks_url");
        var commitsUrl = getFromJson(json, "commits_url");
        var description = getFromJson(json, "description");

        var files = new LinkedHashMap<String, File>();

        final var filesJson = json.get("files").getAsJsonObject();
        for (final String key : filesJson.keySet()) {
            files.put(key, File.fromJson(filesJson.get(key).getAsJsonObject()));
        }
        return new ExistingGist(json, url, forksUrl, commitsUrl, id, isPublic, commentsUrl, comments, htmlUrl, gitPullUrl,
            gitPushUrl, createdAt, updatedAt, files, description);
    }

    private static String getFromJson(final JsonObject json, final String key) {
        return json.get(key).isJsonPrimitive() && json.get(key).getAsJsonPrimitive().isString() ? json.get(key).getAsString() : null;
    }

    @Override
    public String toString() {
        return json.toString();
    }

    public record File(int size, String url, String type, boolean isTruncated) {

        public static File fromJson(final JsonObject json) {
            final var size = json.get("size").getAsInt();
            final var url = json.get("raw_url").getAsString();
            final var type = json.get("type").getAsString();
            final var truncated = json.has("truncated") && json.get("truncated").getAsBoolean();
            return new File(size, url, type, truncated);
        }

    }

}
