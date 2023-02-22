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
package com.mcmoddev.mmdbot.core.util.gist;

import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a gist which is ready to be made using an HTTP request.
 *
 * @author matyrobbrt
 */
public class Gist {

    private final String description;
    private final boolean isPublic;
    private final Map<String, JsonObject> files = new LinkedHashMap<>();

    public Gist(final String description, final boolean isPublic) {
        this.description = description;
        this.isPublic = isPublic;
    }

    public Gist(final String description, final boolean isPublic, final String filename, final String content) {
        this.description = description;
        this.isPublic = isPublic;

        final JsonObject fileobj = new JsonObject();
        fileobj.addProperty("content", content);

        files.put(filename, fileobj);
    }

    public Gist(final String description, final boolean isPublic, final File file) throws IOException {
        this.description = description;
        this.isPublic = isPublic;

        final JsonObject fileobj = new JsonObject();
        fileobj.addProperty("content", GistUtils.readFile(file));

        files.put(file.getName(), fileobj);
    }

    public Gist addFile(final String filename, final String content) {
        final JsonObject fileobj = new JsonObject();
        fileobj.addProperty("content", content);

        files.put(filename, fileobj);
        return this;
    }

    public Gist addFile(final File file) throws IOException {
        final JsonObject fileobj = new JsonObject();
        fileobj.addProperty("content", GistUtils.readFile(file));

        files.put(file.getName(), fileobj);
        return this;
    }

    @Override
    public String toString() {
        JsonObject gistobj = new JsonObject();
        JsonObject fileobj = new JsonObject();

        gistobj.addProperty("public", isPublic);
        gistobj.addProperty("description", description);

        for (final var entry : files.entrySet()) {
            fileobj.add(entry.getKey(), entry.getValue());
        }

        gistobj.add("files", fileobj);

        return gistobj.toString();
    }

}
