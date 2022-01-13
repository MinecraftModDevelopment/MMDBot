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
package com.mcmoddev.mmdbot.modules.logging.misc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.mcmoddev.mmdbot.MMDBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Scam detection system
 * @author matyrobbrt
 */
public class ScamDetector extends ListenerAdapter {

    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final String SCAM_LINKS_DATA_URL = "https://phish.sinking.yachts/v2/all";

    public static final List<String> SCAM_LINKS = Collections.synchronizedList(new ArrayList<>());

    static {
        new Thread(ScamDetector::setupScamLinks, "Scam link collector").start();
    }

    @Override
    public void onGuildMessageReceived(@NotNull final GuildMessageReceivedEvent event) {
        final var msg = event.getMessage();
        if (containsScam(msg)) {
            final var member = msg.getMember();
            final var guild = msg.getGuild();
            final var mutedRoleID = MMDBot.getConfig().getRole("muted");
            final var embed = getLoggingEmbed(msg, "");
            msg.delete().queue($ -> {
                getLoggingChannel(guild).sendMessageEmbeds(embed).queue();
                // TODO once JDA is updated, maybe timeout the user. It seems a better idea
                guild.addRoleToMember(member, guild.getRoleById(mutedRoleID)).queue();
            });
        }
    }

    @Override
    public void onGuildMessageUpdate(@NotNull final GuildMessageUpdateEvent event) {
        final var msg = event.getMessage();
        if (containsScam(msg)) {
            final var member = msg.getMember();
            final var msgChannel = msg.getTextChannel();
            final var guild = msg.getGuild();
            final var mutedRoleID = MMDBot.getConfig().getRole("muted");
            final var embed = getLoggingEmbed(msg, ", by editing an old message");
            msg.delete().queue($ -> {
                getLoggingChannel(guild).sendMessageEmbeds(embed).queue();
                // TODO once JDA is updated, maybe timeout the user. It seems a better idea
                guild.addRoleToMember(member, guild.getRoleById(mutedRoleID)).queue();
            });
        }
    }

    private static MessageEmbed getLoggingEmbed(final Message message, final String extraDescription) {
        final var member = message.getMember();
        return new EmbedBuilder().setTitle("Scam link detected!")
            .setDescription(String.format("User %s sent a scam link in %s%s! Their message was deleted, and they were muted!", member.getAsMention(),
                message.getTextChannel().getAsMention(), extraDescription))
            .addField("Message Content", "> " + message.getContentRaw(), false)
            .setColor(Color.RED)
            .setTimestamp(Instant.now())
            .setFooter("User ID: " + member.getIdLong())
            .setThumbnail(member.getEffectiveAvatarUrl()).build();
    }

    private static TextChannel getLoggingChannel(final Guild guild) {
        return guild.getTextChannelById(MMDBot.getConfig().getChannel("events.requests_deletion"));
    }

    public static boolean containsScam(final Message message) {
        final String msgContent = message.getContentRaw().toLowerCase();
        for (final var link : SCAM_LINKS) {
            if (msgContent.contains(link)) {
                return true;
            }
        }
        return false;
    }

    private static void setupScamLinks() {
        MMDBot.LOGGER.debug("Setting up scam links! Receiving data from {}.", SCAM_LINKS_DATA_URL);
        try (var is = new URL(SCAM_LINKS_DATA_URL).openStream()) {
            final String result = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            GSON.fromJson(result, JsonArray.class)
                .forEach(link -> SCAM_LINKS.add(link.getAsString()));
        } catch (final IOException e) {
            MMDBot.LOGGER.error("Error while setting up scam links!", e);
        }
    }
}
