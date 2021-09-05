/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2021 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.modules.logging.users;

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.console.MMDMarkers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.time.Instant;

/**
 * The type Event nickname changed.
 *
 * @author
 */
public final class EventNicknameChanged extends ListenerAdapter {

    /**
     * On guild member update nickname.
     *
     * @param event the event
     */
    @Override
    public void onGuildMemberUpdateNickname(final GuildMemberUpdateNicknameEvent event) {
        final var guild = event.getGuild();

        if (MMDBot.getConfig().getGuildID() != guild.getIdLong()) {
            return; // Make sure that we don't post if it's not related to 'our' guild
        }

        final long channelID = MMDBot.getConfig().getChannel("events.basic");
        Utils.getChannelIfPresent(channelID, channel ->
            guild.retrieveAuditLogs()
                .type(ActionType.MEMBER_UPDATE)
                .limit(1)
                .cache(false)
                .map(list -> list.get(0))
                .flatMap(entry -> {
                    final var embed = new EmbedBuilder();
                    final var target = event.getUser();

                    embed.setColor(Color.YELLOW);
                    embed.setTitle("Nickname Changed");
                    embed.setThumbnail(target.getEffectiveAvatarUrl());
                    embed.addField("User:", target.getAsMention() + " (" + target.getId() + ")",
                        true);
                    embed.setTimestamp(Instant.now());
                    if (entry.getTargetIdLong() != target.getIdLong()) {
                        MMDBot.LOGGER.warn(MMDMarkers.EVENTS, "Inconsistency between target of retrieved audit log "
                                + "entry and actual nickname event target: retrieved is {}, but target is {}",
                            entry.getUser(), target);
                    } else if (entry.getUser() != null) {
                        final var editor = entry.getUser();
                        embed.addField("Nickname Editor:", editor.getAsMention() + " ("
                            + editor.getId() + ")", true);
                        embed.addBlankField(true);
                    }
                    final String oldNick = event.getOldNickname() == null ? target.getName() : event.getOldNickname();
                    final String newNick = event.getNewNickname() == null ? target.getName() : event.getNewNickname();
                    embed.addField("Old Nickname:", oldNick, true);
                    embed.addField("New Nickname:", newNick, true);

                    MMDBot.LOGGER.info(MMDMarkers.EVENTS, "User {} changed nickname from `{}` to `{}`, by {}",
                        target, oldNick, newNick,
                        entry.getUser());

                    return channel.sendMessageEmbeds(embed.build());
                })
                .queue()
        );
    }
}
