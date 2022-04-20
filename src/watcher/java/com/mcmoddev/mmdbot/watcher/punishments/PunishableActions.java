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
