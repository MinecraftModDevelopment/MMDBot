package com.mcmoddev.mmdbot.updatenotifiers.fabric;

import com.mcmoddev.mmdbot.MMDBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.time.Instant;
import java.util.TimerTask;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;
import static com.mcmoddev.mmdbot.logging.MMDMarkers.NOTIFIER_FABRIC;

public class FabricApiUpdateNotifier extends TimerTask {

    private String lastLatest;

    public FabricApiUpdateNotifier() {
        lastLatest = FabricVersionHelper.getLatestApi();
    }

    @Override
    public void run() {
        String latest = FabricVersionHelper.getLatestApi();

        final long guildId = getConfig().getGuildID();
        final Guild guild = MMDBot.getInstance().getGuildById(guildId);
        if (guild == null) return;
        final long channelId = getConfig().getChannel("notifications.fabric");
        final TextChannel channel = guild.getTextChannelById(channelId);
        if (channel == null) return;

        if (!lastLatest.equals(latest)) {
            LOGGER.info(NOTIFIER_FABRIC, "New Fabric API release found, from {} to {}", lastLatest, latest);
            lastLatest = latest;

            final EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("New Fabric API release available!");
            embed.setDescription(latest);
            embed.setColor(Color.WHITE);
            embed.setTimestamp(Instant.now());
            channel.sendMessage(embed.build()).queue();
        }

        lastLatest = latest;
    }
}
