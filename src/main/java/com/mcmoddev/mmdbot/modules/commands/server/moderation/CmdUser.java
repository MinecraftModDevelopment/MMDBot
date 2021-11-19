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

import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Shows information about a particular user.
 * Includes:
 * - Username
 * - Discriminator
 * - ID
 * - Nickname, if applied
 * - Discord join date
 * - Guild join date
 * - Account age
 *
 * @author ProxyNeko
 * @author WillBL
 * @author sciwhiz12
 * @author Curle
 */
public class CmdUser extends SlashCommand {

    /**
     * Instantiates a new Cmd user.
     */
    public CmdUser() {
        super();
        name = "user";
        help = "Get information about another user.";
        category = new Category("Moderation");
        arguments = "<userID/mention";

        enabledRoles = new String[]{"Staf"};
        guildOnly = true;

        OptionData data = new OptionData(OptionType.USER, "user", "The user to check.").setRequired(false);
        List<OptionData> dataList = new ArrayList<>();
        dataList.add(data);
        this.options = dataList;
    }

    /**
     * Execute.
     *
     * @param event The {@link SlashCommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        final var member = event.getOption("user");
        if (member == null) {
            event.reply(String.format("User %s not found.", event.getOption("user").getAsString())).mentionRepliedUser(false).queue();
            return;
        }

        final var embed = createMemberEmbed(member.getAsMember());
        event.replyEmbeds(embed.build()).mentionRepliedUser(false).setEphemeral(true).queue();
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
