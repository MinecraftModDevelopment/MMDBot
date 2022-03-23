package com.mcmoddev.mmdbot.core.util.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Instant;

public class InstantTypeAdapter extends TypeAdapter<Instant> {

    @Override
    public void write(final JsonWriter out, final Instant value) throws IOException {
        out.value(value.getEpochSecond());
    }

    @Override
    public Instant read(final JsonReader in) throws IOException {
        return Instant.ofEpochSecond(in.nextLong());
    }
}
