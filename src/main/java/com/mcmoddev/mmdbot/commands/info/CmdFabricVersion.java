package com.mcmoddev.mmdbot.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.updatenotifiers.fabric.FabricVersionHelper;
import com.mcmoddev.mmdbot.updatenotifiers.game.MinecraftVersionHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.time.Instant;

/**
 *
 */
public final class CmdFabricVersion extends Command {

    /**
     *
     */
    public CmdFabricVersion() {
        super();
        this.name = "fabricv";
        aliases = new String[]{"fabric", "yarn"};
        help = "Get the latest Fabric versions";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        final EmbedBuilder embed = new EmbedBuilder();
        final TextChannel channel = event.getTextChannel();

        String mcVersion = event.getArgs().trim();
        if (mcVersion.isEmpty())
        	mcVersion = MinecraftVersionHelper.getLatest();

        String yarnVersion = FabricVersionHelper.getLatestYarn(mcVersion);
        if (yarnVersion == null)
        	yarnVersion = "None";

        embed.setTitle("Fabric Versions for Minecraft "+mcVersion);
        embed.addField("Latest Yarn", yarnVersion, true);
        embed.addField("Latest API", FabricVersionHelper.getLatestApi(), true);
        embed.addField("Latest Loader", FabricVersionHelper.getLatestLoader(), true);
        embed.setColor(Color.WHITE);
        embed.setTimestamp(Instant.now());
        channel.sendMessage(embed.build()).queue();
    }
}
