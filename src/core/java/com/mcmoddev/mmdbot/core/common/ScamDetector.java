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
package com.mcmoddev.mmdbot.core.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mcmoddev.mmdbot.core.event.Events;
import com.mcmoddev.mmdbot.core.event.moderation.ScamLinkEvent;
import com.mcmoddev.mmdbot.core.util.TaskScheduler;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

/**
 * Scam detection system
 *
 * @author matyrobbrt
 */
@Slf4j
public class ScamDetector extends ListenerAdapter {

    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final String SCAM_LINKS_DATA_URL = "https://phish.sinking.yachts/v2/all";

    public static final Set<String> SCAM_LINKS = Collections.synchronizedSet(new HashSet<>());

    static {
        new Thread(ScamDetector::setupScamLinks, "ScamLinkCollector").start();
    }

    @Override
    public void onMessageReceived(@NotNull final MessageReceivedEvent event) {
        if (event.isFromGuild()) {
            takeActionIfScam(event.getMessage(), false);
        }
    }

    @Override
    public void onMessageUpdate(@NotNull final MessageUpdateEvent event) {
        if (event.isFromGuild()) {
            takeActionIfScam(event.getMessage(), true);
        }
    }

    public static void takeActionIfScam(@Nonnull final Message msg, final boolean editedMessage) {
        final var member = msg.getMember();
        if (member == null || msg.getAuthor().isBot() || msg.getAuthor().isSystem() ||
            member.hasPermission(Permission.MANAGE_CHANNEL)) {
            // return;
        }
        if (containsScam(msg.getContentRaw().toLowerCase(Locale.ROOT))) {
            final var guild = msg.getGuild();
            msg.delete().reason("Scam link").queue($ -> {
                postScamEvent(msg.getGuild().getIdLong(), msg.getAuthor().getIdLong(), msg.getChannel().getIdLong(),
                    msg.getContentRaw(), msg.getAuthor().getEffectiveAvatarUrl(), editedMessage);
                mute(guild, member);
            });
        }
    }

    private static void mute(final Guild guild, final Member member) {
        guild.timeoutFor(member, 14, TimeUnit.DAYS).reason("Sent a scam link").queue(); // 2 weeks timeout
    }

    private static void postScamEvent(final long guildId, final long targetId, final long channelId,
                                      final String messageContent, final String targetAvatar, final boolean editedMessage) {
        Events.MODERATION_BUS.post(new ScamLinkEvent(guildId, targetId, channelId, messageContent, targetAvatar, editedMessage));
    }

    public static boolean containsScam(final String text) {
        synchronized (SCAM_LINKS) {
            for (final var link : SCAM_LINKS) {
                if (text.contains(link)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final List<String> IGNORED = List.of("discordapp.co", "witch.tv", "steamcommunity.co");

    public static void onCollectTasks(final TaskScheduler.CollectTasksEvent event) {
        event.addTask(() -> {
            if (setupScamLinks()) {
                log.info("Successfully refreshed scam links");
            } else {
                log.warn("Scam links could not be automatically refreshed");
            }
        }, 0, 14, TimeUnit.DAYS);
    }

    public static boolean setupScamLinks() {
        log.info("Setting up scam links! Receiving data from {}.", SCAM_LINKS_DATA_URL);
        try (var is = new URL(SCAM_LINKS_DATA_URL).openStream()) {
            final String result = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            SCAM_LINKS.clear();
            SCAM_LINKS.addAll(StreamSupport.stream(GSON.fromJson(result, JsonArray.class).spliterator(), false)
                .map(JsonElement::getAsString).filter(s -> IGNORED.stream().noneMatch(s::contains)).toList());
            return true;
        } catch (final IOException e) {
            log.error("Error while setting up scam links!", e);
        }
        return false;
    }
}
