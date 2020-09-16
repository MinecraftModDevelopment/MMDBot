package com.mcmoddev.mmdbot.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.updatenotifiers.game.MinecraftVersionHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.time.Instant;

/**
 *
 */
public final class CmdMinecraftVersion extends Command {

    /**
     *
     */
    public CmdMinecraftVersion() {
        super();
        this.name = "minecraftv";
        aliases = new String[]{"minecraft", "mcv"};
        help = "Get the latest Minecraft versions";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        final EmbedBuilder embed = new EmbedBuilder();
        final TextChannel channel = event.getTextChannel();

        embed.setTitle("Minecraft Versions");
        embed.addField("Latest", MinecraftVersionHelper.getLatest(), true);
        embed.addField("Latest Stable", MinecraftVersionHelper.getLatestStable(), true);
        embed.setColor(Color.GREEN);
        embed.setTimestamp(Instant.now());
        channel.sendMessage(embed.build()).queue();
    }
}
