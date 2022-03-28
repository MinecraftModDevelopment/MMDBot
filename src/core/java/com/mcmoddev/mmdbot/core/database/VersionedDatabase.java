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
package com.mcmoddev.mmdbot.core.database;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Class used for JSON Databases which have a schema version for migration purposes.
 *
 * @param <T> the type of the stored data
 */
public final class VersionedDatabase<T> {
    private T data;
    private int schemaVersion;

    private VersionedDatabase(final int schemaVersion, final T data) {
        this.data = data;
        this.schemaVersion = schemaVersion;
    }

    /**
     * Creates an in-memory database.
     *
     * @param data          the data to store
     * @param schemaVersion the version of the database
     * @param <T>           the type of the data
     * @return the database
     */
    public static <T> VersionedDatabase<T> inMemory(int schemaVersion, T data) {
        return new VersionedDatabase<>(schemaVersion, data);
    }

    /**
     * Reads a database from a file.
     *
     * @param gson     the gson to use for reading
     * @param filePath the path of the file to read
     * @param dataType the type of the data the file contains
     * @param <T>      the type of the data
     * @return the database
     * @throws IOException if an exception occurred reading the file
     */
    public static <T> VersionedDatabase<T> fromFile(Gson gson, Path filePath, java.lang.reflect.Type dataType) throws IOException {
        return fromFile(gson, filePath, dataType, 0, null);
    }

    /**
     * Reads a database from a file.
     *
     * @param gson                 the gson to use for reading
     * @param filePath             the path of the file to read
     * @param dataType             the type of the data the file contains
     * @param defaultSchemaVersion the schema version which the database will have if the json is empty
     * @param defaultValue         the value of the database if the json is empty
     * @param <T>                  the type of the data
     * @return the database
     * @throws IOException if an exception occurred reading the file
     */
    public static <T> VersionedDatabase<T> fromFile(Gson gson, Path filePath, java.lang.reflect.Type dataType, int defaultSchemaVersion, T defaultValue) throws IOException {
        try (final var reader = new FileReader(filePath.toFile())) {
            final var json = gson.fromJson(reader, JsonObject.class);
            if (json == null || !json.has(DATA_TAG)) {
                return new VersionedDatabase<>(defaultSchemaVersion, defaultValue);
            }
            return new VersionedDatabase<>(json.get(SCHEMA_VERSION_TAG).getAsInt(), gson.fromJson(json.get(DATA_TAG), dataType));
        }
    }

    public static final String SCHEMA_VERSION_TAG = "schemaVersion";
    public static final String DATA_TAG = "data";

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public JsonObject toJson(Gson gson) {
        final var obj = new JsonObject();
        obj.addProperty(SCHEMA_VERSION_TAG, schemaVersion);
        obj.add(DATA_TAG, gson.fromJson(gson.toJson(data), JsonElement.class));
        return obj;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }
}
