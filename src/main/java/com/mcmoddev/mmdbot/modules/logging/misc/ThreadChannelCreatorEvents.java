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
import com.mcmoddev.mmdbot.utilities.ThreadedEventListener;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ThreadChannelCreatorEvents extends ListenerAdapter {

    private static final Executor THREAD_POOL = Executors.newSingleThreadExecutor(r -> Utils.setThreadDaemon(new Thread(r, "ThreadCreator"), true));

    public static EventListener create() {
        return new ThreadedEventListener(new ThreadChannelCreatorEvents(), THREAD_POOL);
    }

    private ThreadChannelCreatorEvents() {

    }

    @Override
    public void onMessageReceived(@NotNull final MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.isFromThread()) {
            return;
        }
        final var author = event.getMember();
        if (event.getChannel().getIdLong() == MMDBot.getConfig().getChannel("requests.main")) {
            event.getMessage().createThreadChannel("Discussion of %s’s request".formatted(author.getUser().getName())).queue(thread -> {
                thread.addThreadMember(author).queue($ -> {
                    thread.sendMessageEmbeds(new EmbedBuilder().setTitle("Request discussion thread")
                        .setColor(Color.CYAN).setDescription("""
                            **This thread is intended for discussing %s's request. The request:**

                            %s""".formatted(author.getAsMention(), event.getMessage().getContentRaw())).build()).queue();
                });
            });
        }
    }
}
