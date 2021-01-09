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
public class CmdToggleMcServerPings extends Command {

    /**
     *
     */
    public CmdToggleMcServerPings() {
        super();
        name = "serverpings";
        aliases = new String[]{"toggle-mc-server-pings", "mc-server-pings", "toggle-mc-server-announcements", "mc-server-announcements"};
        help = "Add or remove the public server player role from your user.";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) return;
        final TextChannel channel = event.getTextChannel();
        final Guild guild = event.getGuild();
        final Member member = event.getMember();
        final Role role = guild.getRoleById(MMDBot.getConfig().getRole("pings.toggle-mc-server-pings"));

        if (role == null) {
            channel.sendMessage("The MMD Public Server Players role doesn't exist! The config is borked.").queue();
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
        channel.sendMessageFormat("%s, you %s have the MMD Public Server Players role.", member.getAsMention(), added ? "now" : "no longer").queue();
    }
}
