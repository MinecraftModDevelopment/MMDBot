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
package com.mcmoddev.mmdbot.commander.util.script;

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.ISnowflake;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyInstantiable;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ScriptingContext {

    private final String name;
    private final Map<String, Object> map;

    public ScriptingContext(final String name, final Map<String, Object> map) {
        this.map = map;
        this.name = name;
    }

    public static ScriptingContext of(String name) {
        return new ScriptingContext(name, new LinkedHashMap<>());
    }

    public static ScriptingContext of(String name, ISnowflake snowflake) {
        final var context = new ScriptingContext(name, new LinkedHashMap<>());
        context.set("id", snowflake.getId());
        context.set("timeCreated", snowflake.getTimeCreated());
        return context;
    }

    public static ScriptingContext of(String name, IMentionable snowflake) {
        final var context = of(name, (ISnowflake) snowflake);
        context.setFunction("asMention", args -> {
            ScriptingUtils.validateArgs(args, 0);
            return snowflake.getAsMention();
        });
        return context;
    }

    public Object get(String key) {
        return map.get(key);
    }

    public ScriptingContext set(String key, Object value) {
        if (value instanceof ScriptingContext context) {
            map.put(key, context.toProxyObject());
        } else if (value instanceof List list) {
            final var objects = new ArrayList<>();
            for (var obj : list) {
                if (obj instanceof ScriptingContext context) {
                    objects.add(context);
                } else {
                    objects.add(obj);
                }
            }
            map.put(key, objects);
        } else {
            map.put(key, value);
        }
        return this;
    }

    public ScriptingContext setFunction(String name, Function<List<Value>, Object> function) {
        map.put(name, ScriptingUtils.functionObject(function));
        return this;
    }

    public ScriptingContext setFunctionVoid(String name, Consumer<List<Value>> consumer) {
        map.put(name, ScriptingUtils.functionObject(args -> {
            consumer.accept(args);
            return null;
        }));
        return this;
    }

    public ScriptingContext addInstantiatable(String name, Function<List<Value>, Object> factory) {
        map.put(name, new NameableProxyInstantiable() {
            @Override
            public Object newInstance(final Value... arguments) {
                final var any = factory.apply(Arrays.asList(arguments));
                if (any instanceof ScriptingContext context) {
                    return context.toProxyObject();
                }
                return any;
            }

            @Override
            public String getName() {
                return name;
            }
        });
        return this;
    }

    public ScriptingContext addInstantiatable(String[] names, Function<List<Value>, Object> factory) {
        for (var name : names) {
            addInstantiatable(name, factory);
        }
        return this;
    }

    public ScriptingContext flatAdd(ScriptingContext other) {
        other.map().keySet().forEach(key -> this.map().put(key, other.get(key)));
        return this;
    }

    public void applyTo(Value bindings) {
        map().forEach(bindings::putMember);
    }

    public NamedProxyObject toProxyObject() {
        final var delegate = ProxyObject.fromMap(map);
        return new NamedProxyObject() {
            @Override
            public Object getMember(final String key) {
                return delegate.getMember(key);
            }

            @Override
            public Object getMemberKeys() {
                return delegate.getMemberKeys();
            }

            @Override
            public boolean hasMember(final String key) {
                return delegate.hasMember(key);
            }

            @Override
            public void putMember(final String key, final Value value) {
                delegate.putMember(key, value);
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    public String name() {
        return name;
    }

    public Map<String, Object> map() {
        return map;
    }

    public interface NamedProxyObject extends ProxyObject {
        String getName();
    }

    public interface NameableProxyExecutable extends ProxyExecutable {
        String getName();
    }

    public interface NameableProxyInstantiable extends ProxyInstantiable {
        String getName();
    }
}
