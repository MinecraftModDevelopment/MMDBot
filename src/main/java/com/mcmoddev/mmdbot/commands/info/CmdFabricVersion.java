package com.mcmoddev.mmdbot.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import com.mcmoddev.mmdbot.updatenotifiers.fabric.FabricVersionHelper;
import com.mcmoddev.mmdbot.updatenotifiers.minecraft.MinecraftVersionHelper;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Instant;

/**
 *
 * @author
 *
 */
public final class CmdFabricVersion extends Command {

    /**
     *
     */
    public CmdFabricVersion() {
        super();
        this.name = "fabric";
        aliases = new String[]{"fabricv", "yarn"};
        help = "Get the latest Fabric versions";
    }

    /**
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        String mcVersion = event.getArgs().trim();
        if (mcVersion.isEmpty()) {
            mcVersion = MinecraftVersionHelper.getLatest();
        }

        String yarnVersion = FabricVersionHelper.getLatestYarn(mcVersion);
        if (yarnVersion == null) {
            yarnVersion = "None";
        }
        final var embed = new EmbedBuilder();
        final var channel = event.getTextChannel();

        embed.setTitle("Fabric Versions for Minecraft " + mcVersion);
        embed.addField("Latest Yarn", yarnVersion, true);
        embed.addField("Latest API", FabricVersionHelper.getLatestApi(), true);
        embed.addField("Latest Loader", FabricVersionHelper.getLatestLoader(), true);
        embed.setColor(Color.WHITE);
        embed.setTimestamp(Instant.now());
        channel.sendMessage(embed.build()).queue();
    }
}
