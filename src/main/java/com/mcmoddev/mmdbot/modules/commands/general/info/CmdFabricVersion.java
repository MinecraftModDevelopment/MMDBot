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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.fabric.FabricVersionHelper;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.minecraft.MinecraftVersionHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Get the latest Fabric, API and Yarn versions.
 * Optionally takes a version parameter.
 * <p>
 * Takes the form:
 * /fabric
 * /fabric 1.16.5
 * /fabric 1.12.2
 * /fabric [version]
 *
 * @author williambl
 * @author Curle
 */
public final class CmdFabricVersion extends SlashCommand {

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

        OptionData data = new OptionData(OptionType.STRING, "version", "The version of Minecraft to check for.").setRequired(false);
        List<OptionData> dataList = new ArrayList<>();
        dataList.add(data);
        this.options = dataList;
    }

    /**
     * Execute.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        String minecraft = MinecraftVersionHelper.getLatest();
        OptionMapping version = event.getOption("version");
        if (version != null)
            minecraft = version.getAsString();

        var yarnVersion = FabricVersionHelper.getLatestYarn(minecraft);
        if (yarnVersion == null) {
            yarnVersion = "None";
        }
        final var embed = new EmbedBuilder();

        embed.setTitle("Fabric Versions for Minecraft " + minecraft);
        embed.addField("Latest Yarn", yarnVersion, true);
        embed.addField("Latest API", FabricVersionHelper.getLatestApi(), true);
        embed.addField("Latest Loader", FabricVersionHelper.getLatestLoader(), true);
        embed.setColor(Color.WHITE);
        embed.setTimestamp(Instant.now());
        event.replyEmbeds(embed.build()).mentionRepliedUser(false).setEphemeral(true).queue();
    }
}
