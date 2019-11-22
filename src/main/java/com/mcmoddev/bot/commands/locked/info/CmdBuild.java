package com.mcmoddev.bot.commands.locked.info;

import java.awt.Color;
import java.time.Instant;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.MMDBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 *
 */
public final class CmdBuild extends Command {

	/**
	 *
	 */
   public CmdBuild() {
       super();
       name = "build";
       aliases = new String[0];
       help = "Gives build info about this bot. **Locked to <#" + MMDBot.getConfig().getBotStuffChannelId() + ">**";
   }

   /**
    *
    */
   @Override
   protected void execute(final CommandEvent event) {
       final Guild guild = event.getGuild();
       final EmbedBuilder embed = new EmbedBuilder();
       final TextChannel channel = event.getTextChannel();

       embed.setTitle("Bot Build info");
       embed.setColor(Color.GREEN);
       embed.setThumbnail(guild.getIconUrl());
       embed.addField("Version:", MMDBot.VERSION, true);
       embed.addField("Issue Tracker:", MMDBot.ISSUE_TRACKER, true);
       embed.setTimestamp(Instant.now());

       final long channelID = MMDBot.getConfig().getBotStuffChannelId();
       if (channel.getIdLong() != channelID) {
           channel.sendMessage("This command is channel locked to <#" + channelID + ">").queue();
       } else {
           channel.sendMessage(embed.build()).queue();
       }
   }

}
