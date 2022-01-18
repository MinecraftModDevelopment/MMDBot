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
import com.google.gson.JsonElement;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.console.MMDMarkers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

/**
 * Scam detection system
 * @author matyrobbrt
 */
public class ScamDetector extends ListenerAdapter {

    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final String SCAM_LINKS_DATA_URL = "https://phish.sinking.yachts/v2/all";

    public static final Set<String> SCAM_LINKS = Collections.synchronizedSet(new HashSet<>());

    static {
        new Thread(ScamDetector::setupScamLinks, "Scam link collector").start();
    }

    @Override
    public void onGuildMessageReceived(@Nonnull final GuildMessageReceivedEvent event) {
        takeActionIfScam(event.getMessage(), "");
    }

    @Override
    public void onGuildMessageUpdate(@Nonnull final GuildMessageUpdateEvent event) {
        takeActionIfScam(event.getMessage(), ", by editing an old message");
    }

    public static void takeActionIfScam(@Nonnull final Message msg, @Nonnull final String loggingReason) {
        final var member = msg.getMember();
        if (member == null || msg.getAuthor().isBot() || msg.getAuthor().isSystem() ||
            member.hasPermission(Permission.MANAGE_CHANNEL)) {
            return;
        }
        if (containsScam(msg)) {
            final var guild = msg.getGuild();
            final var embed = getLoggingEmbed(msg, loggingReason);
            msg.delete().reason("Scam link").queue($ -> {
                executeInLoggingChannel(channel -> channel.sendMessageEmbeds(embed).queue());
                mute(guild, member);
            });
        }
    }

    private static void mute(final Guild guild, final Member member) {
        final var mutedRoleID = MMDBot.getConfig().getRole("muted");
        // TODO once JDA is updated, maybe timeout the user. It seems a better idea
        final var mutedRole = guild.getRoleById(mutedRoleID);
        if (mutedRole == null) {
            MMDBot.LOGGER.error(MMDMarkers.MUTING, "Unable to find muted role {}", mutedRoleID);
            return;
        }
        guild.addRoleToMember(member, mutedRole).queue();
    }

    private static MessageEmbed getLoggingEmbed(final Message message, final String extraDescription) {
        final var member = message.getMember();
        return new EmbedBuilder().setTitle("Scam link detected!")
            .setDescription(String.format("User %s sent a scam link in %s%s. Their message was deleted, and they were muted.", member.getUser().getAsTag(),
                message.getTextChannel().getAsMention(), extraDescription))
            .addField("Message Content", MarkdownUtil.codeblock(message.getContentRaw()), false)
            .setColor(Color.RED)
            .setTimestamp(Instant.now())
            .setFooter("User ID: " + member.getIdLong())
            .setThumbnail(member.getEffectiveAvatarUrl()).build();
    }

    private static void executeInLoggingChannel(Consumer<TextChannel> channel) {
        Utils.getChannelIfPresent(MMDBot.getConfig().getChannel("events.requests_deletion"), channel);
    }

    public static boolean containsScam(final Message message) {
        final String msgContent = message.getContentRaw().toLowerCase(Locale.ROOT);
        synchronized (SCAM_LINKS) {
            for (final var link : SCAM_LINKS) {
                if (msgContent.contains(link)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean setupScamLinks() {
        MMDBot.LOGGER.debug("Setting up scam links! Receiving data from {}.", SCAM_LINKS_DATA_URL);
        try (var is = new URL(SCAM_LINKS_DATA_URL).openStream()) {
            final String result = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            SCAM_LINKS.clear();
            SCAM_LINKS.addAll(StreamSupport.stream(GSON.fromJson(result, JsonArray.class).spliterator(), false)
                .map(JsonElement::getAsString).filter(s -> !s.contains("discordapp.co")).toList());
            return true;
        } catch (final IOException e) {
            MMDBot.LOGGER.error("Error while setting up scam links!", e);
        }
        return false;
    }
}
