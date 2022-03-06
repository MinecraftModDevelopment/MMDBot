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
package com.mcmoddev.mmdbot.thelistener;

import com.mcmoddev.mmdbot.core.bot.Bot;
import com.mcmoddev.mmdbot.core.bot.BotRegistry;
import com.mcmoddev.mmdbot.core.bot.BotType;
import com.mcmoddev.mmdbot.core.bot.RegisterBotType;
import com.mcmoddev.mmdbot.core.event.WarningEvent;
import com.mcmoddev.mmdbot.dashboard.util.BotUserData;
import com.mcmoddev.mmdbot.thelistener.events.LeaveJoinEvents;
import com.mcmoddev.mmdbot.thelistener.events.MessageEvents;
import com.mcmoddev.mmdbot.thelistener.events.ModerationEvents;
import com.mcmoddev.mmdbot.thelistener.events.ReferencingListener;
import com.mcmoddev.mmdbot.thelistener.events.RoleEvents;
import com.mcmoddev.mmdbot.thelistener.util.EventListener;
import com.mcmoddev.mmdbot.thelistener.util.GuildConfig;
import com.mcmoddev.mmdbot.thelistener.util.LoggingType;
import com.mcmoddev.mmdbot.thelistener.util.ThreadedEventListener;
import com.mcmoddev.mmdbot.thelistener.util.Utils;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.possible.Possible;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.util.Color;
import io.github.cdimascio.dotenv.Dotenv;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.mcmoddev.mmdbot.thelistener.util.Utils.mentionAndID;

public final class TheListener implements Bot {

    public static final Logger LOGGER = LoggerFactory.getLogger("TheListener");

    @RegisterBotType(name = BotRegistry.THE_LISTENER_NAME)
    public static final BotType<TheListener> BOT_TYPE = new BotType<>() {
        @Override
        public TheListener createBot(final Path runPath) {
            return new TheListener(runPath);
        }

        @Override
        public Logger getLogger() {
            return LOGGER;
        }
    };

    static {
        WarningEvent.Add.addListener(event -> {
            final var doc = event.getDocument();
            Mono.zip(getClient().getUserById(Snowflake.of(doc.userId())).getData(),
                    getClient().getUserById(Snowflake.of(doc.moderatorId())).getData())
                .subscribe(t -> {
                    final var user = t.getT1();
                    final var warner = t.getT2();
                    final var embed = EmbedCreateSpec.builder()
                        .color(Color.RED)
                        .title("New Warning")
                        .description("%s warned %s".formatted(mentionAndID(doc.moderatorId()), mentionAndID(doc.userId())))
                        .thumbnail(user.avatar().map(Possible::of).orElse(Possible.absent()))
                        .addField("Reason:", doc.reason(), false)
                        .addField("Warning ID", doc.warnId(), false)
                        .timestamp(Instant.now())
                        .footer("Warner ID: " + doc.moderatorId(), warner.avatar().orElse(null));
                    Utils.executeInLoggingChannel(Snowflake.of(doc.guildId()), LoggingType.MODERATION_EVENTS,
                        c -> c.createMessage(embed.build().asRequest()).subscribe());
                });
        });
        WarningEvent.Clear.addListener(event -> {
            final var warnDoc = event.getDocument();
            Mono.zip(getClient().getUserById(Snowflake.of(warnDoc.userId())).getData(),
                    getClient().getUserById(Snowflake.of(event.getModeratorId())).getData())
                .subscribe(t -> {
                    final var user = t.getT1();
                    final var moderator = t.getT2();
                    final var embed = EmbedCreateSpec.builder()
                        .color(Color.GREEN)
                        .title("Warning Cleared")
                        .description("One of the warnings of " + mentionAndID(warnDoc.userId()) + " has been removed!")
                        .thumbnail(user.avatar().map(Possible::of).orElse(Possible.absent()))
                        .addField("Old warning reason:", warnDoc.reason(), false)
                        .addField("Old warner:", mentionAndID(warnDoc.userId()), false)
                        .timestamp(Instant.now())
                        .footer("Moderator ID: " + event.getModeratorId(), moderator.avatar().orElse(null));
                    Utils.executeInLoggingChannel(Snowflake.of(warnDoc.guildId()), LoggingType.MODERATION_EVENTS,
                        c -> c.createMessage(embed.build().asRequest()).subscribe());
                });
        });
        WarningEvent.ClearAllWarns.addListener(event -> {
            getClient().getUserById(Snowflake.of(event.getModeratorId())).getData()
                .subscribe(moderator -> {
                    final var embed = EmbedCreateSpec.builder()
                        .color(Color.GREEN)
                        .title("Warnings Cleared")
                        .description("All of the warnings of " + mentionAndID(event.getTargetId()) + " have been cleared!")
                        .timestamp(Instant.now())
                        .footer("Moderator ID: " + event.getModeratorId(), moderator.avatar().orElse(null));
                    Utils.executeInLoggingChannel(Snowflake.of(event.getGuildId()), LoggingType.MODERATION_EVENTS,
                        c -> c.createMessage(embed.build().asRequest()).subscribe());
                });
        });
    }

