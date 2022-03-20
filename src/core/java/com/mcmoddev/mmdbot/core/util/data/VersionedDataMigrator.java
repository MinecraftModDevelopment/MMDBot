package com.mcmoddev.mmdbot.core.util.data;

import com.google.gson.JsonElement;
import com.mcmoddev.mmdbot.core.util.Constants;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Migrator used for the migration of {@link VersionedDatabase}.
 *
 * @author matyrobbrt
 */
@SuppressWarnings("ClassCanBeRecord")
public class VersionedDataMigrator implements DataMigrator {

    private final Int2ObjectMap<MigratorCluster> clusters;
    @NonNull
    private final Function<JsonElement, IntObjectPair<JsonElement>> whenNoVersion;
    @NonNull
    private final Supplier<IntObjectPair<JsonElement>> onFileNotFound;

    private VersionedDataMigrator(final Int2ObjectMap<MigratorCluster> clusters, @Nullable final Function<JsonElement, IntObjectPair<JsonElement>> whenNoVersion, final @Nullable Supplier<IntObjectPair<JsonElement>> onFileNotFound) {
        this.clusters = clusters;
        this.whenNoVersion = whenNoVersion == null ? j -> new IntObjectImmutablePair<>(getLatestTargetVersion(), j) : whenNoVersion;
        this.onFileNotFound = onFileNotFound == null ? () -> new IntObjectImmutablePair<>(getLatestTargetVersion(), null) : onFileNotFound;
    }

    public int getLatestTargetVersion() {
        return clusters.keySet().intStream().max().orElse(1);
    }

    /**
     * Attempts to migrate the data from the file to the target version.
     * @param target the target version
     * @param file the file to migrate
     */
    public void migrate(final int target, @NonNull final Path file) throws IOException {
        if (!Files.exists(file)) {
            Files.createDirectories(file.getParent());
            Files.createFile(file);
            try (final var w = new FileWriter(file.toFile())) {
                final var notFound = onFileNotFound.get();
                final var db = VersionedDatabase.inMemory(notFound.firstInt(), notFound.second());
                Constants.Gsons.NO_PRETTY_PRINTING.toJson(db.toJson(Constants.Gsons.NO_PRETTY_PRINTING), w);
                return;
            }
        }
        try (final var reader = new BufferedReader(new FileReader(file.toFile()))) {
            final var json = Constants.Gsons.NO_PRETTY_PRINTING.fromJson(reader, JsonElement.class);
            if (!json.isJsonObject() || !json.getAsJsonObject().has(VersionedDatabase.SCHEMA_VERSION_TAG)) {
                reader.close();
                try (final var w = new BufferedWriter(new FileWriter(file.toFile()))) {
                    final var noVersion = whenNoVersion.apply(json);
                    final var db = VersionedDatabase.inMemory(noVersion.firstInt(), noVersion.second());
                    Constants.Gsons.NO_PRETTY_PRINTING.toJson(db.toJson(Constants.Gsons.NO_PRETTY_PRINTING), w);
                }
            } else {
                final var db = VersionedDatabase.<JsonElement>fromFile(Constants.Gsons.NO_PRETTY_PRINTING, file, JsonElement.class);
                if (db.getSchemaVersion() == target) {
                    return;
                }
                final var newData = migrate(db.getSchemaVersion(), target, db.getData());
                if (newData != null) {
                    db.setData(newData);
                    db.setSchemaVersion(target);
                    try (final var w = new BufferedWriter(new FileWriter(file.toFile()))) {
                        Constants.Gsons.NO_PRETTY_PRINTING.toJson(db.toJson(Constants.Gsons.NO_PRETTY_PRINTING), w);
                    }
                } else {
                    throw new UnsupportedEncodingException("Could not migrate file '%s' from version '%s' to version '%s'.".formatted(
                        file, db.getSchemaVersion(), target
                    ));
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    @SuppressWarnings("Duplicates")
    public JsonElement migrate(int current, int target, final JsonElement data) {
        var migrator = clusters.get(target);
        if (migrator != null) {
            final var j = migrator.migrate(current, target, data);
            if (j != null) {
                return j;
            }
        }
        // Try to find the one closest to the target.
        while (target > 0) {
            target--;
            migrator = clusters.get(target);
            if (migrator != null) {
                final var j = migrator.migrate(current, target, data);
                if (j != null) {
                    return target == 1 ? j : migrate(current, target, j);
                }
            }
        }
        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Int2ObjectMap<MigratorCluster> clusters = new Int2ObjectOpenHashMap<>();
        private Function<JsonElement, IntObjectPair<JsonElement>> whenNoVersion = null;
        private Supplier<IntObjectPair<JsonElement>> onFileNotFound = null;

        /**
         * Adds a cluster of migrators for migration to the {@code targetVersion}.
         *
         * @param targetVersion the target version of the cluster.
         * @param cluster       the cluster
         * @return the builder instance
         */
        public Builder addCluster(final int targetVersion, @NonNull final MigratorCluster cluster) {
            this.clusters.put(targetVersion, cluster);
            return this;
        }

        /**
         * Sets the migrator used for when there is no version.
         *
         * @param whenNoVersion the migrator for when there isn't a version. It takes in the data as input, and returns
         *                      a pair consisting of the new version and the migrated data
         * @return the builder instance
         */
        public Builder whenNoVersion(@NonNull final Function<JsonElement, IntObjectPair<JsonElement>> whenNoVersion) {
            this.whenNoVersion = whenNoVersion;
            return this;
        }

        /**
         * Sets the migrator used for when the file to migrate doesn't exist.
         *
         * @param onFileNotFound the migrator used for when the file to migrate doesn't exist. It's a supplier
         *                       which returns a pair consisting of the schema version and the data
         * @return the builder instance
         */
        public Builder onFileNotFound(@NonNull final Supplier<IntObjectPair<JsonElement>> onFileNotFound) {
            this.onFileNotFound = onFileNotFound;
            return this;
        }

        public VersionedDataMigrator build() {
            return new VersionedDataMigrator(clusters, whenNoVersion, onFileNotFound);
        }
    }
}
