package com.mcmoddev.mmdbot.core.commands.context;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommandContext {

    @NotNull
    User getUser();

    @Nullable
    Member getMember();

    @Nullable
    Guild getGuild();

    @NotNull
    RestAction<SentMessage> replyOrEdit(final Message message);

    @Nullable SlashCommandEvent asSlashCommandEvent();
    @Nullable CommandEvent asCommandEvent();

    static CommandContext fromSlashCommandEvent(final SlashCommandEvent event) {
        return new SlashCommandContext(event, event.getHook());
    }

    static CommandContext fromCommandEvent(final CommandEvent event) {
        return new PrefixCommandContext(event);
    }
}
