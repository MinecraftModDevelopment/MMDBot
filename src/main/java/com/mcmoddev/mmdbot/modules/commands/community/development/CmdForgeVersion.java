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
package com.mcmoddev.mmdbot.modules.commands.community.development;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.utilities.CommandUtilities;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.forge.ForgeVersionHelper;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.forge.MinecraftForgeVersion;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Retrieve the current latest and recommended builds for Forge.
 * Takes an optional version parameter.
 * <p>
 * Takes the forms:
 * /forge
 * /forge 1.16.5
 * /forge 1.12.2
 * /forge [version]
 *
 * @author Poke
 * @author Curle
 */
public final class CmdForgeVersion extends SlashCommand {

    /**
     * Instantiates a new Cmd forge version.
     */
    public CmdForgeVersion() {
        super();
        name = "forge";
        help = "Get forge versions for latest Minecraft version";
        category = new Category("Info");
        arguments = "[Minecraft Version]";
        aliases = new String[]{"forgev"};
        guildOnly = false;


        OptionData data = new OptionData(OptionType.STRING, "version", "The version of Minecraft to check for.").setRequired(false);
        List<OptionData> dataList = new ArrayList<>();
        dataList.add(data);
        this.options = dataList;
    }

    /**
     * Execute.
     *
     * @param event The {@link SlashCommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        if (!CommandUtilities.checkCommand(this, event)) {
            return;
        }

        MinecraftForgeVersion latest;
        OptionMapping version = event.getOption("version");
        try {
            if (version != null)
                latest = new MinecraftForgeVersion(version.getAsString(), ForgeVersionHelper.getForgeVersionsForMcVersion(version.getAsString()));
            else
                latest = ForgeVersionHelper.getLatestMcVersionForgeVersions();
        } catch (Exception ex) {
            event.reply("Unable to get forge versions.").setEphemeral(true).queue();
            ex.printStackTrace();
            return;
        }

        try {
            final var latestForgeVersion = latest.getForgeVersion();
            final var latestForge = latestForgeVersion.getLatest();
            var recommendedForge = latestForgeVersion.getRecommended();
            if (recommendedForge == null) {
                recommendedForge = "none";
            }
            final var changelogLink = Utils.makeHyperlink("Changelog", String.format(
                "https://files.minecraftforge.net/maven/net/minecraftforge/forge/%1$s-%2$s/forge-%1$s-%2$s-changelog.txt",
                latest.getMcVersion(), latest.getForgeVersion().getLatest()));
            final var embed = new EmbedBuilder();

            embed.setTitle(String.format("Forge Versions for MC %s", latest.getMcVersion()));
            embed.addField("Latest", latestForge, true);
            embed.addField("Recommended", recommendedForge, true);
            embed.setDescription(changelogLink);
            embed.setColor(Color.ORANGE);
            embed.setTimestamp(Instant.now());
            event.replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
        } catch (NullPointerException e) {
            event.reply("The given Minecraft version " + version.getAsString() + " is invalid.").setEphemeral(true).queue();
        }
    }
}
