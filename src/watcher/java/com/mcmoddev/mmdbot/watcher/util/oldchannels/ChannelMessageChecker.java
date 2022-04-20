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
package com.mcmoddev.mmdbot.watcher.util.oldchannels;

import com.mcmoddev.mmdbot.watcher.TheWatcher;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

/**
 * The type Channel message checker.
 *
 * @author williambl
 */
public record ChannelMessageChecker(JDA jda) implements Runnable {

    /**
     * Run.
     */
    @Override
    public void run() {
        OldChannelsHelper.clear();
        jda.getGuilds().forEach(guild -> {
            final var currentTime = Instant.now();
            final Member self = guild.getSelfMember();

            CompletableFuture.allOf(guild.getTextChannelCache()
                    .parallelStreamUnordered()
                    .filter(self::hasAccess)
                    .filter(channel -> self.hasPermission(channel, Permission.MESSAGE_HISTORY))
                    .map(channel -> channel.getIterableHistory()
                        .takeAsync(1000).thenAccept(messages -> messages.stream()
                            .filter(message -> !message.isWebhookMessage() && !message.getType().isSystem())
                            .findFirst()
                            .ifPresent(message -> {
                                final long daysSinceLastMessage = ChronoUnit.DAYS.between(message.getTimeCreated()
                                    .toInstant(), currentTime);
                                OldChannelsHelper.put(message.getTextChannel(), daysSinceLastMessage);
                            })
                        )).toArray(CompletableFuture[]::new))
                .thenAccept(v -> OldChannelsHelper.setReady(true));
        });
    }
}
