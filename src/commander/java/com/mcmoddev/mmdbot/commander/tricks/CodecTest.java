package com.mcmoddev.mmdbot.commander.tricks;

import com.mojang.serialization.JsonOps;

import java.io.IOException;
import java.util.List;

public class CodecTest {

    public static void main(String[] args) throws IOException {
        final var trick = new EmbedTrick(List.of("hi"), "mhm", "no", 0xffffff);
        final var json = new TrickCodec().encodeStart(JsonOps.INSTANCE, trick).get().orThrow();
        assert new TrickCodec().decode(JsonOps.INSTANCE, json).get().orThrow().getFirst().equals(trick);
    }

}
