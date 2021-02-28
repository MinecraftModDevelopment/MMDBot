package com.mcmoddev.mmdbot.commands.info.server;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

/**
 *
 */
public class CmdToggleEventPings extends Command {

    /**
     *
     */
    public CmdToggleEventPings() {
        super();
        name = "eventpings";
        aliases = new String[]{"event-pings", "event-notifications", "eventnotifications",
            "toggle-event-pings", "toggleeventpings"};
        help = "Toggle the event notifications role on your user.";
        guildOnly = true;
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        final Guild guild = event.getGuild();
        final Member member = event.getMember();
        final TextChannel channel = event.getTextChannel();
        //TODO get the per guild ID if enabled for the guild the command was run in.
        final Role role = guild.getRoleById(MMDBot.getConfig().getRole("pings.event-pings"));

        if (role == null) {
            channel.sendMessage("The Event Notifications role doesn't exist! The config may be broken.").queue();
            return;
        }

        final List<Role> roles = member.getRoles();
        boolean added;
        if (roles.contains(role)) {
            guild.removeRoleFromMember(member, role).queue();
            added = false;
        } else {
            guild.addRoleToMember(member, role).queue();
            added = true;
        }

        channel.sendMessageFormat("%s, you %s have the Event Notifications role.", member.getAsMention(),
            added ? "now" : "no longer").queue();
    }
}
