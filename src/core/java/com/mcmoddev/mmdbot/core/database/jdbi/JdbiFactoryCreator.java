package com.mcmoddev.mmdbot.core.database.jdbi;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.PreparedStatement;
import java.util.function.BiConsumer;

public abstract class JdbiFactoryCreator<T> extends AbstractArgumentFactory<T> {

    JdbiFactoryCreator(final int sqlType) {
        super(sqlType);
    }

    public static <T> JdbiFactoryCreator<T> create(int sqlType, BiConsumer<PreparedStatement, T> writer) {
        return new JdbiFactoryCreator<T>(sqlType) {
            @Override
            protected Argument build(final T value, final ConfigRegistry config) {
                return (position, statement, ctx) -> writer.accept(statement, value);
            }
        };
    }
}
