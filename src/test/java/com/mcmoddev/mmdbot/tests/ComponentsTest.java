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
package com.mcmoddev.mmdbot.tests;

import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.component.ComponentStorage;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sqlite.SQLiteDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class ComponentsTest {
    static ComponentStorage storage;

    @Test
    void testFeatureIdIsSame() {
        final var id = UUID.randomUUID();
        storage.insertComponent(new Component("test_feature", id, List.of("hi")));
        final var newComponent = storage.getComponent(id);
        assert newComponent.isPresent();
        Assertions.assertEquals(newComponent.get().featureId(), "test_feature");
    }

    @Test
    void testUpdateArgsWorks() {
        final var id = UUID.randomUUID();
        storage.insertComponent(new Component("feature", id, List.of("12")));
        final var newArgs = List.of("hello");
        storage.updateArguments(id, newArgs);
        final var newComponent = storage.getComponent(id);
        assert newComponent.isPresent();
        Assertions.assertEquals(newComponent.get().arguments(), newArgs);
    }

    @BeforeAll
    static void setupStorage() throws IOException {
        final var path = Path.of("run").resolve("tests").resolve("data.db");
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        }
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + path);

        final Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/test")
            .load();
        flyway.migrate();

        storage = new ComponentStorage(Jdbi.create(dataSource), "components");
    }

}
