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
package com.mcmoddev.mmdbot.core.util.webhook;

import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookMessage;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Manages multiple webhooks across different channels.
 *
 * @author matyrobbrt
 */
public interface WebhookManager {

    static WebhookManager of(String name) {
        return of(e -> e.trim().equals(name), name, AllowedMentions.none());
    }

    static WebhookManager of(Predicate<String> matcher, String webhookName, AllowedMentions allowedMentions, @Nullable Consumer<Webhook> creationListener) {
        return new WebhookManagerImpl(matcher, webhookName, allowedMentions, creationListener);
    }

    static WebhookManager of(Predicate<String> matcher, String webhookName, AllowedMentions allowedMentions) {
        return of(matcher, webhookName, allowedMentions, null);
    }

    /**
     * Gets or creates the webhook client for a given {@code channel}.
     *
     * @param channel the channel to get or create the webhook in
     * @return the webhook
     */
    JDAWebhookClient getWebhook(IWebhookContainer channel);

    /**
     * Sends and crossposts / publishes a message in a channel using a webhook.
     *
     * @param channel the channel to send the message in
     * @param message the message to send
     */
    void sendAndCrosspost(IWebhookContainer channel, WebhookMessage message);
}
