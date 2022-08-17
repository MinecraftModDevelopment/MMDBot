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
