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
public class CmdToggleMcServerPings extends Command {

    /**
     *
     */
    public CmdToggleMcServerPings() {
        super();
        name = "serverpings";
        aliases = new String[]{"server-pings", "mc-pings", "mcpings", "toggle-mc-pings", "togglemcpings",
            "mc-server-pings", "mcserverpings"};
        help = "Add or remove the public server player role from your user.";
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
        final Role role = guild.getRoleById(MMDBot.getConfig().getRole("pings.toggle-mc-server-pings"));

        if (role == null) {
            channel.sendMessage("The Server Players role doesn't exist! The config may be broken.").queue();
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

        channel.sendMessageFormat("%s, you %s have the MMD Public Server Players role.", member.getAsMention(),
            added ? "now" : "no longer").queue();
    }
}
