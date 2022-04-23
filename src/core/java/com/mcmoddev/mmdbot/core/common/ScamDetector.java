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
package com.mcmoddev.mmdbot.core.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;
import com.mcmoddev.mmdbot.core.event.Events;
import com.mcmoddev.mmdbot.core.event.moderation.ScamLinkEvent;
import com.mcmoddev.mmdbot.core.util.TaskScheduler;
import lombok.extern.slf4j.Slf4j;

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
 * TODO use a better way
 *
 * @author matyrobbrt
 */
@Slf4j
public class ScamDetector {

    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final String SCAM_LINKS_DATA_URL = "https://phish.sinking.yachts/v2/all";

    public static final Set<String> SCAM_LINKS = Collections.synchronizedSet(new HashSet<>());

    static {
        new Thread(ScamDetector::setupScamLinks, "ScamLinkCollector").start();
    }

    public static void postScamEvent(final long guildId, final long targetId, final long channelId,
                                      final String messageContent, final String targetAvatar, final boolean editedMessage) {
        Events.MODERATION_BUS.post(new ScamLinkEvent(guildId, targetId, channelId, messageContent, targetAvatar, editedMessage));
    }

    public static boolean containsScam(final String text) {
        final var parser = new UrlDetector(text, UrlDetectorOptions.ALLOW_SINGLE_LEVEL_DOMAIN);
        final var urls = parser.detect();
        return urls.stream().anyMatch(u -> ScamDetector.isScam(u.getHost().toLowerCase(Locale.ROOT)));
    }

    public static boolean isScam(final String domain) {
        synchronized (SCAM_LINKS) {
            for (final var link : SCAM_LINKS) {
                if (domain.equals(link)) {
                    return true;
                }
            }
        }
        return false;
    }

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
            SCAM_LINKS.addAll(StreamSupport.stream(GSON.fromJson(result, JsonArray.class)
                    .spliterator(), false)
                .map(JsonElement::getAsString)
                .toList());
            return true;
        } catch (final IOException e) {
            log.error("Error while setting up scam links!", e);
        }
        return false;
    }
}
