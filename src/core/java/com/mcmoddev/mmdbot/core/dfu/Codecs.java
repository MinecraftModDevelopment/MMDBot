package com.mcmoddev.mmdbot.core.dfu;

import com.mojang.serialization.Codec;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Codecs {

    /**
     * A long codec obtained from a xmapped string codec.
     */
    public static final Codec<Long> LONG_FROM_STRING = Codec.STRING.xmap(Long::valueOf, String::valueOf);

}
