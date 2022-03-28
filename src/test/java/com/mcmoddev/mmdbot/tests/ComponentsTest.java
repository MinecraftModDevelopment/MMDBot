package com.mcmoddev.mmdbot.tests;

import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.component.ComponentStorage;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sqlite.SQLiteDataSource;

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
    static void setupStorage() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:./run/tests/data.db");

        final Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/test")
            .load();
        flyway.migrate();

        storage = new ComponentStorage(Jdbi.create(dataSource), "components");
    }

}
