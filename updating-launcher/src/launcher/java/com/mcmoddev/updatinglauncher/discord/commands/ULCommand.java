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

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.updatinglauncher.Config;
import com.mcmoddev.updatinglauncher.JarUpdater;

import java.util.function.Supplier;

public abstract class ULCommand extends SlashCommand {

    protected final Supplier<JarUpdater> jarUpdater;

    protected ULCommand(final Supplier<JarUpdater> jarUpdater, final Config.Discord config) {
        this.jarUpdater = jarUpdater;
        this.enabledRoles = config.roles.toArray(String[]::new);
        guildOnly = true;
        defaultEnabled = false;
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
    }
}
