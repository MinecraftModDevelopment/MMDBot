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
package com.mcmoddev.mmdbot.commander.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.commander.updatenotifiers.fabric.FabricVersionHelper;
import com.mcmoddev.mmdbot.commander.updatenotifiers.forge.ForgeVersionHelper;
import com.mcmoddev.mmdbot.commander.updatenotifiers.forge.MinecraftForgeVersion;
import com.mcmoddev.mmdbot.commander.updatenotifiers.minecraft.MinecraftVersionHelper;
import com.mcmoddev.mmdbot.commander.updatenotifiers.quilt.QuiltVersionHelper;
import com.mcmoddev.mmdbot.core.util.builder.SlashCommandBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.Color;
import java.time.Instant;

public final class VersionCommand {

    @RegisterSlashCommand
    public static final SlashCommand COMMAND = SlashCommandBuilder.builder()
        .name("version")
        .help("Version-related commands.")
        .children(
            SlashCommandBuilder.builder()
                .name("forge")
                .help("Get forge versions for latest Minecraft version.")
                .options(new OptionData(OptionType.STRING, "version", "The version of Minecraft to check for."))
                .executes(event -> {
                    MinecraftForgeVersion latest;
                    OptionMapping version = event.getOption("version");
                    try {
                        if (version != null) {
                            latest = new MinecraftForgeVersion(version.getAsString(), ForgeVersionHelper.getForgeVersionsForMcVersion(version.getAsString()));
                        } else {
                            latest = ForgeVersionHelper.getLatestMcVersionForgeVersions();
                        }
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
                        final var cgLink = "[Changelog](https://files.minecraftforge.net/maven/net/minecraftforge/forge/%1$s-%2$s/forge-%1$s-%2$s-changelog.txt)"
                            .formatted(latest.getMcVersion(), latest.getForgeVersion().getLatest());
                        final var embed = new EmbedBuilder();

                        embed.setTitle(String.format("Forge Versions for MC %s", latest.getMcVersion()));
                        embed.addField("Latest", latestForge, true);
                        embed.addField("Recommended", recommendedForge, true);
                        embed.setDescription(cgLink);
                        embed.setColor(Color.BLUE);
                        embed.setTimestamp(Instant.now());
                        event.replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
                    } catch (NullPointerException e) {
                        event.reply("The given Minecraft version " + version.getAsString() + " is invalid.").setEphemeral(true).queue();
                    }
                }),

            SlashCommandBuilder.builder()
                .name("quilt")
                .help("Get the latest Quilt versions.")
                .options(new OptionData(OptionType.STRING, "version",
                    "The version of Minecraft to check for."))
                .executes(event -> {
                    var minecraft = MinecraftVersionHelper.getLatest();
                    final var version = event.getOption("version");
                    if (version != null) {
                        minecraft = version.getAsString();
                    }

                    var quiltMappingsVersion = QuiltVersionHelper.getLatestQuiltMappingsVersion(minecraft);
                    if (quiltMappingsVersion == null) {
                        quiltMappingsVersion = "None";
                    }
                    final var embed = new EmbedBuilder();

                    embed.setTitle("Quilt Versions for Minecraft " + minecraft);
                    embed.addField("Latest Quilt Mappings", quiltMappingsVersion, true);
                    embed.addField("Latest Quilt Standard Libraries",
                        QuiltVersionHelper.getLatestQuiltStandardLibraries(), true);
                    embed.addField("Latest Quilt Loader",
                        QuiltVersionHelper.getLatestLoaderVersion(), true);
                    embed.setColor(Color.MAGENTA);
                    embed.setTimestamp(Instant.now());
                    event.replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
                }),

            SlashCommandBuilder.builder()
                .name("fabric")
                .help("Get the latest Fabric versions.")
                .options(new OptionData(OptionType.STRING, "version", "The version of Minecraft to check for."))
                .executes(event -> {
                    var minecraft = MinecraftVersionHelper.getLatest();
                    final var version = event.getOption("version");
                    if (version != null) {
                        minecraft = version.getAsString();
                    }

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
                    event.replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
                }),

            SlashCommandBuilder.builder()
                .name("minecraft")
                .help("Get the latest Minecraft versions.")
                .executes(event -> {
                    final var embed = new EmbedBuilder();
                    embed.setTitle("Minecraft Versions");
                    embed.addField("Latest", MinecraftVersionHelper.getLatest(), true);
                    embed.addField("Latest Stable", MinecraftVersionHelper.getLatestStable(), true);
                    embed.setColor(Color.GREEN);
                    embed.setTimestamp(Instant.now());
                    event.replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
                })
        )
        .guildOnly(false)
        .build();

}
