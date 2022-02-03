package com.mcmoddev.mmdbot.utilities.scripting;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record ScriptingContext(String name, Map<String, Object> map) {

    public static ScriptingContext of(String name) {
        return new ScriptingContext(name, new LinkedHashMap<>());
    }

    public Object get(String key) {
        return map.get(key);
    }

    public void set(String key, Object value) {
        if (value instanceof ScriptingContext context) {
            map.put(key, context.toProxyObject());
        } else {
            map.put(key, value);
        }
    }

    public void setFunction(String name, Function<List<Value>, Object> function) {
        map.put(name, ScriptingUtils.functionObject(function));
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
        };
    }

    public interface NamedProxyObject extends ProxyObject {
    }

    public interface NameableProxyExecutable extends ProxyExecutable {
        String getName();
    }
}
