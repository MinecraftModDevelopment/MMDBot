package com.mcmoddev.mmdbot.updatenotifiers.forge;

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.time.Instant;
import java.util.TimerTask;

public class ForgeUpdateNotifier extends TimerTask {

    private static final String CHANGELOG_URL_TEMPLATE = "https://files.minecraftforge.net/maven/net/minecraftforge/forge/%1$s-%2$s/forge-%1$s-%2$s-changelog.txt";
    String mcVersion;
    ForgeVersion lastForgeVersions;

    public ForgeUpdateNotifier() throws Exception {
        MinecraftForgeVersion mcForgeVersions = ForgeVersionHelper.getLatestMcVersionForgeVersions();
        mcVersion = mcForgeVersions.getMcVersion();
        lastForgeVersions = mcForgeVersions.getForgeVersion();
    }

    @Override
    public void run() {
        try {
            mcVersion = ForgeVersionHelper.getLatestMcVersionForgeVersions().getMcVersion();

            ForgeVersion latest = ForgeVersionHelper.getForgeVersionsForMcVersion(mcVersion);

            boolean changed = false;
            EmbedBuilder embed = new EmbedBuilder();
			embed.addField("Minecraft Version", mcVersion, true);
			embed.setTitle("Forge version update");
			embed.setColor(Color.ORANGE);
			embed.setTimestamp(Instant.now());

			if (latest.getLatest() != null) {
                if (lastForgeVersions.getLatest() == null) {
					embed.addField("Latest", String.format("*none* -> **%s**\n", latest.getLatest()), true);
					embed.setDescription(Utils.makeHyperlink("Changelog", String.format(CHANGELOG_URL_TEMPLATE, mcVersion, latest.getLatest())));
                    changed = true;
                } else if (!latest.getLatest().equals(lastForgeVersions.getLatest())) {
                    embed.addField("Latest", String.format("**%s** -> **%s**\n", lastForgeVersions.getLatest(), latest.getLatest()), true);
                    embed.setDescription(Utils.makeHyperlink("Changelog", String.format(CHANGELOG_URL_TEMPLATE, mcVersion, latest.getLatest())));
                    changed = true;
                }
            }

            if (latest.getRecommended() != null) {
                if (lastForgeVersions.getRecommended() == null) {
                    embed.addField("Recommended", String.format("*none* -> **%s**\n", latest.getRecommended()), true);
                    embed.setDescription(Utils.makeHyperlink("Changelog", String.format(CHANGELOG_URL_TEMPLATE, mcVersion, latest.getRecommended())));
                    changed = true;
                } else if (!latest.getRecommended().equals(lastForgeVersions.getRecommended())) {
                    embed.addField("Recommended", String.format("**%s** -> **%s**\n", lastForgeVersions.getRecommended(), latest.getRecommended()), true);
                    embed.setDescription(Utils.makeHyperlink("Changelog", String.format(CHANGELOG_URL_TEMPLATE, mcVersion, latest.getRecommended())));
                    changed = true;
                }
            }

            if (changed) {
                // TODO: save this to disk to persist on restarts
                lastForgeVersions = latest;

                long guildId = MMDBot.getConfig().getGuildID();
                final Guild guild = MMDBot.getInstance().getGuildById(guildId);
				if (guild == null) return;
                long channelId = MMDBot.getConfig().getChannel("notifications.forge");
                final TextChannel channel = guild.getTextChannelById(channelId);
                if (channel == null) return;
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
