package com.mcmoddev.mmdbot.updatenotifiers.game;

import com.mcmoddev.mmdbot.MMDBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

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
        String latest = MinecraftVersionHelper.getLatest();
        String latestStable = MinecraftVersionHelper.getLatestStable();

        final long guildId = getConfig().getGuildID();
        final Guild guild = MMDBot.getInstance().getGuildById(guildId);
        if (guild == null) return;
        final long channelId = getConfig().getChannel("notifications.minecraft");
        final TextChannel channel = guild.getTextChannelById(channelId);
        if (channel == null) return;

        if (!lastLatestStable.equals(latestStable)) {
            LOGGER.info(NOTIFIER_MC, "New Minecraft release found, from {} to {}", lastLatest, latest);
            lastLatest = latest;

            final EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("New Minecraft release available!");
            embed.setDescription(latest);
            embed.setColor(Color.GREEN);
            embed.setTimestamp(Instant.now());
            channel.sendMessage(embed.build()).queue();
        } else if (!lastLatest.equals(latest)) {
            LOGGER.info(NOTIFIER_MC, "New Minecraft snapshot found, from {} to {}", lastLatest, latest);
            lastLatest = latest;

            final EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("New Minecraft snapshot available!");
            embed.setDescription(latest);
            embed.setColor(Color.ORANGE);
            embed.setTimestamp(Instant.now());
            channel.sendMessage(embed.build()).queue();
        }

        lastLatest = latest;
        lastLatestStable = latestStable;
    }
}
