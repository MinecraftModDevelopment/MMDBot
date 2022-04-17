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
package com.mcmoddev.updatinglauncher.discord;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.mcmoddev.updatinglauncher.Config;
import com.mcmoddev.updatinglauncher.JarUpdater;
import com.mcmoddev.updatinglauncher.Main;
import com.mcmoddev.updatinglauncher.discord.commands.ProfilingCommand;
import com.mcmoddev.updatinglauncher.discord.commands.file.FileCommand;
import com.mcmoddev.updatinglauncher.discord.commands.ShutdownCommand;
import com.mcmoddev.updatinglauncher.discord.commands.StartCommand;
import com.mcmoddev.updatinglauncher.discord.commands.StatusCommand;
import com.mcmoddev.updatinglauncher.discord.commands.UpdateCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.nio.file.Path;
import java.util.concurrent.Executors;

public final class DiscordIntegration {
    private final Logger logger = LoggerFactory.getLogger("ULDiscordIntegration");
    private final Config.Discord config;
    private final JarUpdater updater;
    private final JDA jda;

    public DiscordIntegration(final Path basePath, final Config.Discord config, final JarUpdater updater) {
        this.config = config;
        this.updater = updater;

        final var statusCmd = new StatusCommand(() -> updater, config);
        final var commandClient = new CommandClientBuilder()
            .setOwnerId("0000000000")
            .setActivity(null)
            .forceGuildOnly(config.guildId)
            .setPrefixes(config.prefixes.toArray(String[]::new))
            .addSlashCommands(
                new UpdateCommand(() -> updater, config),
                new ShutdownCommand(() -> updater, config),
                new StartCommand(() -> updater, config),
                statusCmd,
                new FileCommand(basePath, config),
                new ProfilingCommand(() -> updater, config)
            )
            .build();

        try {
            jda = JDABuilder.createLight(config.botToken)
                .setStatus(OnlineStatus.IDLE)
                .addEventListeners(commandClient, statusCmd)
                .setRateLimitPool(Executors.newScheduledThreadPool(1, r -> {
                    final var t = new Thread(Main.THREAD_GROUP, r, "ULDiscordRateLimiter");
                    t.setDaemon(true);
                    return t;
                }), true)
                .setGatewayPool(Main.SERVICE)
                .build()
                .setRequiredScopes("applications.commands", "bot");

            Runtime.getRuntime().addShutdownHook(new Thread(jda::shutdownNow));
        } catch (LoginException e) {
            throw new RuntimeException("Please provide a valid bot token!");
        }

    }

    public Logger getLogger() {
        return logger;
    }

    public Config.Discord getConfig() {
        return config;
    }

    public JarUpdater getUpdater() {
        return updater;
    }

    public JDA getJda() {
        return jda;
    }

    public void setActivity(boolean processRunning) {
        getJda().getPresence()
            .setActivity(Activity.of(Activity.ActivityType.WATCHING, processRunning ? "a process \uD83D\uDC40" : "nothing \uD83D\uDE22"));
    }
}
