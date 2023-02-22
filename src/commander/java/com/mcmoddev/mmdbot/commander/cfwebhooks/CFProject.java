/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2023 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.commander.cfwebhooks;

import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import com.mcmoddev.mmdbot.commander.TheCommander;
import io.github.matyrobbrt.curseforgeapi.request.AsyncRequest;
import io.github.matyrobbrt.curseforgeapi.request.Response;
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public record CFProject(int projectId, Set<Long> channels, AtomicInteger lastFoundFile) implements Runnable {

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
                        final var ch = TheCommander.getJDA().getChannelById(IWebhookContainer.class, channelId);
                        if (ch == null) {
                            channels.remove(channelId);
                            return;
                        }
                        CFUtils.WEBHOOKS.sendAndCrosspost(
                            ch, WebhookMessage.embeds(
                                WebhookEmbedBuilder.fromJDA(embed.build())
                                    .build()
                            )
                        );
                    });
                });
        } catch (Exception e) {
            TheCommander.LOGGER.error("Exception while trying to send CurseForge update message!", e);
        }
    }
}
