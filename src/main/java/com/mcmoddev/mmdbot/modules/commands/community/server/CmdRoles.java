/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
package com.mcmoddev.mmdbot.modules.commands.community.server;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.modules.commands.community.PaginatedCommand;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Shows how many users are in each role in the current Guild.
 * <p>
 * This is the first usage of the Paginated Command.
 *
 * @author KiriCattus
 * @author Curle
 */
public final class CmdRoles extends PaginatedCommand {
    private static RolesListener listener;
    private List<Role> roles = new ArrayList<>();

    /**
     * Constructs this command, to be registered in a {@link com.jagrosh.jdautilities.command.CommandClient}.
     */
    public CmdRoles() {
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
        if (!Utils.checkCommand(this, event)) {
            return;
        }

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
