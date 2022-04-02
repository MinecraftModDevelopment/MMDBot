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
package com.mcmoddev.mmdbot.core.database.jdbi;

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
