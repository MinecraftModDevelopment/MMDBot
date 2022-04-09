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
import com.mcmoddev.mmdbot.core.util.Constants;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class used for JSON Databases which have a schema version for migration purposes.
 *
 * @param <T> the type of the stored data
 */
@Slf4j
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
        try (final var reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            final var json = gson.fromJson(reader, JsonObject.class);
            if (json == null || !json.has(DATA_TAG)) {
                return new VersionedDatabase<>(defaultSchemaVersion, defaultValue);
            }
            return new VersionedDatabase<>(json.get(SCHEMA_VERSION_TAG).getAsInt(), gson.fromJson(json.get(DATA_TAG), dataType));
        }
    }

    /**
     * Reads a database from a file. <br>
     * The {@link JsonOps} used is <strong>not</strong> compressed.
     *
     * @param filePath             the path of the file to read
     * @param codec                the codec to use for deserializing the data
     * @param defaultSchemaVersion the schema version which the database will have if the json is empty
     * @param defaultValue         the value of the database if the json is empty
     * @param <T>                  the type of the data
     * @return the database, as a {@link com.mojang.serialization.DataResult}. This result will be an error one, if an IO exception occurred
     */
    public static <T> DataResult<VersionedDatabase<T>> fromFile(Path filePath, Codec<T> codec, int defaultSchemaVersion, T defaultValue) {
        return fromFile(filePath, codec, false, defaultSchemaVersion, defaultValue);
    }

    /**
     * Reads a database from a file.
     *
     * @param filePath             the path of the file to read
     * @param codec                the codec to use for deserializing the data
     * @param compressed           if the {@link JsonOps} used for deserializing the data is a compressed one
     * @param defaultSchemaVersion the schema version which the database will have if the json is empty
     * @param defaultValue         the value of the database if the json is empty
     * @param <T>                  the type of the data
     * @return the database, as a {@link com.mojang.serialization.DataResult}. This result will be an error one, if an IO exception occurred
     */
    public static <T> DataResult<VersionedDatabase<T>> fromFile(Path filePath, Codec<T> codec, boolean compressed, int defaultSchemaVersion, T defaultValue) {

        try (final var reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            final var json = Constants.Gsons.NO_PRETTY_PRINTING.fromJson(reader, JsonObject.class);
            if (json == null || !json.has(DATA_TAG)) {
                return DataResult.error("No data tag is present.", new VersionedDatabase<>(defaultSchemaVersion, defaultValue), Lifecycle.experimental());
            }
            return codec(codec, defaultSchemaVersion, defaultValue).decode(compressed ? JsonOps.COMPRESSED : JsonOps.INSTANCE, json).map(Pair::getFirst);
        } catch (IOException e) {
            return DataResult.error(e.getMessage(), new VersionedDatabase<>(defaultSchemaVersion, defaultValue));
        }
    }

    /**
     * Creates a codec for a {@link VersionedDatabase}.
     *
     * @param dataCodec the codec to use for the data
     * @param <T>       the type of the data
     * @return the codec
     */
    public static <T> Codec<VersionedDatabase<T>> codec(Codec<T> dataCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf(SCHEMA_VERSION_TAG).forGetter(VersionedDatabase::getSchemaVersion),
            dataCodec.fieldOf(DATA_TAG).forGetter(VersionedDatabase::getData)
        ).apply(instance, VersionedDatabase::new));
    }

    /**
     * Creates a codec for a {@link VersionedDatabase}.
     *
     * @param dataCodec            the codec to use for the data
     * @param defaultSchemaVersion the default version of the schema
     * @param defaultData          the default data
     * @param <T>                  the type of the data
     * @return the codec
     */
    public static <T> Codec<VersionedDatabase<T>> codec(Codec<T> dataCodec, int defaultSchemaVersion, T defaultData) {
        return RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf(SCHEMA_VERSION_TAG, defaultSchemaVersion).forGetter(VersionedDatabase::getSchemaVersion),
            dataCodec.optionalFieldOf(DATA_TAG, defaultData).forGetter(VersionedDatabase::getData)
        ).apply(instance, VersionedDatabase::new));
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

    public DataResult<JsonObject> toJson(final Codec<T> codec) {
        return toJson(codec, false);
    }

    public DataResult<JsonObject> toJson(final Codec<T> codec, final boolean compressed) {
        final var obj = new JsonObject();
        obj.addProperty(SCHEMA_VERSION_TAG, schemaVersion);
        return codec.encodeStart(compressed ? JsonOps.COMPRESSED : JsonOps.INSTANCE, data)
            .map(elem -> {
                obj.add(DATA_TAG, elem);
                return obj;
            });
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }
}
