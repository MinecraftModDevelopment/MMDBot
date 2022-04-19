package com.mcmoddev.mmdbot.watcher.punishments;

import com.mcmoddev.mmdbot.watcher.util.Configuration;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.Event;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public interface PunishableAction<E extends Event> extends Predicate<E> {

    Punishment getPunishment(Configuration.Punishments config);

    Class<E> getEventClass();

    @Nullable
    Member getPunishedMember(E event);

    String getReason();

    default void whenPunished(E event, Member member, Punishment punishment) {

    }

}
