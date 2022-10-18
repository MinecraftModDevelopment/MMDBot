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
package com.mcmoddev.mmdbot.thelistener.events;

import com.mcmoddev.mmdbot.core.util.Utils;
import com.mcmoddev.mmdbot.core.util.webhook.WebhookManager;
import com.mcmoddev.mmdbot.thelistener.TheListener;
import com.mcmoddev.mmdbot.thelistener.util.LoggingType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.Instant;

public final class LeaveJoinEvents extends ListenerAdapter {

    private static final WebhookManager WEBHOOKS = WebhookManager.of("LeaveJoinLogging");

    @Override
    public void onGuildMemberJoin(@NotNull final GuildMemberJoinEvent event) {
        final var embed = new EmbedBuilder()
            .setColor(Color.GREEN)
            .setTitle("User Joined")
            .addField("User:", event.getMember().getUser().getAsTag(), true)
            .setFooter("User ID: " + event.getMember().getId(), event.getMember().getEffectiveAvatarUrl())
            .setTimestamp(Instant.now())
            .build();

        TheListener.getInstance().getConfigForGuild(event.getGuild().getIdLong())
            .getChannelsForLogging(LoggingType.LEAVE_JOIN_EVENTS)
            .forEach(snowflakeValue -> {
                final var ch = snowflakeValue.resolve(id -> event.getJDA().getChannelById(MessageChannel.class, id));
                if (ch instanceof TextChannel textChannel) {
                    WEBHOOKS.getWebhook(textChannel)
                        .send(Utils.webhookMessage(embed)
                            .setUsername(event.getMember().getEffectiveName())
                            .setAvatarUrl(event.getMember().getEffectiveAvatarUrl())
                            .build());
                } else if (ch != null) {
                    ch.sendMessageEmbeds(embed).queue();
                }
            });
    }

    @Override
    public void onGuildMemberRemove(@NotNull final GuildMemberRemoveEvent event) {
        if (event.getMember() == null) return;
        final var embed = new EmbedBuilder()
            .setColor(Color.RED)
            .setTitle("User Left")
            .addField("User:", event.getUser().getAsTag(), true)
            .addField("Roles", RoleEvents.mentionsOrEmpty(event.getMember().getRoles()), true)
            .setFooter("User ID: " + event.getMember().getId(), event.getMember().getEffectiveAvatarUrl())
            .setTimestamp(Instant.now())
            .build();

        TheListener.getInstance().getConfigForGuild(event.getGuild().getIdLong())
            .getChannelsForLogging(LoggingType.LEAVE_JOIN_EVENTS)
            .forEach(snowflakeValue -> {
                final var ch = snowflakeValue.resolve(id -> event.getJDA().getChannelById(MessageChannel.class, id));
                if (ch instanceof TextChannel textChannel) {
                    WEBHOOKS.getWebhook(textChannel)
                        .send(Utils.webhookMessage(embed)
                            .setUsername(event.getMember().getEffectiveName())
                            .setAvatarUrl(event.getMember().getEffectiveAvatarUrl())
                            .build());
                } else if (ch != null) {
                    ch.sendMessageEmbeds(embed).queue();
                }
            });
    }
}
