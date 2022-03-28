/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
package com.mcmoddev.mmdbot.core.commands.component;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ComponentManager extends ListenerAdapter {
    private final ComponentStorage storage;
    final Map<String, ComponentListener> listeners = new HashMap<>();

    public ComponentManager(final ComponentStorage storage, final ScheduledExecutorService service, final Period deletionPeriod) {
        this.storage = storage;

        service.scheduleAtFixedRate(() -> getStorage().removeComponentsLastUsedBefore(Instant.now().minus(deletionPeriod.time(), deletionPeriod.unit().toChronoUnit())), 0, deletionPeriod.time(), deletionPeriod.unit());
    }

    public ComponentStorage getStorage() {
        return storage;
    }

    public ComponentListener.Builder createListener(final String featureId) {
        if (listeners.containsKey(featureId)) {
            throw new IllegalArgumentException("Listener with feature ID \"" + featureId + "\" exists already!");
        }
        return new ComponentListener.Builder(featureId, this);
    }

    @Override
    public void onButtonInteraction(@NotNull final ButtonInteractionEvent event) {
        try {
            if (event.getButton().getId() != null) {
                final var id = UUID.fromString(event.getButton().getId());
                getStorage().getComponent(id).ifPresent(component -> {
                    final var listener = listeners.get(component.featureId());
                    if (listener == null) {
                        event.deferReply(true).setContent("It seems like I can't handle this button anymore due to its listener being deleted.").queue();
                    } else {
                        listener.onButtonInteraction(new ButtonInteractionContext() {
                            @Override
                            public ButtonInteractionEvent getEvent() {
                                return event;
                            }

                            @Override
                            public ComponentManager getManager() {
                                return ComponentManager.this;
                            }

                            @Override
                            public List<String> getArguments() {
                                return component.arguments();
                            }

                            @Override
                            public UUID getComponentId() {
                                return component.uuid();
                            }
                        });
                    }
                });
            }
        } catch (IllegalArgumentException ignored) {

        }
    }

    record Period(long time, TimeUnit unit) {

    }
}