    private static TheListener instance;

    private DiscordClient client;
    private GatewayDiscordClient gateway;
    private final Path runPath;
    private final Long2ObjectMap<GuildConfig> guildConfigs = new Long2ObjectOpenHashMap<>();

    public TheListener(final Path runPath) {
        this.runPath = runPath;
    }

    public static final Executor GENERAL_EVENT_THREAD_POOL = Executors.newFixedThreadPool(3,
        r -> Utils.setThreadDaemon(new Thread(r, "GeneralD4JEvents"), true));

    @Override
    public void start() {
        instance = this;

        final var dotenv = Dotenv.configure()
            .directory(runPath.toString()).load();

        final var token = dotenv.get("BOT_TOKEN", "");

        client = DiscordClient.create(token);

        gateway = client.gateway()
            .setEnabledIntents(IntentSet.of(Intent.values()))
            .setEntityRetrievalStrategy(EntityRetrievalStrategy.REST).login().block();

        if (gateway == null) {
            throw new NullPointerException("gateway");
        }

        gateway.getEventDispatcher().on(ReadyEvent.class)
            .subscribe(event -> LOGGER.warn("I am ready to work! Logged in as {}", event.getSelf().getTag()));

        final var guildConfigsPath = getRunPath().resolve("configs/guilds");

        gateway.getEventDispatcher().on(GuildCreateEvent.class)
            .subscribe(event -> {
                final var id = event.getGuild().getId().asLong();
                if (!guildConfigs.containsKey(id)) {
                    guildConfigs.put(id, new GuildConfig(id, guildConfigsPath));
                }
            });

        Utils.subscribe(gateway, wrapListener(new MessageEvents()), wrapListener(new LeaveJoinEvents()),
            wrapListener(new ModerationEvents()), wrapListener(new RoleEvents()),
            wrapListener(new ReferencingListener()));

        new Thread(() -> {
            // D4J doesn't have non-daemon threads
            while (true) {
            }
        }).start();
    }

    @Override
    public void shutdown() {
        gateway.logout().subscribe();
    }

    public static EventListener wrapListener(EventListener listener) {
        return new ThreadedEventListener(listener, GENERAL_EVENT_THREAD_POOL);
    }

    @Override
    public BotType<?> getType() {
        return BOT_TYPE;
    }

    public Path getRunPath() {
        return runPath;
    }

    @NonNull
    public GuildConfig getConfigForGuild(Snowflake guild) {
        return guildConfigs.computeIfAbsent(guild.asLong(), k -> new GuildConfig(k, getRunPath().resolve("configs/guilds")));
    }

    public static TheListener getInstance() {
        return instance;
    }

    public static DiscordClient getClient() {
        return instance == null ? null : instance.client;
    }

    @Override
    public BotUserData getBotUserData() {
        final var selfUser = gateway.getSelf().block();
        if (selfUser != null) {
            return new BotUserData(selfUser.getUsername(), selfUser.getDiscriminator(), selfUser.getAvatarUrl());
        }
        return null;
    }
}
