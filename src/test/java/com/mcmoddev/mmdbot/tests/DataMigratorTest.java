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
package com.mcmoddev.mmdbot.tests;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mcmoddev.mmdbot.core.database.MigratorCluster;
import com.mcmoddev.mmdbot.core.database.VersionedDataMigrator;
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
