package com.mcmoddev.mmdbot.modules.commands.general.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.minecraft.MinecraftVersionHelper;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Instant;

/**
 * The type Cmd minecraft version.
 *
 * @author
 */
public final class CmdMinecraftVersion extends Command {

    /**
     * Instantiates a new Cmd minecraft version.
     */
    public CmdMinecraftVersion() {
        super();
        this.name = "minecraft";
        aliases = new String[]{"minecraftv", "mcv"};
        help = "Get the latest Minecraft versions";
    }

    /**
     * Execute.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }
        final var embed = new EmbedBuilder();
        final var channel = event.getTextChannel();

        embed.setTitle("Minecraft Versions");
        embed.addField("Latest", MinecraftVersionHelper.getLatest(), true);
        embed.addField("Latest Stable", MinecraftVersionHelper.getLatestStable(), true);
        embed.setColor(Color.GREEN);
        embed.setTimestamp(Instant.now());
        channel.sendMessageEmbeds(embed.build()).queue();
    }
}
