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
package com.mcmoddev.mmdbot.commander.util;

import com.mcmoddev.mmdbot.commander.config.Configuration;
import com.mcmoddev.mmdbot.core.util.TaskScheduler;
import com.mcmoddev.mmdbot.core.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ThreadChannelCreatorEvents extends ListenerAdapter {

    private final Map<Type, List<Long>> caches = new HashMap<>();
    private final Supplier<Configuration> configGetter;

    public ThreadChannelCreatorEvents(final Supplier<Configuration> configGetter) {
        this.configGetter = configGetter;
    }

    @Override
    public void onMessageReceived(@NotNull final MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.isFromThread() || event.isWebhookMessage() || event.getAuthor().isBot() || event.getAuthor().isSystem()) {
            return;
        }
        final var cfg = configGetter.get();
        if (cfg == null) return;
        if (cfg.channels().requests().test(event.getChannel())) {
            createThread(event, Type.REQUEST);
        }
        if (cfg.channels().freeModIdeas().test(event.getChannel())) {
            createThread(event, Type.IDEA);
        }
    }

    @Override
    public void onMessageUpdate(@NotNull final MessageUpdateEvent event) {
        if (!event.isFromGuild() || event.isFromThread() || event.getAuthor().isBot() || event.getAuthor().isSystem()) {
            return;
        }
        final var cfg = configGetter.get();
        if (cfg == null) return;
        if (cfg.channels().requests().test(event.getChannel())) {
            notifyMessageEdited(event, Type.REQUEST);
        }else if (cfg.channels().freeModIdeas().test(event.getChannel())) {
            notifyMessageEdited(event, Type.IDEA);
        }
    }

    @Override
    public void onMessageDelete(@NotNull final MessageDeleteEvent event) {
        if (!event.isFromGuild() || event.isFromThread()) {
            return;
        }
        final var cfg = configGetter.get();
        if (cfg == null) return;
        if (cfg.channels().requests().test(event.getChannel()) || cfg.channels().freeModIdeas().test(event.getChannel())) {
            notifyMessageDeleted(event);
        }
    }

    private static final String THREAD_EMOJI = "🧵";

    @Override
    public void onMessageReactionAdd(@NotNull final MessageReactionAddEvent event) {
        if (!event.isFromGuild() || event.isFromThread()) {
            return;
        }
        final var cfg = configGetter.get();
        if (cfg == null) return;
        if (cfg.channels().requests().test(event.getChannel())) {
            createThread(event, Type.REQUEST);
        } else if (cfg.channels().freeModIdeas().test(event.getChannel())) {
            createThread(event, Type.IDEA);
        }
    }

    private void createThread(final MessageReactionAddEvent event, final Type type) {
        if (event.getReaction().getEmoji().getName().equals(THREAD_EMOJI)) {
            event.retrieveMessage().queue(message -> {
                final var thread = message.getStartedThread();
                if (thread == null) {
                    createThread(new MessageReceivedEvent(event.getJDA(), 0, message), type);
                }
                event.getReaction().clearReactions().queue();
            });
        }
    }

    private void createThread(final MessageReceivedEvent event, final Type threadType) {
        final var author = event.getMember();
        if (caches.computeIfAbsent(threadType, k -> new ArrayList<>()).contains(author.getIdLong())) {
            return;
        }
        final var threadTypeStr = threadType.toString();
        if (event.getMessage().getType() != MessageType.DEFAULT) {
            return;
        }
        event.getMessage().createThreadChannel("Discussion of %s’s %s".formatted(author.getEffectiveName(), threadTypeStr)).queue(thread -> {
            thread.addThreadMember(author).queue($ -> {
                thread.sendMessageEmbeds(new EmbedBuilder().setTitle("%s discussion thread".formatted(Utils.uppercaseFirstLetter(threadTypeStr)))
                        .setTimestamp(Instant.now()).setColor(Color.CYAN).setDescription("""
                            **This thread is intended for discussing %s's %s. The %s:**
                            %s""".formatted(author.getAsMention(), threadTypeStr, threadTypeStr, event.getMessage().getContentRaw())).build())
                    .queue(msg -> msg.pin().queue());
                caches.computeIfAbsent(threadType, k -> new ArrayList<>()).add(author.getIdLong());
                TaskScheduler.scheduleTask(() -> caches.get(threadType).remove(author.getIdLong()), 30, TimeUnit.MINUTES);
            });
        });
    }

    private void notifyMessageEdited(final MessageUpdateEvent event, final Type threadType) {
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

    private void notifyMessageDeleted(final MessageDeleteEvent event) {
        final var thread = event.getGuild().getThreadChannelById(event.getMessageId());
        if (thread != null) {
            thread.sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setDescription("The original message of this thread has been deleted! This thread will now be archived!")
                    .setTimestamp(Instant.now()).build())
                .queue(ms -> thread.getManager().setArchived(true).queue());
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
