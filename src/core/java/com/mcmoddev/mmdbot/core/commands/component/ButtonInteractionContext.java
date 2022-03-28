package com.mcmoddev.mmdbot.core.commands.component;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.List;
import java.util.UUID;

public interface ButtonInteractionContext {

    ButtonInteractionEvent getEvent();

    ComponentManager getManager();

    List<String> getArguments();

    UUID getComponentId();
}
