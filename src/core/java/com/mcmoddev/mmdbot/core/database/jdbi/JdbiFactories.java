/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2023 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.core.database.jdbi;

import com.mcmoddev.mmdbot.core.util.Constants;
import net.dv8tion.jda.api.entities.ISnowflake;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.NullArgument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.core.statement.UnableToCreateStatementException;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
public final class JdbiFactories {
    private static final Map<Type, ArgumentFactory<?>> BY_TYPE_FACTORIES = new HashMap<>();

    public static final ArgumentFactory<UUID> UUID = createAndRegister(Types.JAVA_OBJECT, java.util.UUID.class, ((position, statement, ctx, value) -> statement.setString(position, value.toString())));
    public static final ArgumentFactory<ISnowflake> SNOWFLAKE = create(Types.BIGINT, ISnowflake.class, ((position, statement, ctx, value) -> statement.setLong(position, value.getIdLong())));
    public static final ArgumentFactory<List> LIST = create(Types.JAVA_OBJECT, List.class, ((position, statement, ctx, value) -> statement.setString(position, Constants.Gsons.NO_PRETTY_PRINTING.toJson(value))));

    public static <T> ArgumentFactory<T> create(int sqlType, Type type, Factory<T> factory) {
        return new ArgumentFactory<T>(sqlType, type) {
            @Override
            protected Argument build(final T value, final ConfigRegistry config) {
                return (position, statement, ctx) -> factory.accept(position, statement, ctx, value);
            }
        };
    }

    public static <T> ArgumentFactory<T> createAndRegister(int sqlType, Type type, Factory<T> factory) {
        final var fct = create(sqlType, type, factory);
        BY_TYPE_FACTORIES.put(type, fct);
        return fct;
    }

    @FunctionalInterface
    private interface Factory<T> {
        void accept(int position, PreparedStatement statement, StatementContext ctx, T value) throws SQLException;
    }

    /**
     * A factory for argument factories defined in {@link JdbiFactories}.
     */
    public static final class JdbiArgumentFactory implements org.jdbi.v3.core.argument.ArgumentFactory {

        public static final JdbiArgumentFactory FACTORY = new JdbiArgumentFactory();

        public JdbiArgumentFactory() {
        }

        @Override
        public Optional<Argument> build(final Type type, final Object value, final ConfigRegistry config) {
            final Class<?> erased = GenericTypes.getErasedType(type);
            if (List.class.isAssignableFrom(erased)) {
                return LIST.build(type, value, config);
            } else if (ISnowflake.class.isAssignableFrom(erased)) {
                return SNOWFLAKE.build(type, value, config);
            } else if (BY_TYPE_FACTORIES.containsKey(erased)) {
                return BY_TYPE_FACTORIES.get(type).build(type, value, config);
            }
            return Optional.empty();
        }
    }

    public static abstract class ArgumentFactory<T> implements org.jdbi.v3.core.argument.ArgumentFactory.Preparable {
        private final int sqlType;
        private final ArgumentPredicate isInstance;

        /**
         * Constructs an {@link ArgumentFactory} for type {@code T}.
         *
         * @param sqlType the {@link java.sql.Types} constant to use when the argument value is {@code null}.
         */
        protected ArgumentFactory(int sqlType, final Type argumentType) {
            this.sqlType = sqlType;

            if (argumentType instanceof Class<?> argumentClass) {
                this.isInstance = (type, value) ->
                    argumentClass.isAssignableFrom(GenericTypes.getErasedType(type)) || argumentClass.isInstance(value);
            } else {
                this.isInstance = (type, value) -> argumentType.equals(type);
            }
        }

        @Override
        public Optional<Function<Object, Argument>> prepare(Type type, ConfigRegistry config) {
            return isInstance.test(type, null)
                ? Optional.of(value -> innerBuild(value, config)
                .orElseThrow(() -> new UnableToCreateStatementException("Prepared argument " + value + " of type " + type + " failed to bind")))
                : Optional.empty();
        }

        @Override
        public final Optional<Argument> build(Type type, Object value, ConfigRegistry config) {
            if (!isInstance.test(type, value)) {
                return Optional.empty();
            }
            return innerBuild(value, config);
        }

        @SuppressWarnings("unchecked")
        private Optional<Argument> innerBuild(Object value, ConfigRegistry config) {
            return Optional.of(value == null
                ? new NullArgument(sqlType)
                : build((T) value, config));
        }

        /**
         * Produce an argument object for the given value.
         *
         * @param value  the value to convert to an argument
         * @param config the config registry
         * @return an {@link Argument} for the given {@code value}.
         */
        protected abstract Argument build(T value, ConfigRegistry config);

        private interface ArgumentPredicate {
            boolean test(Type type, Object value);
        }
    }
}
