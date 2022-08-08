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

import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookMessage;
import com.mcmoddev.mmdbot.core.util.Utils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.IWebhookContainer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.NewsChannel;
import net.dv8tion.jda.api.entities.Webhook;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class WebhookManagerImpl implements WebhookManager {
    private static final List<WebhookManagerImpl> MANAGERS = new CopyOnWriteArrayList<>();
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
    private static ScheduledExecutorService executor;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
            MANAGERS.forEach(WebhookManagerImpl::close), "WebhookClosing"));
    }

    private static ScheduledExecutorService getExecutor() {
        if (executor == null) {
            executor = Executors.newScheduledThreadPool(Math.max(MANAGERS.size() / 2, 1), r -> Utils.setThreadDaemon(new Thread(r, "Webhooks"), true));
        }
        return executor;
    }

    private final Predicate<String> predicate;
    private final String webhookName;
    private final AllowedMentions allowedMentions;
    private final Long2ObjectMap<JDAWebhookClient> webhooks = new Long2ObjectOpenHashMap<>();
    @Nullable
    private final Consumer<Webhook> creationListener;

    public WebhookManagerImpl(final Predicate<String> predicate, final String webhookName, final AllowedMentions allowedMentions, @javax.annotation.Nullable final Consumer<Webhook> creationListener) {
        this.predicate = predicate;
        this.webhookName = webhookName;
        this.allowedMentions = allowedMentions;
        this.creationListener = creationListener;
        MANAGERS.add(this);
    }

    @Override
    public JDAWebhookClient getWebhook(final IWebhookContainer channel) {
        return webhooks.computeIfAbsent(channel.getIdLong(), k ->
            WebhookClientBuilder.fromJDA(getOrCreateWebhook(channel))
                .setExecutorService(getExecutor())
                .setHttpClient(HTTP_CLIENT)
                .setAllowedMentions(allowedMentions)
                .buildJDA());
    }

    @Override
    public void sendAndCrosspost(final IWebhookContainer channel, final WebhookMessage message) {
        getWebhook(channel)
            .send(message)
            .thenAccept(msg -> {
                if (channel.getType() == ChannelType.NEWS) {
                    ((NewsChannel) channel).retrieveMessageById(msg.getId()).flatMap(Message::crosspost).queue();
                }
            });
    }

    private Webhook getOrCreateWebhook(IWebhookContainer channel) {
        final var alreadyExisted = unwrap(Objects.requireNonNull(channel).retrieveWebhooks()
            .submit(false))
            .stream()
            .filter(w -> predicate.test(w.getName()))
            .findAny();
        return alreadyExisted.orElseGet(() -> {
            final var webhook = unwrap(channel.createWebhook(webhookName).submit(false));
            if (creationListener != null) {
                creationListener.accept(webhook);
            }
            return webhook;
        });
    }

    private static <T> T unwrap(CompletableFuture<T> completableFuture) {
        try {
            return completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        webhooks.forEach((id, client) -> client.close());
    }
}
