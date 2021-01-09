package com.mcmoddev.mmdbot.commands.fun;

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
        aliases = new String[]{"toggle-event-pings", "event-pings", "toggleeventpings"};
        help = "Add or remove the Event Notifications role to get or stop getting pings about events in MMD.";
    }

    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) return;
        final TextChannel channel = event.getTextChannel();
        final Guild guild = event.getGuild();
        final Member member = event.getMember();
        final Role role = guild.getRoleById(MMDBot.getConfig().getRole("pings.event-pings"));

        if (role == null) {
            channel.sendMessage("The Event Notifications role doesn't exist! The config must be borked.").queue();
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
        channel.sendMessageFormat("%s, you %s have the Event Notifications role.", member.getAsMention(), added ? "now" : "no longer").queue();
    }
}
