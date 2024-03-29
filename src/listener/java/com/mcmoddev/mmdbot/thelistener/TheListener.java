/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2023 <MMD - MinecraftModDevelopment>
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
import com.mcmoddev.mmdbot.core.util.DotenvLoader;
import com.mcmoddev.mmdbot.core.util.Utils;
import com.mcmoddev.mmdbot.core.util.jda.caching.JdaMessageCache;
import com.mcmoddev.mmdbot.thelistener.events.LeaveJoinEvents;
import com.mcmoddev.mmdbot.thelistener.events.MessageEvents;
import com.mcmoddev.mmdbot.thelistener.events.ModerationEvents;
import com.mcmoddev.mmdbot.thelistener.events.RoleEvents;
import com.mcmoddev.mmdbot.thelistener.events.TrickEvents;
import com.mcmoddev.mmdbot.thelistener.util.GuildConfig;
import io.github.cdimascio.dotenv.Dotenv;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class TheListener implements Bot {

    public static final Logger LOGGER = LoggerFactory.getLogger("TheListener");

    @RegisterBotType(name = BotRegistry.THE_LISTENER_NAME, priority = -10)
    public static final BotType<TheListener> BOT_TYPE = new BotType<>() {
        @Override
        public TheListener createBot(final Path runPath) {
            try {
                return new TheListener(runPath, DotenvLoader.builder()
                    .filePath(runPath.toAbsolutePath().resolve(".env"))
                    .whenCreated(writer -> writer
                        .writeComment("The token of the bot: ")
                        .writeValue("BOT_TOKEN", "")
                    )
                    .load());
            } catch (IOException e) {
                LOGGER.error("Could not load the .env file due to an IOException: ", e);
            }
            return null;
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

    private static final Set<GatewayIntent> INTENTS = Set.of(
        GatewayIntent.DIRECT_MESSAGES,
        GatewayIntent.GUILD_MODERATION,
        GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
        GatewayIntent.GUILD_MESSAGES,
        GatewayIntent.GUILD_MEMBERS,
        GatewayIntent.MESSAGE_CONTENT
    );

    private static TheListener instance;

    private JDA jda;
    private final Path runPath;
    private final Dotenv dotenv;
    private final Long2ObjectMap<GuildConfig> guildConfigs = new Long2ObjectOpenHashMap<>();

    public TheListener(final Path runPath, final Dotenv dotenv) {
        this.runPath = runPath;
        this.dotenv = dotenv;
    }

    public static final ExecutorService GENERAL_EVENT_THREAD_POOL = Executors.newFixedThreadPool(2,
        r -> Utils.setThreadDaemon(new Thread(r, "TheListenerEvents"), true));
    public static final com.mcmoddev.mmdbot.core.util.event.ThreadedEventListener GENERAL_EVENT_LISTENER = new com.mcmoddev.mmdbot.core.util.event.ThreadedEventListener(GENERAL_EVENT_THREAD_POOL);

    @Override
    public void start() {
        instance = this;

        GENERAL_EVENT_LISTENER.addListeners(
            MessageEvents.INSTANCE,
            ModerationEvents.INSTANCE,
            new LeaveJoinEvents(),
            new RoleEvents()
        );

        jda = JDABuilder.create(
                getToken(),
                INTENTS
            )
            .addEventListeners(JdaMessageCache.builder()
                    .onDelete(MessageEvents.INSTANCE::onMessageDelete)
                    .onEdit(MessageEvents.INSTANCE::onMessageUpdate)
                    .build(),
                GENERAL_EVENT_LISTENER
            )
            .disableCache(CacheFlag.CLIENT_STATUS)
            .disableCache(CacheFlag.ONLINE_STATUS)
            .disableCache(CacheFlag.VOICE_STATE)
            .disableCache(CacheFlag.ACTIVITY)
            .disableCache(CacheFlag.SCHEDULED_EVENTS)
            .build();
    }

    @Override
    public boolean blocksStartupThread() {
        return false;
    }

    @Override
    public void shutdown() {
        jda.shutdownNow();
    }

    @Override
    public BotType<?> getType() {
        return BOT_TYPE;
    }

    @Override
    public String getToken() {
        return dotenv.get("BOT_TOKEN", "");
    }

    public Path getRunPath() {
        return runPath;
    }

    public JDA getJDA() {
        return jda;
    }

    @NotNull
    public GuildConfig getConfigForGuild(long guild) {
        return guildConfigs.computeIfAbsent(guild, k -> new GuildConfig(k, getRunPath().resolve("configs").resolve("guilds")));
    }

    public static TheListener getInstance() {
        return instance;
    }
}
