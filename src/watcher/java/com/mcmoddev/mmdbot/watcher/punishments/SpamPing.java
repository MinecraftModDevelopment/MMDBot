/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2023 <MMD - MinecraftModDevelopment>
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

import com.mcmoddev.mmdbot.watcher.util.Configuration;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

class SpamPing implements PunishableAction<MessageReceivedEvent> {

    public static final int USER_THRESHOLD = 15;
    public static final int ROLE_THRESHOLD = 5;

    @Override
    public Punishment getPunishment(final Configuration.Punishments config) {
        return config.spamPing;
    }

    @Override
    public Class<MessageReceivedEvent> getEventClass() {
        return MessageReceivedEvent.class;
    }

    @Override
    public @Nullable Member getPunishedMember(final MessageReceivedEvent event) {
        if (event.getAuthor().isSystem() || event.getAuthor().isBot()) {
            return null;
        }
        return event.getMember();
    }

    @Override
    public String getReason(final MessageReceivedEvent event, final Member member) {
        return "Spam Pinging";
    }

    @Override
    public boolean test(final MessageReceivedEvent event) {
        final var msg = event.getMessage();
        return event.isFromGuild() &&
            (msg.getMentions().getUsersBag().uniqueSet().size() >= USER_THRESHOLD ||
                msg.getMentions().getRolesBag().uniqueSet().size() >= ROLE_THRESHOLD);
    }
}
