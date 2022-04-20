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
package com.mcmoddev.mmdbot.core.database;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.NonNull;

/**
 * A cluster of migrators which migrate from different versions to the same version.
 */
@SuppressWarnings("ClassCanBeRecord")
public final class MigratorCluster implements DataMigrator {
    private final Int2ObjectMap<DataMigrator> migrators;

    private MigratorCluster(final Int2ObjectMap<DataMigrator> migrators) {
        this.migrators = migrators;
    }

    @Override
    @SuppressWarnings("Duplicates")
    public JsonElement migrate(int current, final int target, final JsonElement data) {
        var migrator = migrators.get(current);
        if (migrator != null) {
            final var j = migrator.migrate(current, target, data);
            if (j != null) {
                return j;
            }
        }
        // Try to find the one closest to the current version, as they might be compatible
        while (current > 0) {
            current--;
            migrator = migrators.get(current);
            if (migrator != null) {
                final var j = migrator.migrate(current, target, data);
                if (j != null) {
                    return current == 1 ? j : migrate(current, target, j);
                }
            }
        }
        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Int2ObjectMap<DataMigrator> migrators = new Int2ObjectOpenHashMap<>();

        /**
         * Adds a migrator to the cluster, which migrates from the {@code currentVersion}.
         *
         * @param currentVersion the origin version to migrate from
         * @param migrator       the migrator
         * @return the builder instance
         */
        public Builder addMigrator(final int currentVersion, @NonNull final DataMigrator migrator) {
            this.migrators.put(currentVersion, migrator);
            return this;
        }

        public MigratorCluster build() {
            return new MigratorCluster(migrators);
        }
    }
}
