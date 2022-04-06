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

record SlashCommandContext(SlashCommandEvent event,
                           InteractionHook hook) implements CommandContext {

    @Override
    public @NotNull User getUser() {
        return hook.getInteraction().getUser();
    }

    @Override
    public @Nullable Member getMember() {
        return hook.getInteraction().getMember();
    }

    @Override
    public @Nullable Guild getGuild() {
        return hook.getInteraction().getGuild();
    }

    @Override
    public @NotNull RestAction<SentMessage> replyOrEdit(final Message message) {
        return hook.editOriginal(message)
            .map(s -> new SentMessage() {
                @Override
                public @Nullable
                Message asMessage() {
                    return s;
                }

                @Override
                public @NotNull
                InteractionHook asInteraction() {
                    return hook;
                }

                @Override
                public RestAction<?> delete() {
                    return hook.deleteOriginal();
                }

                @Override
                public long getIdLong() {
                    return s.getIdLong();
                }
            });
    }

    @Override
    public @Nullable SlashCommandEvent asSlashCommandEvent() {
        return event;
    }

    @Override
    public @Nullable CommandEvent asCommandEvent() {
        return null;
    }
}
