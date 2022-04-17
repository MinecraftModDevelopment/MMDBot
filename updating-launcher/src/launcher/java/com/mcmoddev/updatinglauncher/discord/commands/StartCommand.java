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

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.updatinglauncher.Config;
import com.mcmoddev.updatinglauncher.DefaultJarUpdater;
import com.mcmoddev.updatinglauncher.api.JarUpdater;

import java.nio.file.Files;
import java.util.function.Supplier;

public class StartCommand extends ULCommand {
    public StartCommand(final Supplier<JarUpdater> jarUpdater, final Config.Discord config) {
        super(jarUpdater, config);
        name = "start";
        help = "Starts the process.";
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        final var updater = jarUpdater.get();
        final var process = updater.getProcess();
        if (process != null) {
            event.deferReply().setContent("A process is running already! Use `/shutdown` to stop it.").queue();
            return;
        }
        if (!Files.exists(updater.getJarPath())) {
            event.deferReply().setContent("Cannot start the process due its jar file missing. Use `/update` to update to a version.").queue();
            return;
        }
        event.deferReply()
            .setContent("Starting the process!")
            .flatMap(hook -> {
                DefaultJarUpdater.LOGGER.warn("Starting process at the request of {} via Discord.", event.getUser().getAsTag());
                updater.startProcess();
                return hook.editOriginal("Successfully started the process.");
            })
            .queue();
    }
}
