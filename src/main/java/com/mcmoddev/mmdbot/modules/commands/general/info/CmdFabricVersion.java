/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2021 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.modules.commands.general.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.fabric.FabricVersionHelper;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.minecraft.MinecraftVersionHelper;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Instant;

/**
 * The type Cmd fabric version.
 *
 * @author williambl
 */
public final class CmdFabricVersion extends Command {

    /**
     * Instantiates a new Cmd fabric version.
     */
    public CmdFabricVersion() {
        super();
        name = "fabric";
        help = "Get the latest Fabric versions";
        category = new Category("Info");
        arguments = "<Minecraft Version>";
        aliases = new String[]{"fabricv"};
        guildOnly = true;
    }

    /**
     * Execute.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        var mcVersion = event.getArgs().trim();
        if (mcVersion.isEmpty()) {
            mcVersion = MinecraftVersionHelper.getLatest();
        }

        var yarnVersion = FabricVersionHelper.getLatestYarn(mcVersion);
        if (yarnVersion == null) {
            yarnVersion = "None";
        }
        final var embed = new EmbedBuilder();
        final var channel = event.getMessage();

        embed.setTitle("Fabric Versions for Minecraft " + mcVersion);
        embed.addField("Latest Yarn", yarnVersion, true);
        embed.addField("Latest API", FabricVersionHelper.getLatestApi(), true);
        embed.addField("Latest Loader", FabricVersionHelper.getLatestLoader(), true);
        embed.setColor(Color.WHITE);
        embed.setTimestamp(Instant.now());
        channel.replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
    }
}
