package com.mcmoddev.mmdbot.core.commands.context;

import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Nullable;

public interface SentMessage extends ISnowflake {

    @Nullable
    Message asMessage();

    @Nullable
    InteractionHook asInteraction();

    RestAction<?> delete();
}
