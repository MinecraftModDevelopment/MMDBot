package com.mcmoddev.mmdbot.updatenotifiers.forge;

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.time.Instant;
import java.util.TimerTask;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;
import static com.mcmoddev.mmdbot.logging.MMDMarkers.NOTIFIER_FORGE;

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

            StringBuilder logMsg = new StringBuilder(32);
            if (latest.getLatest() != null) {
                if (lastForgeVersions.getLatest() == null) {
                    embed.addField("Latest", String.format("*none* -> **%s**\n", latest.getLatest()), true);
                    embed.setDescription(Utils.makeHyperlink("Changelog", String.format(CHANGELOG_URL_TEMPLATE, mcVersion, latest.getLatest())));
                    changed = true;
                    logMsg.append("Latest, from none to ").append(latest.getLatest());
                } else if (!latest.getLatest().equals(lastForgeVersions.getLatest())) {
                    embed.addField("Latest", String.format("**%s** -> **%s**\n", lastForgeVersions.getLatest(), latest.getLatest()), true);
                    embed.setDescription(Utils.makeHyperlink("Changelog", String.format(CHANGELOG_URL_TEMPLATE, mcVersion, latest.getLatest())));
                    changed = true;
                    logMsg.append("Latest, from ").append(lastForgeVersions.getLatest()).append(" to ").append(latest.getLatest());
                }
            }

            if (latest.getRecommended() != null) {
                if (logMsg.length() != 0) logMsg.append("; ");
                if (lastForgeVersions.getRecommended() == null) {
                    embed.addField("Recommended", String.format("*none* -> **%s**\n", latest.getRecommended()), true);
                    embed.setDescription(Utils.makeHyperlink("Changelog", String.format(CHANGELOG_URL_TEMPLATE, mcVersion, latest.getRecommended())));
                    changed = true;
                    logMsg.append("Recommended, from none to ").append(latest.getLatest());
                } else if (!latest.getRecommended().equals(lastForgeVersions.getRecommended())) {
                    embed.addField("Recommended", String.format("**%s** -> **%s**\n", lastForgeVersions.getRecommended(), latest.getRecommended()), true);
                    embed.setDescription(Utils.makeHyperlink("Changelog", String.format(CHANGELOG_URL_TEMPLATE, mcVersion, latest.getRecommended())));
                    changed = true;
                    logMsg.append("Recommended, from ").append(lastForgeVersions.getLatest()).append(" to ").append(latest.getLatest());
                }
            }

            if (changed) {
                LOGGER.info(NOTIFIER_FORGE, "New Forge version found for {}: {}", mcVersion, logMsg);
                // TODO: save this to disk to persist on restarts
                lastForgeVersions = latest;

                long guildId = getConfig().getGuildID();
                final Guild guild = MMDBot.getInstance().getGuildById(guildId);
                if (guild == null) return;
                long channelId = getConfig().getChannel("notifications.forge");
                final TextChannel channel = guild.getTextChannelById(channelId);
                if (channel == null) return;
                channel.sendMessage(embed.build()).queue();
            } else {
                LOGGER.info(NOTIFIER_FORGE, "No new Forge version found");
            }
        } catch (Exception e) {
            LOGGER.error(NOTIFIER_FORGE, "Error while running", e);
            e.printStackTrace();
        }
    }
}
