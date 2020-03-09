package com.mcmoddev.bot.commands.locked.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.misc.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

public class CmdUnmute extends Command {

	public CmdUnmute() {
		super();
		name = "unmute";
		aliases = new String[]{};
		help = "Unmutes a user. Usage: !mmd-unmute <userID/mention> **Locked to <#"+MMDBot.getConfig().getChannelIDConsole()+">**.";
	}

	@Override
	protected void execute(CommandEvent event) {
		final Guild guild = event.getGuild();
		final MessageChannel channel = event.getChannel();
		final String[] args = event.getArgs().split(" ");
		final Member member = Utils.getMemberFromString(args[0], event.getGuild());
		final Role mutedRole = guild.getRoleById(MMDBot.getConfig().getRoleMuted());

		final Long channelID = MMDBot.getConfig().getChannelIDConsole();
		if (channel.getIdLong() != channelID) {
			channel.sendMessage("This command is channel locked to <#" + channelID + ">").queue();
			return;
		}
		if (member == null) {
			channel.sendMessage(String.format("User %s not found.", event.getArgs())).queue();
			return;
		}
		if (mutedRole == null) {
			MMDBot.LOGGER.error("Unable to find muted role!");
			return;
		}

		guild.removeRoleFromMember(member, mutedRole).queue();

		channel.sendMessageFormat("Unmuted user %s.", member.getAsMention()).queue();
	}

}
