package com.mcmoddev.mmdbot.commander.tricks;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.Map;

public class TrickCodec implements Codec<Trick> {

    public static final Map<String, Trick.TrickType<?>> CLASS_TO_TYPE_CONVERSION = Map.of(
        StringTrick.class.getTypeName(), StringTrick.TYPE,
        EmbedTrick.class.getTypeName(), EmbedTrick.TYPE,
        ScriptTrick.class.getTypeName(), ScriptTrick.TYPE
    );

    @Override
    public <T> DataResult<Pair<Trick, T>> decode(final DynamicOps<T> ops, final T input) {
        return ops.getStringValue(ops.get(input, "$type").get().orThrow())
            .flatMap(typeName -> {
                var type = Tricks.getTrickType(typeName);
                if (type == null) {
                    type = CLASS_TO_TYPE_CONVERSION.get(typeName); // try to convert from a class
                }
                if (type == null) {
                    return DataResult.error("Unknown trick type: " + typeName);
                } else {
                    return type.getCodec().decode(ops, ops.get(input, "value").get().orThrow())
                        .map(p -> Pair.of(p.getFirst(), p.getSecond()));
                }
            });
    }

    @Override
    public <T> DataResult<T> encode(final Trick input, final DynamicOps<T> ops, final T prefix) {
        return ops.mergeToMap(prefix, Map.of(
            ops.createString("$type"), ops.createString(Tricks.getTrickTypeName(input.getType())),
            ops.createString("value"), encodeUnsafe(input.getCodec(), input, ops).get().orThrow()
        ));
    }

    @SuppressWarnings("unchecked")
    private static <A, T> DataResult<T> encodeUnsafe(Codec<A> codec, final Object input, final DynamicOps<T> ops) {
        return codec.encodeStart(ops, (A) input);
    }
}
