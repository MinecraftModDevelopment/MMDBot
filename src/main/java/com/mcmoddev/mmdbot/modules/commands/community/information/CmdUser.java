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
package com.mcmoddev.mmdbot.modules.commands.community.information;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.modules.commands.DismissListener;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;

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
 * @author KiriCattus
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
        aliases = new String[]{"whoami", "myinfo", "me"};
        category = new Category("Info");
        arguments = "<userID/mention>";
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

        final var embed = createMemberEmbed(Utils.getArgumentOr(event, "user", OptionMapping::getAsMember, event.getMember()));
        event.replyEmbeds(embed.build()).addActionRow(DismissListener.createDismissButton(event)).mentionRepliedUser(false).queue();
    }

    /**
     * Create member embed builder.
     *
     * @param member the member
     * @return EmbedBuilder. embed builder
     */
    public static EmbedBuilder createMemberEmbed(final Member member) {
        final var user = member.getUser();
        final var embed = new EmbedBuilder();
        final var dateJoinedDiscord = member.getTimeCreated().toInstant();
        final var dateJoinedServer = Utils.getMemberJoinTime(member);

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
        if (dateJoinedServer != null) {
            embed.addField("Joined MMD:", date.format(dateJoinedServer.toEpochMilli()), true);
            embed.addField("Member for:", Utils.getTimeDifference(Utils.getTimeFromUTC(dateJoinedServer),
                OffsetDateTime.now(ZoneOffset.UTC)), true);
        }
        embed.setTimestamp(Instant.now());

        return embed;
    }
}
