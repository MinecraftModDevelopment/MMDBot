package com.mcmoddev.mmdbot.commands.info.server;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Instant;
import java.util.stream.Collectors;

/**
 * A {@link Command} for listing the roles and their member counts.
 *
 * @author
 *
 */
public final class CmdRoles extends Command {

    /**
     * Constructs this command, to be registered in a {@link com.jagrosh.jdautilities.command.CommandClient}.
     */
    public CmdRoles() {
        super();
        name = "roles";
        aliases = new String[]{"roleinfo", "server-roles"};
        help = "Gives a count of users per role.";
    }

    /**
     * Executes the command.
     * <p>
     * Sends a message with a listing of all roles in the guild, along with a count of how many members hold those roles.
     * The message is sent in the same channel where the command was sent from.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }
        final var embed = new EmbedBuilder();
        final var channel = event.getTextChannel();

        final String rolesCount = event.getGuild().getRoles()
            .stream()
            .filter(role -> !role.isManaged()) // Filter out managed roles
            .map(role -> role.getAsMention() + ": " + role.getGuild().getMembersWithRoles(role).size())
            .collect(Collectors.joining("\n"));

        embed.setColor(Color.GREEN);
        embed.setTitle("Users With Roles");
        embed.setDescription("A count of how many members have been assigned some of MMD's many roles.");
        embed.addField("Role count:", rolesCount, true);
        embed.setTimestamp(Instant.now());
        channel.sendMessage(embed.build()).queue();
    }
}
