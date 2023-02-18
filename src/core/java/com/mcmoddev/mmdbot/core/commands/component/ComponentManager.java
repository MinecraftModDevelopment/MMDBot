/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
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

import com.mcmoddev.mmdbot.core.commands.component.context.ButtonInteractionContext;
import com.mcmoddev.mmdbot.core.commands.component.context.ModalInteractionContext;
import com.mcmoddev.mmdbot.core.commands.component.context.SelectMenuInteractionContext;
import com.mcmoddev.mmdbot.core.commands.component.storage.ComponentStorage;
import lombok.NonNull;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.mcmoddev.mmdbot.core.commands.component.Component.ID_SPLITTER;

/**
 * A {@link ComponentManager} is responsible for tracking {@link Component Components} and dispatching events affecing them to
 * the correct listeners, based on the {@link Component#featureId() component feature ID}.
 */
public class ComponentManager implements EventListener {

    private final ComponentStorage storage;
    final Map<String, ComponentListener> listeners = new HashMap<>();

    public ComponentManager(final ComponentStorage storage, final List<ComponentListener> listeners) {
        this.storage = storage;
        listeners.forEach(this::addListener);
    }

    /**
     * @return the storage of this manager
     */
    public ComponentStorage getStorage() {
        return storage;
    }

    /**
     * Removes {@link com.mcmoddev.mmdbot.core.commands.component.Component.Lifespan#TEMPORARY temporary} components that are older than the time specified.
     *
     * @param time the time
     * @param unit the unit
     */
    public void removeComponentsOlderThan(final long time, final TemporalUnit unit) {
        getStorage().removeComponentsLastUsedBefore(Instant.now().minus(time, unit));
    }

    /**
     * Adds a listener to this manager.
     *
     * @param listener the listener
     */
    public void addListener(final ComponentListener listener) {
        final var id = listener.getName();
        if (listeners.containsKey(id)) {
            throw new IllegalArgumentException("Listener with feature ID \"" + id + "\" exists already!");
        }
        listener.setManager(this);
        listeners.put(id, listener);
    }

    @Override
    @SubscribeEvent
    public void onEvent(@NotNull final GenericEvent event) {
        if (event instanceof ButtonInteractionEvent btn) {
            onButtonInteraction(btn);
        } else if (event instanceof GenericSelectMenuInteractionEvent sEvent) {
            onSelectMenuInteraction(sEvent);
        } else if (event instanceof ModalInteractionEvent mEvent) {
            onModalInteraction(mEvent);
        }
    }

    public void onButtonInteraction(@NotNull final ButtonInteractionEvent event) {
        try {
            if (event.getButton().getId() != null) {
                final var buttonArguments = event.getButton().getId().split(ID_SPLITTER);
                final var id = UUID.fromString(buttonArguments[0]);
                getStorage().getComponent(id).ifPresentOrElse(component -> {
                    final var listener = listeners.get(component.featureId());
                    if (listener == null) {
                        event.deferReply(true).setContent("It seems like I can't handle this button anymore due to its listener being deleted.").queue();
                    } else {
                        List<String> buttonArgsList = buttonArguments.length == 1 ? List.of() : Arrays.asList(Arrays.copyOfRange(buttonArguments, 1, buttonArguments.length));
                        listener.onButtonInteraction(new ButtonInteractionContext() {
                            @NotNull
                            @Override
                            public ButtonInteractionEvent getEvent() {
                                return event;
                            }

                            @NotNull
                            @Override
                            public ComponentManager getManager() {
                                return ComponentManager.this;
                            }

                            @NotNull
                            @Override
                            public List<String> getArguments() {
                                return component.arguments();
                            }

                            @Override
                            public @NonNull List<String> getItemComponentArguments() {
                                return buttonArgsList;
                            }

                            @NotNull
                            @Override
                            public UUID getComponentId() {
                                return component.uuid();
                            }
                        });
                    }
                }, () -> event.deferReply(true).setContent("I am sorry. It seems like I don't know what this button does anymore. <:sadge:926848859668353055>").queue());
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException ignored) {

        }
    }

    // TODO - fix the generics
    public void onSelectMenuInteraction(@NotNull final GenericSelectMenuInteractionEvent event) {
        try {
            if (event.getSelectMenu().getId() != null) {
                final var buttonArguments = event.getSelectMenu().getId().split(ID_SPLITTER);
                final var id = UUID.fromString(buttonArguments[0]);
                getStorage().getComponent(id).ifPresentOrElse(component -> {
                    final var listener = listeners.get(component.featureId());
                    if (listener == null) {
                        event.deferReply(true).setContent("It seems like I can't handle this select menu anymore due to its listener being deleted.").queue();
                    } else {
                        List<String> buttonArgsList = buttonArguments.length == 1 ? List.of() : Arrays.asList(Arrays.copyOfRange(buttonArguments, 1, buttonArguments.length));
                        listener.onSelectMenuInteraction(new SelectMenuInteractionContext() {
                            @NotNull
                            @Override
                            public GenericSelectMenuInteractionEvent getEvent() {
                                return event;
                            }

                            @NotNull
                            @Override
                            public ComponentManager getManager() {
                                return ComponentManager.this;
                            }

                            @NotNull
                            @Override
                            public List<String> getArguments() {
                                return component.arguments();
                            }

                            @Override
                            public @NonNull List<String> getItemComponentArguments() {
                                return buttonArgsList;
                            }

                            @NotNull
                            @Override
                            public UUID getComponentId() {
                                return component.uuid();
                            }
                        });
                    }
                }, () -> event.deferReply(true).setContent("I am sorry. It seems like I don't know what this select menu does anymore. <:sadge:926848859668353055>").queue());
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException ignored) {

        }
    }

    public void onModalInteraction(@NotNull final ModalInteractionEvent event) {
        try {
            final var modalArgs = event.getModalId().split(ID_SPLITTER);
            final var id = UUID.fromString(modalArgs[0]);
            getStorage().getComponent(id).ifPresentOrElse(component -> {
                final var listener = listeners.get(component.featureId());
                if (listener == null) {
                    event.deferReply(true).setContent("It seems like I can't handle this modal anymore due to its listener being deleted.").queue();
                } else {
                    List<String> modalArgsList = modalArgs.length == 1 ? List.of() : Arrays.asList(Arrays.copyOfRange(modalArgs, 1, modalArgs.length));
                    listener.onModalInteraction(new ModalInteractionContext() {
                        @NotNull
                        @Override
                        public ModalInteractionEvent getEvent() {
                            return event;
                        }

                        @NotNull
                        @Override
                        public ComponentManager getManager() {
                            return ComponentManager.this;
                        }

                        @NotNull
                        @Override
                        public List<String> getArguments() {
                            return component.arguments();
                        }

                        @Override
                        public @NonNull List<String> getItemComponentArguments() {
                            return modalArgsList;
                        }

                        @NotNull
                        @Override
                        public UUID getComponentId() {
                            return component.uuid();
                        }
                    });
                }
            }, () -> event.deferReply(true).setContent("I am sorry. It seems like I don't know what this modal does anymore. <:sadge:926848859668353055>").queue());
        } catch (IllegalArgumentException | IndexOutOfBoundsException ignored) {

        }
    }
}
