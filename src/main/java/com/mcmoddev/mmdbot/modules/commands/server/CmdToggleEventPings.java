package com.mcmoddev.mmdbot.modules.commands.server;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

/**
 * The type Cmd toggle event pings.
 *
 * @author
 */
public final class CmdToggleEventPings extends Command {

    /**
     * Instantiates a new Cmd toggle event pings.
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
     * Execute.
     *
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
        final var role = guild.getRoleById(MMDBot.getConfig().getRole("pings.event-pings"));

        if (role == null) {
            channel.sendMessage("The Event Notifications role doesn't exist! The config may be broken.").queue();
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

        channel.sendMessageFormat("%s, you %s have the Event Notifications role.", member.getAsMention(),
            added ? "now" : "no longer").queue();
    }
}
