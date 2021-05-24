package com.mcmoddev.mmdbot.updatenotifiers.fabric;

import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Instant;
import java.util.TimerTask;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;
import static com.mcmoddev.mmdbot.logging.MMDMarkers.NOTIFIER_FABRIC;

/**
 *
 * @author
 *
 */
public final class FabricApiUpdateNotifier extends TimerTask {

	/**
	 *
	 */
    private String lastLatest;

    /**
     *
     */
    public FabricApiUpdateNotifier() {
        lastLatest = FabricVersionHelper.getLatestApi();
    }

    @Override
    public void run() {
        LOGGER.debug(NOTIFIER_FABRIC, "Checking for new Fabric API versions...");
        final String latest = FabricVersionHelper.getLatestApi();

        final long channelId = getConfig().getChannel("notifications.fabric");

        if (!lastLatest.equals(latest)) {
            LOGGER.info(NOTIFIER_FABRIC, "New Fabric API release found, from {} to {}", lastLatest, latest);
            lastLatest = latest;

            Utils.getChannelIfPresent(channelId, channel -> {
                final EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("New Fabric API release available!");
                embed.setDescription(latest);
                embed.setColor(Color.WHITE);
                embed.setTimestamp(Instant.now());
                channel.sendMessage(embed.build()).queue();
            });
        } else {
            LOGGER.debug(NOTIFIER_FABRIC, "No new Fabric API version found");
        }

        lastLatest = latest;
    }
}
