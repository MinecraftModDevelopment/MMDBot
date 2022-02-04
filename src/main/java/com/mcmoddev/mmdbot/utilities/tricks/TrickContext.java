package com.mcmoddev.mmdbot.utilities.tricks;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface TrickContext {

    @Nullable
    Member getMember();

    @Nonnull
    User getUser();

    @Nonnull
    TextChannel getChannel();

    @Nullable
    Guild getGuild();

    @Nonnull
    String[] getArgs();

    void reply(String content);
    void replyEmbeds(MessageEmbed... embeds);
}
