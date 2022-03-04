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
package com.mcmoddev.mmdbot.commander;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.mcmoddev.mmdbot.commander.curseforge.CFProjects;
import com.mcmoddev.mmdbot.commander.curseforge.CurseForgeManager;
import com.mcmoddev.mmdbot.commander.curseforge.CurseForgeWebhooksCommand;
import com.mcmoddev.mmdbot.core.bot.Bot;
import com.mcmoddev.mmdbot.core.bot.BotRegistry;
import com.mcmoddev.mmdbot.core.bot.BotType;
import com.mcmoddev.mmdbot.core.bot.RegisterBotType;
import com.mcmoddev.mmdbot.core.util.DotenvLoader;
import com.mcmoddev.mmdbot.core.util.Utils;
import com.mcmoddev.mmdbot.dashboard.util.BotUserData;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.matyrobbrt.curseforgeapi.CurseForgeAPI;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.AllowedMentions;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class TheCommander implements Bot {

    public static final Logger LOGGER = LoggerFactory.getLogger("TheCommander");
    public static final ScheduledExecutorService CURSE_FORGE_UPDATE_SCHEDULER = Executors.newScheduledThreadPool(1,
        r -> Utils.setThreadDaemon(new Thread(r, "CurseForgeUpdateChecker"), true));

    @RegisterBotType(name = BotRegistry.THE_COMMANDER_NAME)
    public static final BotType<TheCommander> BOT_TYPE = new BotType<>() {
        @Override
        public TheCommander createBot(final Path runPath) {
            try {
                return new TheCommander(runPath, DotenvLoader.builder()
                    .filePath(runPath.resolve(".env"))
                    .whenCreated(writer -> writer
                        .writeComment("The token of the bot: ")
                        .writeValue("BOT_TOKEN", "")
                        .writeComment("The API key to use for CurseForge requests: ")
                        .writeValue("CF_API_KEY", ""))
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

    private static final Set<GatewayIntent> INTENTS = Set.of(
        GatewayIntent.DIRECT_MESSAGES,
        GatewayIntent.GUILD_BANS,
        GatewayIntent.GUILD_EMOJIS,
        GatewayIntent.GUILD_MESSAGE_REACTIONS,
        GatewayIntent.GUILD_MESSAGES,
        GatewayIntent.GUILD_MEMBERS);

    static {
        AllowedMentions.setDefaultMentionRepliedUser(false);
    }

    /**
     * The instance.
     */
    private static TheCommander instance;

    public static TheCommander getInstance() {
        return instance;
    }

    public static JDA getJDA() {
        return getInstance().getJda();
    }

    private JDA jda;
    @Nullable
    private CurseForgeManager curseForgeManager;
    private final Dotenv dotenv;
    private final Path runPath;

    public TheCommander(final Path runPath, final Dotenv dotenv) {
        this.dotenv = dotenv;
        this.runPath = runPath;
    }

    @Override
    public void start() {
        instance = this;

        try {
            jda = JDABuilder
                .create(dotenv.get("BOT_TOKEN"), INTENTS)
                .disableCache(CacheFlag.VOICE_STATE)
                .disableCache(CacheFlag.ACTIVITY)
                .disableCache(CacheFlag.CLIENT_STATUS)
                .disableCache(CacheFlag.ONLINE_STATUS)
                .build().awaitReady();

        } catch (final LoginException exception) {
            LOGGER.error("Error logging in the bot! Please give the bot a valid token in the config file.", exception);
            System.exit(1);
        } catch (InterruptedException e) {
            LOGGER.error("Error awaiting caching.", e);
            System.exit(1);
        }

        // TODO config
        final var commandClient = new CommandClientBuilder()
            .setOwnerId(561254664750891009L)
            .forceGuildOnly(853270691176906802L)
            .addSlashCommands(new CurseForgeWebhooksCommand())
            .build();

        jda.addEventListener(commandClient); // TODO convert to a threaded event listener

        final var cfKey = dotenv.get("CF_API_KEY", "");
        if (!cfKey.isBlank()) {
            final var api = new CurseForgeAPI(cfKey);
            final var cfProjects = new CFProjects(runPath.resolve("cf_projects.json"));
            this.curseForgeManager = new CurseForgeManager(api, cfProjects);

            CURSE_FORGE_UPDATE_SCHEDULER.scheduleAtFixedRate(cfProjects, 0, 10, TimeUnit.MINUTES);
        } else {
            LOGGER.warn("Could not find a valid CurseForge API Key! Some features might not work as expected.");
        }
    }

    @Override
    public void shutdown() {
        jda.shutdown();
    }

    @Override
    public BotType<?> getType() {
        return BOT_TYPE;
    }

    public Dotenv getDotenv() {
        return dotenv;
    }

    public Path getRunPath() {
        return runPath;
    }

    public JDA getJda() {
        return jda;
    }

    public Optional<CurseForgeManager> getCurseForgeManager() {
        return Optional.ofNullable(curseForgeManager);
    }

    @Override
    public BotUserData getBotUserData() {
        final var selfUser = jda.getSelfUser();
        return new BotUserData(selfUser.getName(), selfUser.getDiscriminator(),
            selfUser.getAvatarUrl() == null ? selfUser.getDefaultAvatarUrl() : selfUser.getAvatarUrl());
    }
}
