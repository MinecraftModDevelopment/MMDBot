package com.mcmoddev.mmdbot.commander.quotes;

import com.mcmoddev.mmdbot.core.dfu.ExtendedCodec;
import com.mcmoddev.mmdbot.core.dfu.ExtendedDynamicOps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.Map;

public class QuoteCodec implements ExtendedCodec<IQuote> {

    public static final Map<String, IQuote.QuoteType<?>> CLASS_TO_TYPE_CONVERSION = Map.of(
        StringQuote.class.getTypeName(), StringQuote.TYPE,
        NullQuote.class.getTypeName(), NullQuote.TYPE
    );

    @Override
    public <T> DataResult<Pair<IQuote, T>> decode(final ExtendedDynamicOps<T> ops, final T input) {
        return ops.getOpsMap(input).map(map -> map.getAsString("type"))
            .flatMap(tN -> {
                final var typeName = tN.get().orThrow();
                var type = Quotes.getQuoteType(typeName);
                if (type == null) {
                    type = CLASS_TO_TYPE_CONVERSION.get(typeName); // try to convert from a class
                }
                if (type == null) {
                    return DataResult.error("Unknown quote type: " + typeName);
                } else {
                    return type.getCodec().decode(ops, ops.get(input, "value").get().orThrow())
                        .map(p -> Pair.of(p.getFirst(), p.getSecond()));
                }
            });
    }

    @Override
    public <T> DataResult<T> encode(final IQuote input, final ExtendedDynamicOps<T> ops, final T prefix) {
        return ops.mergeToMap(prefix, ops.createOpsMap()
            .put("type", ops.createString(Quotes.getQuoteTypeName(input.getType())))
            .put("value", encodeUnsafe(input.getType().getCodec(), input, ops).get().orThrow())
        );
    }

    @SuppressWarnings("unchecked")
    private static <A, T> DataResult<T> encodeUnsafe(Codec<A> codec, final Object input, final DynamicOps<T> ops) {
        return codec.encodeStart(ops, (A) input);
    }
}
