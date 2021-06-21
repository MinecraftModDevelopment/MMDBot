package com.mcmoddev.mmdbot.commands.info.server;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

/**
 *
 * @author
 *
 */
public final class CmdToggleMcServerPings extends Command {

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
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        final var guild = event.getGuild();
        final var channel = event.getTextChannel();
        // TODO: Get the per guild ID if enabled for the guild the command was run in.
        final var role = guild.getRoleById(MMDBot.getConfig().getRole("pings.toggle-mc-server-pings"));

        if (role == null) {
            channel.sendMessage("The Server Players role doesn't exist! The config may be broken.").queue();
            return;
        }

        final var member = event.getMember();
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
