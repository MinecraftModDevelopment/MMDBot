package com.mcmoddev.mmdbot.updatenotifiers.game;

import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Instant;
import java.util.TimerTask;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;
import static com.mcmoddev.mmdbot.logging.MMDMarkers.NOTIFIER_MC;

public class MinecraftUpdateNotifier extends TimerTask {

    private String lastLatest;
    private String lastLatestStable;

    public MinecraftUpdateNotifier() {
        lastLatest = MinecraftVersionHelper.getLatest();
        lastLatestStable = MinecraftVersionHelper.getLatestStable();
    }

    @Override
    public void run() {
        LOGGER.debug(NOTIFIER_MC, "Checking for new Minecraft versions...");
        String latest = MinecraftVersionHelper.getLatest();
        String latestStable = MinecraftVersionHelper.getLatestStable();
        final long channelId = getConfig().getChannel("notifications.minecraft");

        if (!lastLatestStable.equals(latestStable)) {
            LOGGER.info(NOTIFIER_MC, "New Minecraft release found, from {} to {}", lastLatest, latest);

            Utils.getChannelIfPresent(channelId, channel -> {
                final EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("New Minecraft release available!");
                embed.setDescription(latest);
                embed.setColor(Color.GREEN);
                embed.setTimestamp(Instant.now());
                channel.sendMessage(embed.build()).queue();
            });
        } else if (!lastLatest.equals(latest)) {
            LOGGER.info(NOTIFIER_MC, "New Minecraft snapshot found, from {} to {}", lastLatest, latest);

            Utils.getChannelIfPresent(channelId, channel -> {
                final EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("New Minecraft snapshot available!");
                embed.setDescription(latest);
                embed.setColor(Color.ORANGE);
                embed.setTimestamp(Instant.now());
                channel.sendMessage(embed.build()).queue();
            });
        } else {
            LOGGER.debug(NOTIFIER_MC, "No new Minecraft version found");
        }

        lastLatest = latest;
        lastLatestStable = latestStable;
    }
}
