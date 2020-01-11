package com.mcmoddev.bot.commands.locked.info;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.MMDBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 *
 */
public final class CmdMe extends CmdUser {

	/**
	 *
	 */
    public CmdMe() {
        super();
        name = "me";
        aliases = new String[]{"whoami", "myinfo"};
        help = "Get information about your own user. **Locked to <#" + MMDBot.getConfig().getBotStuffChannelId() + ">**";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        final TextChannel channel = event.getTextChannel();
        final EmbedBuilder embed = createMemberEmbed(event.getMember());

        final long channelID = MMDBot.getConfig().getBotStuffChannelId();
        if (channel.getIdLong() != channelID) {
            channel.sendMessage("This command is channel locked to <#" + channelID + ">").queue();
        } else {
            channel.sendMessage(embed.build()).queue();
        }
    }
}
