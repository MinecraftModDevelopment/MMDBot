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
package com.mcmoddev.mmdbot.modules.commands.general.info;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.minecraft.MinecraftVersionHelper;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Instant;

/**
 * Returns the latest Minecraft release and snapshot version.
 *
 * @author Poke
 * @author Curle
 */
public final class CmdMinecraftVersion extends SlashCommand {

    /**
     * Instantiates a new Cmd minecraft version.
     */
    public CmdMinecraftVersion() {
        super();
        name = "minecraft";
        help = "Get the latest Minecraft versions";
        category = new Category("Info");
        aliases = new String[]{"minecraftv", "mcv"};
        guildOnly = false;
    }

    /**
     * Execute.
     *
     * @param event The {@link SlashCommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        final var embed = new EmbedBuilder();
        embed.setTitle("Minecraft Versions");
        embed.addField("Latest", MinecraftVersionHelper.getLatest(), true);
        embed.addField("Latest Stable", MinecraftVersionHelper.getLatestStable(), true);
        embed.setColor(Color.GREEN);
        embed.setTimestamp(Instant.now());
        event.replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
    }
}
