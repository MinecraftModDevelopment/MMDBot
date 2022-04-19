package com.mcmoddev.mmdbot.watcher.punishments;

import com.mcmoddev.mmdbot.watcher.TheWatcher;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

public enum PunishableActions implements EventListener {
    SPAM_PING(new SpamPing()),
    SCAM_LINK(new ScamLink());

    private final EventListener listener;

    <E extends Event> PunishableActions(PunishableAction<E> listener) {
        this.listener = event -> {
            if (listener.getEventClass().isInstance(event)) {
                final var actualEvent = listener.getEventClass().cast(event);
                final var doPunish = listener.test(actualEvent);
                final var member = listener.getPunishedMember(actualEvent);
                if (member != null && doPunish && TheWatcher.getInstance() != null) {
                    final var punishment = listener.getPunishment(TheWatcher.getInstance().getConfig().punishments());
                    punishment.punish(member, listener.getReason(), () -> listener.whenPunished(actualEvent, member, punishment));
                }
            }
        };
    }

    @Override
    public void onEvent(@NotNull final GenericEvent event) {
        listener.onEvent(event);
    }
}
