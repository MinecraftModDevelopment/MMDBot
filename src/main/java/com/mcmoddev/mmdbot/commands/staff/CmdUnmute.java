package com.mcmoddev.mmdbot.commands.staff;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;
import static com.mcmoddev.mmdbot.logging.MMDMarkers.MUTING;

public class CmdUnmute extends Command {

    public CmdUnmute() {
        super();
        name = "unmute";
        help = "Unmutes a user. Usage: !mmd-unmute <userID/mention>";
        hidden = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (!Utils.checkCommand(this, event)) return;
        final Guild guild = event.getGuild();
        final MessageChannel channel = event.getChannel();
        final String[] args = event.getArgs().split(" ");
        final Member author = event.getGuild().getMember(event.getAuthor());
        if (author == null) return;
        final Member member = Utils.getMemberFromString(args[0], event.getGuild());
        final long mutedRoleID = getConfig().getRole("muted");
        final Role mutedRole = guild.getRoleById(mutedRoleID);

        if (author.hasPermission(Permission.KICK_MEMBERS)) {
            if (member == null) {
                channel.sendMessage(String.format("User %s not found.", event.getArgs())).queue();
                return;
            }

            if (mutedRole == null) {
                LOGGER.error(MUTING, "Unable to find muted role {}", mutedRoleID);
                return;
            }

            guild.removeRoleFromMember(member, mutedRole).queue();
            channel.sendMessageFormat("Unmuted user %s.", member.getAsMention()).queue();
            LOGGER.info(MUTING, "User {} was unmuted by {}", member, author);
        } else {
            channel.sendMessage("You do not have permission to use this command.").queue();
        }
    }
}
