package com.mcmoddev.mmdbot.utilities.updatenotifiers.minecraft;

import com.mcmoddev.mmdbot.core.Utils;
import com.mcmoddev.mmdbot.utilities.console.MMDMarkers;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Instant;
import java.util.TimerTask;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;

/**
 * The type Minecraft update notifier.
 *
 * @author
 */
public final class MinecraftUpdateNotifier extends TimerTask {

    /**
     * The Last latest.
     */
    private String lastLatest;

    /**
     * The Last latest stable.
     */
    private String lastLatestStable;

    /**
     * Instantiates a new Minecraft update notifier.
     */
    public MinecraftUpdateNotifier() {
        lastLatest = MinecraftVersionHelper.getLatest();
        lastLatestStable = MinecraftVersionHelper.getLatestStable();
    }

    /**
     * Run.
     */
    @Override
    public void run() {
        LOGGER.debug(MMDMarkers.NOTIFIER_MC, "Checking for new Minecraft versions...");
        MinecraftVersionHelper.update();
        final String latest = MinecraftVersionHelper.getLatest();
        final String latestStable = MinecraftVersionHelper.getLatestStable();
        final long channelId = getConfig().getChannel("notifications.minecraft");

        if (!lastLatestStable.equals(latestStable)) {
            LOGGER.info(MMDMarkers.NOTIFIER_MC, "New Minecraft release found, from {} to {}", lastLatest, latest);

            Utils.getChannelIfPresent(channelId, channel -> {
                final var embed = new EmbedBuilder();
                embed.setTitle("New Minecraft release available!");
                embed.setDescription(latest);
                embed.setColor(Color.GREEN);
                embed.setTimestamp(Instant.now());
                channel.sendMessageEmbeds(embed.build()).queue();
            });
        } else if (!lastLatest.equals(latest)) {
            LOGGER.info(MMDMarkers.NOTIFIER_MC, "New Minecraft snapshot found, from {} to {}", lastLatest, latest);

            Utils.getChannelIfPresent(channelId, channel -> {
                final var embed = new EmbedBuilder();
                embed.setTitle("New Minecraft snapshot available!");
                embed.setDescription(latest);
                embed.setColor(Color.ORANGE);
                embed.setTimestamp(Instant.now());
                channel.sendMessageEmbeds(embed.build()).queue();
            });
        } else {
            LOGGER.debug(MMDMarkers.NOTIFIER_MC, "No new Minecraft version found");
        }

        lastLatest = latest;
        lastLatestStable = latestStable;
    }
}
