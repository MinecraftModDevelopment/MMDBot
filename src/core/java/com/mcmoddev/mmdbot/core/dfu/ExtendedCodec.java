package com.mcmoddev.mmdbot.core.dfu;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

/**
 * An implementation of {@link Codec}, which adds some helper methods.
 *
 * @param <C> the type of the codec
 */
public interface ExtendedCodec<C> extends Codec<C> {

    /**
     * Decodes an object from the specified serialized data. If decoding fails, returns an error {@link DataResult}.
     *
     * @param ops   The {@link ExtendedDynamicOps} instance defining the serialized form.
     * @param input The serialized data.
     * @param <T>  The type of the serialized form.
     * @return A {@link Pair} containing the decoded object and the remaining serialized data, wrapped in a {@link DataResult}.
     */
    <T> DataResult<Pair<C, T>> decode(ExtendedDynamicOps<T> ops, T input);

    /**
     * {@inheritDoc}
     */
    @Override
    default <T> DataResult<Pair<C, T>> decode(DynamicOps<T> ops, T input) {
        return decode(ExtendedDynamicOps.wrap(ops), input);
    }

    /**
     * Encodes an object into the specified serialized data. If encoding fails, returns an error {@link DataResult}.
     *
     * @param input  The object to serialize.
     * @param ops    The {@link ExtendedDynamicOps} instance defining the serialized form.
     * @param prefix The existing serialized data to append to.
     * @param <T>    The type of the serialized form.
     * @return A {@link DataResult} wrapping the serialized form of {@code input}, appended to {@code prefix}.
     */
    <T> DataResult<T> encode(C input, ExtendedDynamicOps<T> ops, T prefix);

    /**
     * {@inheritDoc}
     */
    @Override
    default <T> DataResult<T> encode(C input, DynamicOps<T> ops, T prefix) {
        return encode(input, ExtendedDynamicOps.wrap(ops), prefix);
    }
}
