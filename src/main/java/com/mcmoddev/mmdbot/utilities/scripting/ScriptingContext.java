package com.mcmoddev.mmdbot.utilities.scripting;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.ArrayList;
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

    public Object get(String key) {
        return map.get(key);
    }

    public void set(String key, Object value) {
        if (value instanceof ScriptingContext context) {
            map.put(key, context.toProxyObject());
        } else if (value instanceof List list) {
            final var objects = new ArrayList<>();
            for (var obj : list ){
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
    }

    public void setFunction(String name, Function<List<Value>, Object> function) {
        map.put(name, ScriptingUtils.functionObject(function));
    }

    public void setFunctionVoid(String name, Consumer<List<Value>> consumer) {
        map.put(name, ScriptingUtils.functionObject(args -> {
            consumer.accept(args);
            return null;
        }));
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
}
