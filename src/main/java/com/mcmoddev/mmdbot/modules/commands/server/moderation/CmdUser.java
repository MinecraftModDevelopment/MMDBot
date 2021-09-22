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
package com.mcmoddev.mmdbot.modules.commands.server.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

/**
 * The type Cmd user.
 *
 * @author ProxyNeko
 * @author WillBL
 * @author sciwhiz12
 */
public class CmdUser extends Command {

    /**
     * Instantiates a new Cmd user.
     */
    public CmdUser() {
        super();
        name = "user";
        help = "Get information about another user.";
        category = new Category("Moderation");
        arguments = "<userID/mention";
        requiredRole = "Staff";
        guildOnly = true;
    }

    /**
     * Execute.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        final var channel = event.getMessage();
        final var member = Utils.getMemberFromString(event.getArgs(), event.getGuild());
        if (member == null) {
            channel.reply(String.format("User %s not found.", event.getArgs())).mentionRepliedUser(false).queue();
            return;
        }

        final var embed = createMemberEmbed(member);
        channel.replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
    }

    /**
     * Create member embed builder.
     *
     * @param member the member
     * @return EmbedBuilder. embed builder
     */
    protected EmbedBuilder createMemberEmbed(final Member member) {
        final var user = member.getUser();
        final var embed = new EmbedBuilder();
        final var dateJoinedDiscord = member.getTimeCreated().toInstant();
        final var dateJoinedMMD = Utils.getMemberJoinTime(member);

        embed.setTitle("User info");
        embed.setColor(Color.WHITE);
        embed.setThumbnail(user.getEffectiveAvatarUrl());
        embed.addField("Username:", user.getName(), true);
        embed.addField("Users discriminator:", "#" + user.getDiscriminator(), true);
        embed.addField("Users id:", member.getId(), true);

        if (member.getNickname() == null) {
            embed.addField("Users nickname:", "No nickname applied.", true);
        } else {
            embed.addField("Users nickname:", member.getNickname(), true);
        }

        final var date = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
        embed.addField("Joined Discord:", date.format(dateJoinedDiscord.toEpochMilli()), true);
        embed.addField("Joined MMD:", date.format(dateJoinedMMD.toEpochMilli()), true);
        embed.addField("Member for:", Utils.getTimeDifference(Utils.getTimeFromUTC(dateJoinedMMD),
                OffsetDateTime.now(ZoneOffset.UTC)), true);
        embed.setTimestamp(Instant.now());

        return embed;
    }
}
