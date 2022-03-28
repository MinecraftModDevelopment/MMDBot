package com.mcmoddev.mmdbot.core.commands.component;

import java.util.List;
import java.util.UUID;

public record Component(String featureId, UUID uuid, List<String> arguments, Lifespan lifespan) {

    public Component(String featureId, UUID uuid, List<String> arguments) {
        this(featureId, uuid, arguments, Lifespan.TEMPORARY);
    }

    enum Lifespan {
        PERMANENT,
        TEMPORARY
    }
}
