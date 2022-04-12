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
package com.mcmoddev.mmdbot.thelistener.util;

import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.WebhooksUpdateEvent;
import discord4j.core.event.domain.guild.BanEvent;
import discord4j.core.event.domain.guild.EmojisUpdateEvent;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.guild.GuildUpdateEvent;
import discord4j.core.event.domain.guild.IntegrationsUpdateEvent;
import discord4j.core.event.domain.guild.MemberChunkEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.event.domain.guild.UnbanEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageBulkDeleteEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveAllEvent;
import discord4j.core.event.domain.message.ReactionRemoveEmojiEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import io.github.matyrobbrt.eventdispatcher.util.ClassWalker;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class ListenerAdapter implements EventListener {

    public void onReady(ReadyEvent event) {
    }

    // Message related
    public void onMessageCreate(MessageCreateEvent event) {
    }

    public void onMessageDelete(MessageDeleteEvent event) {
    }

    public void onMessageUpdate(MessageUpdateEvent event) {
    }

    public void onMessageBulkDelete(MessageBulkDeleteEvent event) {
    }

    public void onReactionAdd(ReactionAddEvent event) {
    }

    public void onReactionRemove(ReactionRemoveEvent event) {
    }

    public void onReactionRemoveEmoji(ReactionRemoveEmojiEvent event) {
    }

    public void onReactionRemoveAll(ReactionRemoveAllEvent event) {
    }

    // Guild related
    public void onGuildCreate(GuildCreateEvent event) {
    }

    public void onGuildDelete(GuildDeleteEvent event) {
    }

    public void onGuildUpdate(GuildUpdateEvent event) {
    }

    public void onMemberJoin(MemberJoinEvent event) {
    }

    public void onMemberLeave(MemberLeaveEvent event) {
    }

    public void onMemberUpdate(MemberUpdateEvent event) {
    }

    public void onMemberChunk(MemberChunkEvent event) {
    }

    public void onEmojisUpdate(EmojisUpdateEvent event) {
    }

    public void onBan(BanEvent event) {
    }

    public void onUnban(UnbanEvent event) {
    }

    public void onIntegrationsUpdate(IntegrationsUpdateEvent event) {
    }

    public void onWebhooksUpdate(WebhooksUpdateEvent event) {
    }

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final ConcurrentMap<Class<?>, MethodHandle> methods = new ConcurrentHashMap<>();
    private static final Set<Class<?>> unresolved;

    static {
        unresolved = ConcurrentHashMap.newKeySet();
        Collections.addAll(unresolved,
            Object.class, // Objects aren't events
            Event.class // onEvent is final and would never be found
        );
    }

    @Override
    public final void onEvent(@Nonnull Event event) {
        for (Class<?> clazz : ClassWalker.range(event.getClass(), Event.class)) {
            if (unresolved.contains(clazz)) {
                continue;
            }
            MethodHandle mh = methods.computeIfAbsent(clazz, ListenerAdapter::findMethod);
            if (mh == null) {
                unresolved.add(clazz);
                continue;
            }

            try {
                mh.invoke(this, event);
            } catch (Throwable throwable) {
                if (throwable instanceof RuntimeException) {
                    throw (RuntimeException) throwable;
                }
                if (throwable instanceof Error) {
                    throw (Error) throwable;
                }
                throw new IllegalStateException(throwable);
            }
        }
    }

    private static MethodHandle findMethod(Class<?> clazz) {
        String name = clazz.getSimpleName();
        MethodType type = MethodType.methodType(Void.TYPE, clazz);
        try {
            name = "on" + name.substring(0, name.length() - "Event".length());
            return lookup.findVirtual(ListenerAdapter.class, name, type);
        } catch (NoSuchMethodException | IllegalAccessException ignored) {
        }
        return null;
    }

}
