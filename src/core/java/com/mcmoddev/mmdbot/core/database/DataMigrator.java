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
