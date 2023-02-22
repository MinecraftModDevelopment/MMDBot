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

import com.mcmoddev.mmdbot.core.common.ScamDetector;
import com.mcmoddev.mmdbot.watcher.util.Configuration;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import org.jetbrains.annotations.Nullable;

import static com.mcmoddev.mmdbot.core.common.ScamDetector.postScamEvent;

class PhishingLink implements PunishableAction<GenericMessageEvent> {
    @Override
    public Punishment getPunishment(final Configuration.Punishments config) {
        return config.phishingLink;
    }

    @Override
    public Class<GenericMessageEvent> getEventClass() {
        return GenericMessageEvent.class;
    }

    @Override
    public @Nullable Member getPunishedMember(final GenericMessageEvent event) {
        if (!event.isFromGuild()) {
            return null;
        }
        if (event instanceof MessageReceivedEvent received) {
            return maybeReturnMember(received.getMember());
        } else if (event instanceof MessageUpdateEvent updateEvent) {
            return maybeReturnMember(updateEvent.getMember());
        }
        return null;
    }

    private Member maybeReturnMember(Member author) {
        if (author == null || author.getUser().isBot() || author.getUser().isSystem()) {
            return null;
        }
        return author;
    }

    @Override
    public String getReason(final GenericMessageEvent event, final Member member) {
        return "Sending a phishing link";
    }

    @Override
    public boolean test(final GenericMessageEvent genericMessageEvent) {
        if (!genericMessageEvent.isFromGuild()) return false;
        final var msg = resolveMessage(genericMessageEvent);
        if (msg == null) return false;
        final var containsScam = ScamDetector.containsScam(msg.getContentRaw());
        if (containsScam && msg.getMember() == null) {
            msg.delete().queue(); // We don't have a member, but scam link is still present
            return false;
        }
        return containsScam;
    }

    @Override
    public void whenPunished(final GenericMessageEvent event, final Member member, final Punishment punishment) {
        final var msg = resolveMessage(event);
        if (msg == null) return;
        msg.delete()
            .reason("Phishing link")
            .queue($ -> postScamEvent(msg.getGuild().getIdLong(), msg.getAuthor().getIdLong(), msg.getChannel().getIdLong(),
                msg.getContentRaw(), msg.getAuthor().getEffectiveAvatarUrl(), edited(event)));
    }

    @Nullable
    public Message resolveMessage(GenericMessageEvent event) {
        if (event instanceof MessageReceivedEvent mR) {
            return mR.getMessage();
        } else if (event instanceof MessageUpdateEvent mU) {
            return mU.getMessage();
        }
        return null;
    }

    public boolean edited(GenericMessageEvent event) {
        return event instanceof MessageUpdateEvent;
    }
}
