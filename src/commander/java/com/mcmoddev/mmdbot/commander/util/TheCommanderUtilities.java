/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2024 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.commander.util;

import com.mcmoddev.mmdbot.core.util.config.SnowflakeValue;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Slf4j
@UtilityClass
public class TheCommanderUtilities {

    public static String readInputStream(InputStream stream) throws IOException {
        StringBuilder content = new StringBuilder();

        try (stream; InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8); BufferedReader buffer = new BufferedReader(reader)) {
            String line;
            while ((line = buffer.readLine()) != null) {
                content.append(line).append('\n');
            }
        }

        return content.toString();
    }


    /**
     * Checks if the given member any of the given roles
     *
     * @param member  the member to check
     * @param roleIds the IDs of the roles to check for
     * @return if the member has any of the role
     */
    public static boolean memberHasRolesString(final Member member, @NonNull final List<String> roleIds) {
        if (member == null) {
            return false;
        }
        return member.getRoles().stream().anyMatch(r -> roleIds.contains(r.getId()));
    }

    /**
     * Checks if the given member any of the given roles
     *
     * @param member  the member to check
     * @param roleIds the IDs of the roles to check for
     * @return if the member has any of the role
     */
    public static boolean memberHasRoles(final Member member, @NonNull final List<SnowflakeValue> roleIds) {
        return memberHasRolesString(member, roleIds.stream().map(SnowflakeValue::asString).toList());
    }

    /**
     * Checks if the given member any of the given roles
     *
     * @param member  the member to check
     * @param roleIds the IDs of the roles to check for
     * @return if the member has any of the role
     */
    public static boolean memberHasRoles(final Member member, @NonNull final String... roleIds) {
        return memberHasRolesString(member, Arrays.asList(roleIds));
    }

    /**
     * Creates an embed for the information of a member.
     *
     * @param member the member
     * @return EmbedBuilder. embed builder
     */
    public static EmbedBuilder createMemberInfoEmbed(final Member member) {
        final var user = member.getUser();
        final var embed = new EmbedBuilder();
        final var dateJoinedDiscord = member.getTimeCreated().toInstant();
        final var dateJoinedServer = member.getTimeJoined();

        embed.setTitle("User info");
        embed.setColor(Color.WHITE);
        embed.setThumbnail(user.getEffectiveAvatarUrl());
        embed.addField("Username:", user.getName(), true);
        embed.addField("Users discriminator:", "#" + user.getDiscriminator(), true);
        embed.addField("Users id:", member.getId(), true);

        if (member.getNickname() == null) {
            embed.addField("User's nickname:", "No nickname applied.", true);
        } else {
            embed.addField("User's nickname:", member.getNickname(), true);
        }

        final var date = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
        embed.addField("Joined Discord:", date.format(dateJoinedDiscord.toEpochMilli()), true);
        embed.addField("Joined Server:", TimeFormat.RELATIVE.format(dateJoinedServer), true);
        embed.setTimestamp(Instant.now());

        return embed;
    }

}
