package com.mcmoddev.mmdbot.core.database.jdbi;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonArray;
import com.mcmoddev.mmdbot.core.util.Constants;
import io.github.matyrobbrt.curseforgeapi.util.Utils;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.ArgumentFactory;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.statement.StatementContext;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class JdbiFactories<T> extends AbstractArgumentFactory<T> {
    private static final Map<Type, AbstractArgumentFactory<?>> BY_TYPE_FACTORIES = new HashMap<>();

    public static final AbstractArgumentFactory<UUID> UUID = create(Types.JAVA_OBJECT, java.util.UUID.class, ((position, statement, ctx, value) -> statement.setString(position, value.toString())));

    JdbiFactories(final int sqlType) {
        super(sqlType);
    }

    public static <T> JdbiFactories<T> create(int sqlType, Type type, Factory<T> factory) {
        final var fct = new JdbiFactories<T>(sqlType) {
            @Override
            protected Argument build(final T value, final ConfigRegistry config) {
                return (position, statement, ctx) -> factory.accept(position, statement, ctx, value);
            }
        };
        BY_TYPE_FACTORIES.put(type, fct);
        return fct;
    }

    @FunctionalInterface
    interface Factory<T> {
        void accept(int position, PreparedStatement statement, StatementContext ctx, T value) throws SQLException;
    }

    public static final class JdbiArgumentFactory implements ArgumentFactory {

        @Override
        public Optional<Argument> build(final Type type, final Object value, final ConfigRegistry config) {
            if (BY_TYPE_FACTORIES.containsKey(type)) {
                return BY_TYPE_FACTORIES.get(type).build(type, value, config);
            }
            return Optional.empty();
        }
    }
}
