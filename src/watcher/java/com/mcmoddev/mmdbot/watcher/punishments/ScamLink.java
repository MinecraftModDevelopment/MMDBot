package com.mcmoddev.mmdbot.watcher.punishments;

import static com.mcmoddev.mmdbot.core.common.ScamDetector.postScamEvent;
import com.mcmoddev.mmdbot.core.common.ScamDetector;
import com.mcmoddev.mmdbot.watcher.util.Configuration;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

class ScamLink implements PunishableAction<GenericMessageEvent> {
    @Override
    public Punishment getPunishment(final Configuration.Punishments config) {
        return config.scamLink;
    }

    @Override
    public Class<GenericMessageEvent> getEventClass() {
        return GenericMessageEvent.class;
    }

    @Override
    public @Nullable Member getPunishedMember(final GenericMessageEvent event) {
        if (event.isFromGuild()) {
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
    public String getReason() {
        return "Scam Link";
    }

    @Override
    public boolean test(final GenericMessageEvent genericMessageEvent) {
        if (!genericMessageEvent.isFromGuild()) return false;
        final var msg = resolveMessage(genericMessageEvent);
        if (msg == null) return false;
        if (!Objects.requireNonNull(msg.getMember()).hasPermission(Permission.MESSAGE_MANAGE)) {
            return ScamDetector.containsScam(msg.getContentRaw().toLowerCase(Locale.ROOT));
        }
        return false;
    }

    @Override
    public void whenPunished(final GenericMessageEvent event, final Member member, final Punishment punishment) {
        final var msg = resolveMessage(event);
        if (msg == null) return;
        msg.delete()
            .reason("Scam link")
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
