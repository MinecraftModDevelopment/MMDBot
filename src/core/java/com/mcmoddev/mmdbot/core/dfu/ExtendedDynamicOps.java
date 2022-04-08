package com.mcmoddev.mmdbot.core.dfu;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the extension of a {@link DynamicOps}.
 *
 * @param <T> The type this interface serializes to and deserializes from. For example,
 *            {@link com.google.gson.JsonElement} or NbtTag.
 */
public interface ExtendedDynamicOps<T> extends DynamicOps<T> {

    /**
     * Wraps a {@link DynamicOps}.
     *
     * @param ops the ops to wrap
     * @param <T> the type of the ops
     * @return the wrapped ops
     */
    static <T> ExtendedDynamicOps<T> wrap(final DynamicOps<T> ops) {
        return new ExtendedDynamicOpsImpl<>(ops);
    }

    /**
     * Creates an {@link OpsMap} from the delegate.
     *
     * @param delegate the map to delegate the calls of the {@link OpsMap} to
     * @return the ops map
     * @see OpsMap#create(Map, DynamicOps)
     */
    default OpsMap<T> createOpsMap(final Map<T, T> delegate) {
        return OpsMap.create(delegate, this);
    }

    /**
     * Creates an immutable {@link OpsMap} from the provided {@link MapLike}.
     *
     * @param map the {@link MapLike} from which to get the values
     * @return the <storng>immutable</storng> ops map
     * @see OpsMap#from(MapLike, DynamicOps)
     */
    default OpsMap<T> createOpsMap(final MapLike<T> map) {
        return OpsMap.from(map, this);
    }

    /**
     * Creates an {@link OpsMap}, with a new {@link HashMap} as the delegate.
     *
     * @return the ops map
     * @see OpsMap#create(Map, DynamicOps)
     */
    default OpsMap<T> createOpsMap() {
        return createOpsMap(new HashMap<>());
    }

    /**
     * Extracts the entries in the given value, returning them in an <b>immutable</b> {@link OpsMap} object.
     *
     * @param input The serialized value.
     * @return A {@link DataResult} containing the extracted entries, or an error message if the entries
     * could not be extracted.
     */
    DataResult<OpsMap<T>> getOpsMap(T input);

    /**
     * Extracts the entries in the given value, returning them in an <b>immutable</b> {@link OpsMap} object.
     * <br>
     * If the entries couldn't be extracted, a {@link java.util.NoSuchElementException} is thrown.
     *
     * @param input The serialized value.
     * @return An {@link OpsMap} containing the extracted entries.
     */
    default OpsMap<T> getOpsMapOrThrow(T input) {
        return getOpsMap(input).result().orElseThrow();
    }

    /**
     * An implementation of a {@link Map} which stores a reference to a {@link DynamicOps} in order to
     * allow the adding of keys and values more easily.
     * <br>
     * This Map's purpose is for {@link DynamicOps#getMap(Object)} and {@link DynamicOps#mergeToMap(Object, Object, Object)}.
     *
     * @param <T> the type of the map
     */
    interface OpsMap<T> extends Map<T, T> {

        /**
         * Creates an {@link OpsMap}.
         *
         * @param delegate a map to delegate calls to
         * @param ops      the ops
         * @param <T>      the type of the map
         * @return the map
         */
        static <T> OpsMap<T> create(final Map<T, T> delegate, final DynamicOps<T> ops) {
            return new ExtendedDynamicOpsImpl.OpsMapImpl<>(delegate, ops);
        }

        /**
         * Creates an {@link OpsMap} from a {@link MapLike}.
         *
         * @param map the {@link MapLike} from which to get the values
         * @param ops the ops
         * @param <T> the type of the map
         * @return the map. This map is Immutable!
         */
        static <T> OpsMap<T> from(final MapLike<T> map, final DynamicOps<T> ops) {
            return new ExtendedDynamicOpsImpl.OpsMapImpl<>(map.entries().collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond)), ops);
        }

        /**
         * Puts a value in the map.
         *
         * @param key   the key of the value
         * @param value the value to put
         * @return the map instance
         */
        OpsMap<T> put(String key, T value);

        /**
         * Gets a value from the map.
         *
         * @param key the key of the value
         * @return the value, or {@code null} if it doesn't exist
         */
        T get(String key);

        /**
         * Gets a string value from the map.
         *
         * @param key the key of the value
         * @return a {@link DataResult} which may or may not contain the value, depending on if it exists
         */
        DataResult<String> getAsString(String key);

        /**
         * Gets a {@link Number} value from the map.
         *
         * @param key the key of the value
         * @return a {@link DataResult} which may or may not contain the value, depending on if it exists
         */
        DataResult<Number> getAsNumber(String key);

        /**
         * Gets a list from the map.
         *
         * @param key    the key of the list
         * @param mapper a function which maps a value to another one
         * @param <A>    the type of the list
         * @return a {@link DataResult} which may or may not contain the value, depending on if the key exists
         */
        <A> DataResult<List<? extends A>> getAsList(String key, Function<? super T, ? extends A> mapper);

        /**
         * Gets a list from the map, using {@link #getAsList(String, Function)}. <br>
         * If the result of that method is an error {@link DataResult}, a {@link java.util.NoSuchElementException} will be thrown.
         *
         * @param key    the key of the list
         * @param mapper a function which maps a value to another one
         * @param <A>    the type of the list
         * @return the list
         */
        default <A> List<? extends A> getAsListOrThrow(String key, Function<? super T, ? extends A> mapper, boolean allowPartial) {
            return getAsList(key, mapper).getOrThrow(allowPartial, s -> {
                throw new NoSuchElementException("Expected an object with the key " + key + " to be present.");
            });
        }
    }
}
