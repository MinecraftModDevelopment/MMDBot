package com.mcmoddev.bot.jobs;

import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.helpers.forge.ForgeVersion;
import com.mcmoddev.bot.helpers.forge.ForgeVersionHelper;
import com.mcmoddev.bot.helpers.forge.MinecraftForgeVersion;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.time.Instant;
import java.util.TimerTask;

public class ForgeUpdateNotifier extends TimerTask {

	String mcVersion;
	ForgeVersion lastForgeVersions;

	private static final String CHANGELOG_URL_TEMPLATE = "https://files.minecraftforge.net/maven/net/minecraftforge/forge/%1$s-%2$s/forge-%1$s-%2$s-changelog.txt";

	public ForgeUpdateNotifier() throws Exception {
		MinecraftForgeVersion mcForgeVersions = ForgeVersionHelper.getLatestMcVersionForgeVersions();

		mcVersion = mcForgeVersions.getMcVersion();
		lastForgeVersions = mcForgeVersions.getForgeVersion();
	}

	@Override
	public void run() {
		try {
			ForgeVersion latest = ForgeVersionHelper.getForgeVersionsForMcVersion(mcVersion);

			boolean changed = false;
			StringBuilder body = new StringBuilder(String.format("Minecraft version: **%s**\n", mcVersion));


			if (latest.getLatest() != null) {
				if (lastForgeVersions.getLatest() == null) {
					body.append(String.format("Latest: **none** -> **%s**\n", latest.getLatest()));
					body.append(String.format("Changelog: "+CHANGELOG_URL_TEMPLATE+"\n", mcVersion, latest.getLatest()));
					changed = true;
				} else if (!latest.getLatest().equals(lastForgeVersions.getLatest())) {
					body.append(String.format("Latest: **%s** -> **%s**\n", lastForgeVersions.getLatest(), latest.getLatest()));
					body.append(String.format("Changelog: "+CHANGELOG_URL_TEMPLATE+"\n", mcVersion, latest.getLatest()));
					changed = true;
				}
			}

			if (latest.getRecommended() != null) {
				if (lastForgeVersions.getRecommended() == null) {
					body.append(String.format("Recommended: **none** -> **%s**\n", latest.getRecommended()));
					body.append(String.format("Changelog: "+CHANGELOG_URL_TEMPLATE+"\n", mcVersion, latest.getRecommended()));
					changed = true;
				} else if (!latest.getRecommended().equals(lastForgeVersions.getRecommended())) {
					body.append(String.format("Recommended: **%s** -> **%s**\n", lastForgeVersions.getRecommended(), latest.getRecommended()));
					body.append(String.format("Changelog: "+CHANGELOG_URL_TEMPLATE+"\n", mcVersion, latest.getRecommended()));
					changed = true;
				}
			}

			if (changed) {
				// TODO: save this to disk to persist on restarts
				lastForgeVersions = latest;

				final EmbedBuilder embed = new EmbedBuilder();
				Long guildId = MMDBot.getConfig().getGuildID();
				final Guild guild = MMDBot.getInstance().getGuildById(guildId);
				Long channelId = MMDBot.getConfig().getChannelIDForgeNotifier();
				final TextChannel channel = guild.getTextChannelById(channelId);
				embed.setTitle("Forge version update");
				embed.setDescription(body.toString());
				embed.setColor(Color.ORANGE);
				embed.setTimestamp(Instant.now());
				channel.sendMessage(embed.build()).queue();
			} else {
				MMDBot.LOGGER.info("[ForgeUpdateNotifier] No Forge version update");
			}
		} catch (Exception e) {
			MMDBot.LOGGER.error("[ForgeUpdateNotifier] Error running forge update notifier", e);
			e.printStackTrace();
		}
	}
}
