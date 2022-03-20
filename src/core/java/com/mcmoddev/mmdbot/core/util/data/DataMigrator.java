package com.mcmoddev.mmdbot.core.util.data;

import com.google.gson.JsonElement;

import javax.annotation.Nullable;

/**
 * Migrates the data from an element and converts it to another.
 */
@FunctionalInterface
public interface DataMigrator {

    /**
     * Migrates data.
     *
     * @param currentVersion the version of the current data
     * @param targetVersion  the version of the migrated data
     * @param data           the data to migrate
     * @return the migrated data. {@code null} if the data cannot be migrated.
     */
    @Nullable
    JsonElement migrate(int currentVersion, int targetVersion, JsonElement data);

}
