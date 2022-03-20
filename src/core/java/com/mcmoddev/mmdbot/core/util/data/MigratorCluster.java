package com.mcmoddev.mmdbot.core.util.data;

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
         * @param migrator    the migrator
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
