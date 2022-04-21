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
package com.mcmoddev.mmdbot.thelistener;

import com.mcmoddev.mmdbot.core.bot.Bot;
import com.mcmoddev.mmdbot.core.bot.BotRegistry;
import com.mcmoddev.mmdbot.core.bot.BotType;
import com.mcmoddev.mmdbot.core.bot.RegisterBotType;
import com.mcmoddev.mmdbot.core.event.Events;
import com.mcmoddev.mmdbot.core.util.jda.caching.JdaMessageCache;
import com.mcmoddev.mmdbot.thelistener.events.LeaveJoinEvents;
import com.mcmoddev.mmdbot.thelistener.events.MessageEvents;
import com.mcmoddev.mmdbot.thelistener.events.ModerationEvents;
import com.mcmoddev.mmdbot.thelistener.events.RoleEvents;
import com.mcmoddev.mmdbot.thelistener.events.TrickEvents;
import com.mcmoddev.mmdbot.thelistener.util.EventListener;
import com.mcmoddev.mmdbot.thelistener.util.GuildConfig;
import com.mcmoddev.mmdbot.thelistener.util.ThreadedEventListener;
import com.mcmoddev.mmdbot.thelistener.util.Utils;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import io.github.cdimascio.dotenv.Dotenv;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO move to JDA
public final class TheListener implements Bot {

    public static final Logger LOGGER = LoggerFactory.getLogger("TheListener");

    @RegisterBotType(name = BotRegistry.THE_LISTENER_NAME, priority = -10)
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
        Events.MODERATION_BUS.register(ModerationEvents.INSTANCE);
        Events.MODERATION_BUS.register(MessageEvents.INSTANCE);

        Events.CUSTOM_AUDIT_LOG_BUS.register(TrickEvents.class);
    }

    private static TheListener instance;

    private JDA jda;
    private GatewayDiscordClient client;
    private final Path runPath;
    private final Long2ObjectMap<GuildConfig> guildConfigs = new Long2ObjectOpenHashMap<>();

    public TheListener(final Path runPath) {
        this.runPath = runPath;
    }

    public static final ExecutorService GENERAL_EVENT_THREAD_POOL = Executors.newFixedThreadPool(2,
        r -> Utils.setThreadDaemon(new Thread(r, "TheListenerEvents"), true));
    public static final com.mcmoddev.mmdbot.core.util.event.ThreadedEventListener GENERAL_EVENT_LISTENER = new com.mcmoddev.mmdbot.core.util.event.ThreadedEventListener(GENERAL_EVENT_THREAD_POOL);

    @Override
    public void start() throws LoginException {
        instance = this;

        final var dotenv = Dotenv.configure()
            .directory(runPath.toString()).load();

        final var token = dotenv.get("BOT_TOKEN", "");

        jda = JDABuilder.create(
            token,
            List.of(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_BANS, GatewayIntent.GUILD_MESSAGES)
        )
            .addEventListeners(JdaMessageCache.builder()
                .onDelete(MessageEvents.INSTANCE::onMessageDelete)
                .onEdit(MessageEvents.INSTANCE::onMessageUpdate)
                .build(),
                GENERAL_EVENT_LISTENER
            )
            .build();

        client = DiscordClient.create(token).gateway()
            .setEnabledIntents(IntentSet.of(Intent.values()))
            .setEntityRetrievalStrategy(EntityRetrievalStrategy.REST).login().block();

        if (client == null) {
            throw new NullPointerException("client");
        }

        client.getEventDispatcher().on(ReadyEvent.class)
            .subscribe(event -> LOGGER.warn("I am ready to work! Logged in as {}", event.getSelf().getTag()));

        final var guildConfigsPath = getRunPath().resolve("configs/guilds");

        client.getEventDispatcher().on(GuildCreateEvent.class)
            .subscribe(event -> {
                final var id = event.getGuild().getId().asLong();
                if (!guildConfigs.containsKey(id)) {
                    guildConfigs.put(id, new GuildConfig(id, guildConfigsPath));
                }
            });

        Utils.subscribe(client,
            wrapListener(ModerationEvents.INSTANCE), wrapListener(new RoleEvents()));

        addListener(MessageEvents.INSTANCE);
        addListener(new LeaveJoinEvents());
    }

    @Override
    public boolean blocksStartupThread() {
        return true;
    }

    @Override
    public void shutdown() {
        client.logout().subscribe();
    }

    public static EventListener wrapListener(EventListener listener) {
        return new ThreadedEventListener(listener, GENERAL_EVENT_THREAD_POOL);
    }

    public static void addListener(net.dv8tion.jda.api.hooks.EventListener listener) {
        GENERAL_EVENT_LISTENER.addListener(listener);
    }

    @Override
    public BotType<?> getType() {
        return BOT_TYPE;
    }

    public Path getRunPath() {
        return runPath;
    }

    public JDA getJDA() {
        return jda;
    }

    @NonNull
    public GuildConfig getConfigForGuild(long guild) {
        return guildConfigs.computeIfAbsent(guild, k -> new GuildConfig(k, getRunPath().resolve("configs/guilds")));
    }

    public static TheListener getInstance() {
        return instance;
    }

    public static GatewayDiscordClient getClient() {
        return instance == null ? null : instance.client;
    }
}
