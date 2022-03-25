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
package com.mcmoddev.mmdbot.commander.util;

import com.google.gson.JsonParser;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Slf4j
@UtilityClass
public class TheCommanderUtilities {

    /**
     * Gets a cat fact.
     *
     * @return a cat fact
     */
    public static String getCatFact() {
        try {
            final var url = new URL("https://catfact.ninja/fact");
            final URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10 * 1000);
            final var reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            final String inputLine = reader.readLine();
            reader.close();
            final var objectArray = JsonParser.parseString(inputLine).getAsJsonObject();
            return ":cat:  " + objectArray.get("fact").toString();

        } catch (final RuntimeException ex) {
            throw ex;
        } catch (final Exception ex) {
            log.error("Error getting cat fact...", ex);
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * Checks if the given member any of the given roles
     *
     * @param member the member to check
     * @param roleIds the IDs of the roles to check for
     * @return if the member has any of the role
     */
    public static boolean memberHasRoles(@NonNull final Member member, @NonNull final List<String> roleIds) {
        if (member == null) {
            return false;
        }
        return member.getRoles().stream().anyMatch(r -> roleIds.contains(r.getId()));
    }

    /**
     * Checks if the given member any of the given roles
     *
     * @param member the member to check
     * @param roleIds the IDs of the roles to check for
     * @return if the member has any of the role
     */
    public static boolean memberHasRoles(@NonNull final Member member, @NonNull final String... roleIds) {
        return memberHasRoles(member, Arrays.asList(roleIds));
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
            embed.addField("Users nickname:", "No nickname applied.", true);
        } else {
            embed.addField("Users nickname:", member.getNickname(), true);
        }

        final var date = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
        embed.addField("Joined Discord:", date.format(dateJoinedDiscord.toEpochMilli()), true);
        embed.addField("Joined Server:", TimeFormat.RELATIVE.format(dateJoinedServer), true);
        embed.setTimestamp(Instant.now());

        return embed;
    }

}
