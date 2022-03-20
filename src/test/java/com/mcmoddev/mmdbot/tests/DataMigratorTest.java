package com.mcmoddev.mmdbot.tests;

import com.google.gson.JsonObject;
import com.mcmoddev.mmdbot.core.util.data.VersionedDataMigrator;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.Path;

@UtilityClass
public class DataMigratorTest {

    public static final VersionedDataMigrator MIGRATOR = VersionedDataMigrator.builder()
        .onFileNotFound(() -> new IntObjectImmutablePair<>(10, new JsonObject()))
        .build();

    public static void main(String[] args) throws IOException {
        final var path = Path.of("run").resolve("tests").resolve("test_migration.json");
        MIGRATOR.migrate(10, path);
    }
}
