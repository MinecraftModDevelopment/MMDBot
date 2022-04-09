package com.mcmoddev.mmdbot.core.dfu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.UnboundedMapCodec;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

public interface Codecs {

    /**
     * A long codec obtained from a xmapped string codec.
     */
    Codec<Long> LONG_FROM_STRING = Codec.STRING.xmap(Long::valueOf, String::valueOf);

    /**
     * A codec used for {@link Pattern patterns}.
     */
    Codec<Pattern> PATTERN = Codec.STRING.xmap(Pattern::compile, Pattern::toString);

    /**
     * A codec used for {@link Color colours}.
     */
    Codec<Color> COLOR = Codec.INT.xmap(Color::new, Color::getRGB);

    /**
     * Creates a codec for mutable lists.
     *
     * @param codec the codec of the elements of the list
     * @param <T>   the type of the list
     * @return the list codec
     */
    static <T> Codec<List<T>> mutableList(Codec<T> codec) {
        return codec.listOf().xmap(ArrayList::new, Function.identity());
    }

    /**
     * Creates a map codec for a {@link java.util.Map} with arbitrary keys. <br>
     * The map creates by this codec is mutable.
     *
     * @param keyCodec     A codec for the key type.
     * @param elementCodec A codec for the element type.
     * @param <K>          The key type.
     * @param <V>          The element type.
     * @return A codec for the map
     */
    static <K, V> Codec<Map<K, V>> unboundedMutableMap(final Codec<K> keyCodec, final Codec<V> elementCodec) {
        return new UnboundedMapCodec<>(keyCodec, elementCodec).xmap(HashMap::new, Function.identity());
    }
}
