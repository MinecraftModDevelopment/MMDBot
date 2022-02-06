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
package com.mcmoddev.mmdbot.modules.logging.misc;

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.TaskScheduler;
import com.mcmoddev.mmdbot.utilities.ThreadedEventListener;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadChannelCreatorEvents extends ListenerAdapter {

    private final Map<Type, List<Long>> caches = new HashMap<>();

    @Override
    public void onMessageReceived(@NotNull final MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.isFromThread() || event.isWebhookMessage() || event.getAuthor().isBot() || event.getAuthor().isSystem()) {
            return;
        }
        if (event.getChannel().getIdLong() == MMDBot.getConfig().getChannel("requests.main")) {
            createThread(event, Type.REQUEST);
        }
        if (event.getChannel().getIdLong() == MMDBot.getConfig().getChannel("free_mod_ideas")) {
            createThread(event, Type.IDEA);
        }
    }

    @Override
    public void onMessageUpdate(@NotNull final MessageUpdateEvent event) {
        if (!event.isFromGuild() || event.isFromThread() || event.getAuthor().isBot() || event.getAuthor().isSystem()) {
            return;
        }
        if (event.getChannel().getIdLong() == MMDBot.getConfig().getChannel("requests.main")) {
            notifyMessageDeleted(event, Type.REQUEST);
        }
        if (event.getChannel().getIdLong() == MMDBot.getConfig().getChannel("free_mod_ideas")) {
            notifyMessageDeleted(event, Type.IDEA);
        }
    }

    private void createThread(final MessageReceivedEvent event, final Type threadType) {
        final var author = event.getMember();
        if (caches.computeIfAbsent(threadType, k -> new ArrayList<>()).contains(author.getIdLong())) {
            return;
        }
        final var threadTypeStr = threadType.toString();
        event.getMessage().createThreadChannel("Discussion of %s’s %s".formatted(author.getUser().getName(), threadTypeStr)).queue(thread -> {
            thread.addThreadMember(author).queue($ -> {
                thread.sendMessageEmbeds(new EmbedBuilder().setTitle("%s discussion thread".formatted(Utils.uppercaseFirstLetter(threadTypeStr)))
                        .setTimestamp(Instant.now()).setColor(Color.CYAN).setDescription("""
                            **This thread is intended for discussing %s's %s. The %s:**
                            %s""".formatted(author.getAsMention(), threadTypeStr, threadTypeStr, event.getMessage().getContentRaw())).build())
                    .queue(msg -> msg.pin().queue());
                caches.computeIfAbsent(threadType, k -> new ArrayList<>()).add(author.getIdLong());
                TaskScheduler.scheduleTask(() -> caches.get(threadType).remove(author.getIdLong()), 100, TimeUnit.MINUTES);
            });
        });
    }

    private void notifyMessageDeleted(final MessageUpdateEvent event, final Type threadType) {
        final var author = event.getMember();
        final var threadTypeStr = threadType.toString();
        final var thread = event.getGuild().getThreadChannelById(event.getMessageId());
        if (thread != null) {
            thread.sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.CYAN).setDescription("""
                        **%s's original %s has been edited. New content:**
                        %s""".formatted(author.getAsMention(), threadTypeStr, event.getMessage().getContentRaw()))
                    .setTitle(Utils.uppercaseFirstLetter(threadTypeStr) + " edited!").setTimestamp(Instant.now()).build())
                .queue(ms -> ms.pin().queue());
        }
    }

    public enum Type {
        REQUEST("request"),
        IDEA("idea");

        private final String name;

        Type(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
