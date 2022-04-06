/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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
