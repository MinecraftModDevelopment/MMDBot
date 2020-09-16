package com.mcmoddev.mmdbot.commands.staff;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class CmdUnmute extends Command {

    public CmdUnmute() {
        super();
        name = "unmute";
        help = "Unmutes a user. Usage: !mmd-unmute <userID/mention>";
        hidden = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        final Guild guild = event.getGuild();
        final MessageChannel channel = event.getChannel();
        final String[] args = event.getArgs().split(" ");
        final Member author = event.getGuild().getMember(event.getAuthor());
        final Member member = Utils.getMemberFromString(args[0], event.getGuild());
        final Role mutedRole = guild.getRoleById(MMDBot.getConfig().getRoleMuted());
        final TextChannel consoleChannel = guild.getTextChannelById(MMDBot.getConfig().getChannelIDConsole());

        if (author.hasPermission(Permission.KICK_MEMBERS)) {
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
            consoleChannel.sendMessageFormat("Unuted user %s.", member.getAsMention()).queue();
        } else {
            channel.sendMessage("You do not have permission to use this command.").queue();
        }
    }
}
