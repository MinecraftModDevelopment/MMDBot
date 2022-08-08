package com.mcmoddev.mmdbot.tests.jda;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.IThreadContainer;
import net.dv8tion.jda.api.entities.NewsChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

public record CastingChannelUnion(TextChannel textChannel) implements MessageChannelUnion {
    @NotNull
    @Override
    public PrivateChannel asPrivateChannel() {
        return cast();
    }

    @NotNull
    @Override
    public TextChannel asTextChannel() {
        return cast();
    }

    @NotNull
    @Override
    public NewsChannel asNewsChannel() {
        return cast();
    }

    @NotNull
    @Override
    public ThreadChannel asThreadChannel() {
        return cast();
    }

    @NotNull
    @Override
    public VoiceChannel asVoiceChannel() {
        return cast();
    }

    @NotNull
    @Override
    public GuildMessageChannel asGuildMessageChannel() {
        return cast();
    }

    @Override
    public @NotNull IThreadContainer asThreadContainer() {
        return cast();
    }

    @NotNull
    @Override
    public String getName() {
        return textChannel.getName();
    }

    @NotNull
    @Override
    public ChannelType getType() {
        return textChannel.getType();
    }

    @NotNull
    @Override
    public JDA getJDA() {
        return textChannel.getJDA();
    }

    @NotNull
    @Override
    public RestAction<Void> delete() {
        return textChannel.delete();
    }

    @Override
    public long getIdLong() {
        return textChannel.getIdLong();
    }

    @SuppressWarnings("unchecked")
    private <T> T cast() {
        return (T) textChannel;
    }

    @Override
    public long getLatestMessageIdLong() {
        return textChannel.getLatestMessageIdLong();
    }

    @Override
    public boolean canTalk() {
        return textChannel.canTalk();
    }
}
