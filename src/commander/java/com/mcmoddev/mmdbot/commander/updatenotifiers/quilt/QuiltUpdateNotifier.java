package com.mcmoddev.mmdbot.commander.updatenotifiers.quilt;

import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifiers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.*;
import java.time.Instant;

import static com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifiers.LOGGER;
import static com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifiers.MARKER;

/**
 * The type Quilt update notifier.
 *
 * @author KiriCattus
 */
public class QuiltUpdateNotifier implements Runnable {

    /**
     * The latest known release of Quilt and tools.
     */
    private String latestKnownRelease;

    /**
     * Instantiates a new Quilt Standard Libraries update notifier.
     */
    public QuiltUpdateNotifier() {
        latestKnownRelease = QuiltVersionHelper.getLatestQuiltStandardLibraries();
    }

    @Override
    public void run() {
        if (TheCommander.getInstance() == null) {
            UpdateNotifiers.LOGGER.warn(MARKER, "Cannot start Quilt update notifier while the bot instance is null.");
            return;
        }
        UpdateNotifiers.LOGGER.debug(MARKER, "Checking for new Quilt Standard Libraries version...");
        final String latest = QuiltVersionHelper.getLatestQuiltStandardLibraries();

        if (!latestKnownRelease.equals(latest)) {
            UpdateNotifiers.LOGGER.info(MARKER, "New Quilt Standard Libraries release found, from {} to {}",
                latestKnownRelease, latest);
            latestKnownRelease = latest;

            TheCommander.getInstance().getGeneralConfig().channels().updateNotifiers().quilt().forEach(chId -> {
                final var channel = TheCommander.getJDA().getChannelById(MessageChannel.class, chId);
                if (channel != null) {
                    final var embed = new EmbedBuilder();
                    embed.setTitle("New Quilt release available!");
                    embed.setDescription(latest);
                    embed.setColor(Color.WHITE);
                    embed.setTimestamp(Instant.now());
                    channel.sendMessageEmbeds(embed.build()).queue(msg -> {
                        if (channel.getType() == ChannelType.NEWS) {
                            msg.crosspost().queue();
                        }
                    });
                }
            });
        } else {
            LOGGER.debug(MARKER, "No new Quilt Standard Libraries version found");
        }

        latestKnownRelease = latest;
    }
}
