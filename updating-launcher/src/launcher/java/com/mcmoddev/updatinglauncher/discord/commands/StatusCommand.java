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
package com.mcmoddev.updatinglauncher.discord.commands;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.updatinglauncher.Config;
import com.mcmoddev.updatinglauncher.JarUpdater;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.Color;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.jar.JarFile;

public class StatusCommand extends ULCommand {
    private final Supplier<JarUpdater> jarUpdater;
    public StatusCommand(final Supplier<JarUpdater> jarUpdater, final Config.Discord config) {
        super(config);
        this.jarUpdater = jarUpdater;
        name = "status";
        help = "Gets information about the process status.";
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        final var updater = jarUpdater.get();
        final var process = updater.getProcess();
        final var version = updater.getJarVersion();
        if (process == null) {
            event.deferReply().addEmbeds(
                new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Process is not running")
                    .addField("Jar Version", version.orElse("Unknown"), true)
                    .setTimestamp(Instant.now())
                    .build()
            ).queue();
        } else {
            event.deferReply().addEmbeds(
                new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("Process is running.")
                    .addField("Jar Version", version.orElse("Unknown"), true)
                    .addField("Running Since", process.process().info().startInstant().map(TimeFormat.RELATIVE::format).orElse("Unknown startup time"), true)
                    .setTimestamp(Instant.now())
                    .build()
            ).queue();
        }

        try {
            assert process != null;
            System.out.println(Arrays.toString(Objects.requireNonNull(process.connector()).getThreads()));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
