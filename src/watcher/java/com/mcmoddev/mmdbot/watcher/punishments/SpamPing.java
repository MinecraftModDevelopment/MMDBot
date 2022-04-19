package com.mcmoddev.mmdbot.watcher.punishments;

import com.mcmoddev.mmdbot.watcher.util.Configuration;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

class SpamPing implements PunishableAction<MessageReceivedEvent> {

    public static final int THRESHOLD = 20;

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
    public String getReason() {
        return "Spam Pinging";
    }

    @Override
    public boolean test(final MessageReceivedEvent event) {
        final var msg = event.getMessage();
        return event.isFromGuild() && (msg.getMentionedUsersBag().uniqueSet().size() >= THRESHOLD || msg.getMentionedRolesBag().uniqueSet().size() >= THRESHOLD);
    }
}
