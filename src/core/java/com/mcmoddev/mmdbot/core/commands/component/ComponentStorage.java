package com.mcmoddev.mmdbot.core.commands.component;

import com.google.common.reflect.TypeToken;
import com.mcmoddev.mmdbot.core.database.jdbi.JdbiFactories;
import com.mcmoddev.mmdbot.core.util.Constants;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.customizer.TimestampedConfig;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ComponentStorage {
    public static final String FEATURE_ROW_NAME = "feature";
    public static final String ID_ROW_NAME = "id";
    public static final String ARGUMENTS_ROW_NAME = "arguments";
    public static final String LIFESPAN_ROW_NAME = "lifespan";
    public static final String LAST_USED_ROW_NAME = "last_used";

    private final Jdbi jdbi;
    private final String tableName;

    public ComponentStorage(final Jdbi jdbi, final String tableName) {
        this.jdbi = jdbi;
        this.tableName = tableName;

        // Install the SQL Objects and Guava plugins
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.registerArgument(new JdbiFactories.JdbiArgumentFactory());
        jdbi.registerRowMapper(Component.class, (rs, ctx) ->
            new Component(rs.getString(FEATURE_ROW_NAME),
                UUID.fromString(rs.getString(ID_ROW_NAME)),
                listFromString(rs.getString(ARGUMENTS_ROW_NAME)),
                Component.Lifespan.valueOf(rs.getString(LIFESPAN_ROW_NAME))));
        // Set default timezone to UTC
        jdbi.getConfig(TimestampedConfig.class).setTimezone(ZoneOffset.UTC);
    }

    public void insertComponent(Component component) {
        jdbi.useHandle(handle -> handle.createUpdate("insert into %s (%s, %s, %s, %s, %s) values (:feature, :id, :arguments, :lifespan, :last_used)".formatted(
                tableName, FEATURE_ROW_NAME, ID_ROW_NAME, ARGUMENTS_ROW_NAME, LIFESPAN_ROW_NAME, LAST_USED_ROW_NAME
            ))
            .bind(FEATURE_ROW_NAME, component.featureId())
            .bind(ID_ROW_NAME, component.uuid())
            .bind(ARGUMENTS_ROW_NAME, listToString(component.arguments()))
            .bind(LIFESPAN_ROW_NAME, component.lifespan().toString())
            .bind(LAST_USED_ROW_NAME, Instant.now())
            .execute());
    }

    public Optional<Component> getComponent(UUID id) {
        final var comp = jdbi.withHandle(handle -> handle.createQuery("select %s, %s, %s, %s from %s where %s = :id".formatted(
                FEATURE_ROW_NAME, ID_ROW_NAME, ARGUMENTS_ROW_NAME, LIFESPAN_ROW_NAME, tableName, ID_ROW_NAME
            ))
            .bind("id", id.toString())
            .mapTo(Component.class)
            .findOne());
        comp.ifPresent(component -> setLastUsed(id, Instant.now()));
        return comp;
    }

    public void updateArguments(UUID id, List<String> newArguments) {
        jdbi.useHandle(handle -> handle.createUpdate("update %s set %s = :args, %s = :last_used where %s = :id".formatted(
                tableName, ARGUMENTS_ROW_NAME, LAST_USED_ROW_NAME, ID_ROW_NAME
            ))
            .bind("args", listToString(newArguments))
            .bind("id", id.toString())
            .bind("last_used", Instant.now())
            .execute());
    }

    public void setLastUsed(UUID id, Instant lastUsed) {
        jdbi.useHandle(handle -> handle.createUpdate("update %s set %s = :last_used where %s = :id".formatted(
                tableName, LAST_USED_ROW_NAME, ID_ROW_NAME
            ))
            .bind(LAST_USED_ROW_NAME, lastUsed)
            .bind("id", id.toString())
            .execute());
    }

    public void removeComponentsLastUsedBefore(Instant before) {
        jdbi.useHandle(handle -> handle.createUpdate("delete from %s where %s <= :before and %s != %s".formatted(
                tableName, LAST_USED_ROW_NAME, LIFESPAN_ROW_NAME, Component.Lifespan.PERMANENT.toString()
            ))
            .bind("before", before));
    }

    private static final Type STRING_LIST_TYPE = new TypeToken<List<String>>() {}.getType();
    private static String listToString(final List<String> list) {
        return Constants.Gsons.NO_PRETTY_PRINTING.toJson(list, STRING_LIST_TYPE);
    }
    private static List<String> listFromString(final String string) {
        return Constants.Gsons.NO_PRETTY_PRINTING.fromJson(string, STRING_LIST_TYPE);
    }
}
