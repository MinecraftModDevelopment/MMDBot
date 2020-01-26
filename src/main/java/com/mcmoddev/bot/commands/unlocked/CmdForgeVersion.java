package com.mcmoddev.bot.commands.unlocked;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.helpers.forge.ForgeVersionHelper;
import com.mcmoddev.bot.helpers.forge.MinecraftForgeVersion;
import com.mcmoddev.bot.misc.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.time.Instant;
import java.util.Map;

public final class CmdForgeVersion extends Command {
    public CmdForgeVersion() {
        super();
        this.name = "forgev";
        aliases = new String[]{"forge"};
        help = "Get forge versions for latest minecraft version";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        final EmbedBuilder embed = new EmbedBuilder();
        final TextChannel channel = event.getTextChannel();

        MinecraftForgeVersion latest = null;
        try {
            latest = ForgeVersionHelper.getLatestMcVersionForgeVersions();
        } catch (Exception e) {
            channel.sendMessage("Unable to get forge versions.").queue();
            e.printStackTrace();
            return;
        }

        String latestForge = latest.getForgeVersion().getLatest();
        String recommendedForge = latest.getForgeVersion().getRecommended();
        if(recommendedForge == null) {
            recommendedForge = "none";
        }

        embed.setTitle(String.format("Forge Versions for MC %s", latest.getMcVersion()));
        embed.setDescription(String.format("Latest: **%s**\nRecommended: **%s**", latestForge, recommendedForge));
        embed.setColor(Color.ORANGE);
        embed.setTimestamp(Instant.now());
        channel.sendMessage(embed.build()).queue();
    }
}
