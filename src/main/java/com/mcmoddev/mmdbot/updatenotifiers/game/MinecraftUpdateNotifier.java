package com.mcmoddev.mmdbot.updatenotifiers.game;

import com.mcmoddev.mmdbot.MMDBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.time.Instant;
import java.util.TimerTask;

public class MinecraftUpdateNotifier extends TimerTask {

    private String lastLatest;
    private String lastLatestStable;

    public MinecraftUpdateNotifier() {
        MinecraftVersionHelper.update();
        lastLatest = MinecraftVersionHelper.getLatest();
        lastLatestStable = MinecraftVersionHelper.getLatestStable();
    }

    @Override
    public void run() {
        MinecraftVersionHelper.update();
        String latest = MinecraftVersionHelper.getLatest();
        String latestStable = MinecraftVersionHelper.getLatestStable();

        final long guildId = MMDBot.getConfig().getGuildID();
        final Guild guild = MMDBot.getInstance().getGuildById(guildId);
        final long channelId = MMDBot.getConfig().getChannelIDForgeNotifier();
        final TextChannel channel = guild.getTextChannelById(channelId);

        if (!lastLatestStable.equals(latestStable)) {
            lastLatest = latest;

            final EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("New Minecraft release available!");
            embed.setDescription(latest);
            embed.setColor(Color.GREEN);
            embed.setTimestamp(Instant.now());
            channel.sendMessage(embed.build()).queue();
        } else if (!lastLatest.equals(latest)) {
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
