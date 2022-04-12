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
package com.mcmoddev.updatinglauncher.discord.commands;

import com.google.gson.JsonObject;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.updatinglauncher.Config;
import com.mcmoddev.updatinglauncher.Constants;
import com.mcmoddev.updatinglauncher.JarUpdater;
import com.mcmoddev.updatinglauncher.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

public class ProfilingCommand extends ULCommand {
    public static final Path DIRECTORY_PATH = Main.UL_DIRECTORY.resolve("profiling");

    public ProfilingCommand(final Supplier<JarUpdater> jarUpdater, final Config.Discord config) {
        super(jarUpdater, config);
        name = "profiling";
        help = "Profiling related commands.";
        options = List.of(
            new OptionData(OptionType.STRING, "type", "The type of the profiler to run.")
                .addChoice("Process", "process")
        );
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        final var updater = jarUpdater.get();
        final var process = updater.getProcess();
        final var connector = process == null ? null : process.connector();
        if (connector == null) {
            event.deferReply(true).setContent("The process is not running or the agent wasn't attached!").queue();
            return;
        }
        event.deferReply()
            .flatMap(hook -> {
                try {
                    if (!Files.exists(DIRECTORY_PATH)) {
                        Files.createDirectories(DIRECTORY_PATH);
                    }
                    return switch (event.getOption("type", "", OptionMapping::getAsString)) {
                        case "process" -> {
                            final var result = connector.getProcessInfoProfiling();
                            final var file = DIRECTORY_PATH.resolve(Instant.now().getEpochSecond() + ".json");
                            try (final var writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE)) {
                                Constants.GSON.toJson(result, writer);
                                yield hook.editOriginal(file.toFile(), "profiling.json");
                            }
                        }
                        default -> hook.editOriginal("Invalid type provided!");
                    };
                } catch (Exception e) {
                    Main.LOG.error("Exception getting profiling results: ", e);
                    return hook.editOriginal("There was an exception getting profiling results: " + e.getLocalizedMessage());
                }
            })
            .queue();
    }
}
