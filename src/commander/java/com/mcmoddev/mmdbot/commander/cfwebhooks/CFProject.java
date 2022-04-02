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
package com.mcmoddev.mmdbot.commander.cfwebhooks;

import com.mcmoddev.mmdbot.commander.TheCommander;
import io.github.matyrobbrt.curseforgeapi.request.AsyncRequest;
import io.github.matyrobbrt.curseforgeapi.request.Response;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public record CFProject(int projectId, Set<Long> channels, AtomicInteger lastFoundFile) implements Runnable {
    private static final Collection<Message.MentionType> ALLOWED_MENTIONS = Set.of();

    @Override
    public void run() {
        final var manager = TheCommander.getInstance().getCurseForgeManager().orElseThrow();
        final var api = manager.api();
        final var allProjects = manager.projects();

        try {
            api.getAsyncHelper().getMod(projectId)
                .flatMap(r -> r.flatMap(m -> {
                        if (m.latestFilesIndexes().isEmpty() || (m.latestFilesIndexes().get(0).fileId() <= lastFoundFile.get())) {
                            return Response.empty(r.getStatusCode());
                        }
                        return r;
                    }).mapOrElseWithException(m -> {
                        final var latestFile = m.latestFilesIndexes().get(0).fileId();
                        final var toRet = CFUtils.createWebhookFileEmbed(m, latestFile);
                        lastFoundFile.set(latestFile);
                        allProjects.save();
                        return toRet;
                    }, AsyncRequest::empty, t -> AsyncRequest.empty())
                )
                .queue(embed -> {
                    channels.forEach(channelId -> {
                        CFUtils.getWebhookClient(channelId)
                            .send(new MessageBuilder()
                                .setEmbeds(embed.build())
                                .setAllowedMentions(ALLOWED_MENTIONS)
                                .build())
                            .thenAccept(msg -> {
                                final var channel = TheCommander.getJDA().getChannelById(MessageChannel.class, msg.getChannelId());
                                if (channel != null && channel.getType() == ChannelType.NEWS) {
                                    channel.retrieveMessageById(msg.getId()).flatMap(Message::crosspost).queue();
                                }
                            });
                    });
                });
        } catch (Exception e) {
            TheCommander.LOGGER.error("Exception while trying to send CurseForge update message!", e);
        }
    }
}
