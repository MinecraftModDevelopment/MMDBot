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
package com.mcmoddev.mmdbot.commander.custompings;

import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.core.util.Utils;
import com.mcmoddev.mmdbot.core.util.event.ThreadedEventListener;
import io.github.matyrobbrt.eventdispatcher.LazySupplier;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;

public class CustomPingsListener extends ListenerAdapter {
    public static final LazySupplier<EventListener> LISTENER = LazySupplier.of(() -> new ThreadedEventListener(Executors.newSingleThreadExecutor(r -> Utils.setThreadDaemon(new Thread(r, "CustomPingsListener"), true)),
        new CustomPingsListener()));

    private CustomPingsListener() {}
    @Override
    public void onMessageReceived(@NotNull final MessageReceivedEvent event) {
        if (event.isFromGuild() && TheCommander.getInstance().getGeneralConfig().features().customPings().areEnabled()
            && event.getAuthor().getIdLong() != event.getJDA().getSelfUser().getIdLong()) {
            handlePings(event.getGuild(), event.getMessage());
        }
    }

    public static void handlePings(final @NonNull Guild guild, @NonNull final Message message) {
        final var author = message.getAuthor();
        final var allPings = CustomPings.getAllPingsInGuild(guild.getIdLong());
        allPings.forEach((userId, pings) -> {
            if (pings.isEmpty()) return;
            if (userId == author.getIdLong()) return;
            final var user = guild.getMemberById(userId);
            if (user == null) {
                // Member left the guild. Remove pings
                CustomPings.clearPings(guild.getIdLong(), userId);
                return;
            }
            // They can't view the channel
            if (!canViewChannel(guild, user, message.getTextChannel())) return;
            user.getUser().openPrivateChannel().queue(privateChannel -> {
                final var dmActions = pings.stream().filter(p -> p.test(message))
                    .map(p -> sendPingMessage(p, message, privateChannel))
                    .toList();
                if (!dmActions.isEmpty()) {
                    RestAction.allOf(dmActions).queue();
                }
            }, $ -> /* Can't DM, so clear pings */ CustomPings.clearPings(guild.getIdLong(), userId));
        });
    }

    public static MessageAction sendPingMessage(final CustomPing ping, final Message message, final MessageChannel channel) {
        return channel.sendMessageEmbeds(
            new EmbedBuilder()
                .setAuthor("New ping from: %s".formatted(message.getAuthor().getName()), message.getJumpUrl(), message.getAuthor().getAvatarUrl())
                .addField(ping.text(), message.getContentRaw().isBlank() ? "[Blank]" : message.getContentRaw(), false)
                .addField("Link", message.getJumpUrl(), false)
                .setTimestamp(message.getTimeCreated())
                .build()
        );
    }

    public static boolean canViewChannel(Guild guild, Member member, TextChannel channel) {
        return member.getPermissions(channel).contains(Permission.VIEW_CHANNEL);
    }
}
