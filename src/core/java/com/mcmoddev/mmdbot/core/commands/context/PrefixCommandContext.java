package com.mcmoddev.mmdbot.core.commands.context;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record PrefixCommandContext(CommandEvent event) implements CommandContext {

    @Override
    public @NotNull User getUser() {
        return event.getAuthor();
    }

    @Override
    public @Nullable Member getMember() {
        return event.getMember();
    }

    @Override
    public @Nullable Guild getGuild() {
        return event.getGuild();
    }

    @Override
    public @NotNull RestAction<SentMessage> replyOrEdit(final Message message) {
        return event.getMessage()
            .reply(message)
            .map(s -> new SentMessage() {
                @Override
                public @Nullable
                Message asMessage() {
                    return s;
                }

                @Override
                public @Nullable
                InteractionHook asInteraction() {
                    return null;
                }

                @Override
                public RestAction<?> delete() {
                    return message.delete();
                }

                @Override
                public long getIdLong() {
                    return s.getIdLong();
                }
            });
    }

    @Override
    public @Nullable SlashCommandEvent asSlashCommandEvent() {
        return null;
    }

    @Override
    public @Nullable CommandEvent asCommandEvent() {
        return event;
    }
}
