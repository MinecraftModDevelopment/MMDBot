package com.mcmoddev.mmdbot.commander.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.core.util.command.PaginatedCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class RolesCommand extends PaginatedCommand {
    @RegisterSlashCommand
    public static final SlashCommand COMMAND = new RolesCommand();

    private static RolesListener listener;
    private List<Role> roles = new ArrayList<>();

    private RolesCommand() {
        super("roles",
            "Shows how many users are in each role.",
            true,
            new ArrayList<>(),
            25);

        listener = new RolesListener();
        category = new Category("Info");
    }

    /**
     * Returns the instance of our button listener.
     * Used for handling the pagination buttons.
     */
    public static ButtonListener getListener() {
        return listener;
    }

    /**
     * Executes the command.
     * <p>
     * Sends a message with a listing of all roles in the guild, along with a count of how many members have the role.
     * The message is sent in the same channel where the command was sent from.
     * <p>
     * As a recap of the control flow due to the new Paginated Command system:
     * - Pre checks
     * - Populate the role list, in case it updated
     * - Set the maximum index with the size of the list
     * - Hand off to the Paginated Message handler, which will
     * - Call the {@link #getEmbed(int)} function
     * - Build it
     * - Compare the entries against the saved maximum
     * - Add buttons to scroll if necessary.
     *
     * @param event The {@link SlashCommandEvent event} that triggered this Command.
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        roles = event.getGuild().getRoles()
            .stream()
            .filter(role -> !role.isManaged()) // Filter out managed roles
            .collect(Collectors.toList());

        updateMaximum(roles.size() - 1);

        sendPaginatedMessage(event);
    }

    @Override
    protected EmbedBuilder getEmbed(final int startingIndex) {
        final var embed = new EmbedBuilder();
        embed.setColor(Color.GREEN);
        embed.setTitle("Users With Roles");
        embed.setDescription("A count of how many members have been assigned some of MMD's many roles.");
        embed.addField("Role count:", String.valueOf(roles.size()), true);
        StringBuilder str = new StringBuilder();
        for (int i = startingIndex; i < startingIndex + items_per_page - 1; i++)
            if (i <= maximum)
                str.append(roles.get(i).getAsMention() + ": " + roles.get(i).getGuild().getMembersWithRoles(roles.get(i)).size() + "\n");

        embed.addField("", str.toString(), false);
        embed.setTimestamp(Instant.now());

        return embed;
    }

    public class RolesListener extends PaginatedCommand.ButtonListener {
        @Override
        public String getButtonID() {
            return getName();
        }
    }
}