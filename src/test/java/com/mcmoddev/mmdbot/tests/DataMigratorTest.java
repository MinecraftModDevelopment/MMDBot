package com.mcmoddev.mmdbot.tests;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mcmoddev.mmdbot.core.util.data.MigratorCluster;
import com.mcmoddev.mmdbot.core.util.data.VersionedDataMigrator;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.Path;

@UtilityClass
public class DataMigratorTest {

    public static final VersionedDataMigrator MIGRATOR = VersionedDataMigrator.builder()
        .onFileNotFound(() -> new IntObjectImmutablePair<>(9, new JsonPrimitive(12)))
        .addCluster(10, MigratorCluster.builder()
            .addMigrator(9, (c, t, json) -> {
                final var j = new JsonObject();
                j.add("theData", json);
                j.addProperty("migrationTo", t);
                return j;
            })
            .build())
        .build();

    public static void main(String[] args) throws IOException {
        final var path = Path.of("run").resolve("tests").resolve("test_migration.json");
        MIGRATOR.migrate(10, path);
    }
}
