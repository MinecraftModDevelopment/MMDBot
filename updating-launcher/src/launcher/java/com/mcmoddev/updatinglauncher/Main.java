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
package com.mcmoddev.updatinglauncher;

import com.mcmoddev.updatinglauncher.discord.DiscordIntegration;
import com.mcmoddev.updatinglauncher.github.UpdateChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurateException;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Main {
    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("UpdatingLauncher");

    public static final Logger LOG = LoggerFactory.getLogger("UpdatingLauncher");
    public static final Path CONFIG_PATH = Path.of("updating_launcher.conf");
    public static final Path AGENT_PATH = Path.of("agent.jar");
    public static final ScheduledThreadPoolExecutor SERVICE;

    static {
        final var service = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, r -> new Thread(THREAD_GROUP, r, "UpdatingLauncher"));
        service.setKeepAliveTime(1, TimeUnit.HOURS);
        SERVICE = service;
    }

    private static final ExecutorService HTTP_CLIENT_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        final var thread = new Thread(THREAD_GROUP, r, "UpdatingLauncherHttpClient");
        thread.setDaemon(true);
        return thread;
    });

    private static DiscordIntegration discordIntegration;

    public static void main(String[] args) {
        try {
            copyAgent();
        } catch (IOException e) {
            LOG.error("Exception copying agent JAR: ", e);
            throw new RuntimeException(e);
        }

        final var cfgExists = Files.exists(CONFIG_PATH);
        Config config;
        try {
            config = Config.load(CONFIG_PATH);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
        if (!cfgExists) {
            throw new RuntimeException("A new configuration file was created! Please configure it.");
        }

        final var updateChecker = new UpdateChecker(config.gitHub.owner, config.gitHub.repo, HttpClient.newBuilder()
            .executor(HTTP_CLIENT_EXECUTOR)
            .build());
        final var updater = new JarUpdater(Paths.get(config.jarPath), updateChecker, Pattern.compile(config.checkingInfo.filePattern), config.jvmArgs);
        if (config.checkingInfo.rate > -1) {
            SERVICE.scheduleAtFixedRate(updater, 0, config.checkingInfo.rate, TimeUnit.MINUTES);
            LOG.warn("Scheduled updater. Will run every {} minutes.", config.checkingInfo.rate);
        } else {
            updater.tryFirstStart();
            SERVICE.allowCoreThreadTimeOut(true);
        }

        if (config.discord.enabled) {
            discordIntegration = new DiscordIntegration(Paths.get(""), config.discord, updater);
            LOG.warn("Discord integration is active!");
        }
    }

    public static DiscordIntegration getDiscordIntegration() {
        return discordIntegration;
    }

    public static void copyAgent() throws IOException {
        var agent = Main.class.getResourceAsStream("/agent.jar");
        if (agent == null) {
            // If it isn't a .jar, try finding a .zip
            agent = Main.class.getResourceAsStream("/agent.zip");
        }
        Files.copy(Objects.requireNonNull(agent), AGENT_PATH, StandardCopyOption.REPLACE_EXISTING);
    }
}
