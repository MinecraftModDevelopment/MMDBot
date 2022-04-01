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
package com.mcmoddev.mmdbot.commander.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.paginate.PaginatedCommand;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

public final class RolesCommand extends PaginatedCommand {
    @RegisterSlashCommand
    public static final SlashCommand COMMAND = new RolesCommand();

    private RolesCommand() {
        super(TheCommander.getComponentListener("roles-cmd"), Component.Lifespan.TEMPORARY, 25);
        name = "roles";
        help = "Shows how many users are in each role.";
        guildOnly = true;

        category = new Category("Info");
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
     * - Call the {@link #getEmbed(int, int, List)} method
     * - Build it
     * - Compare the entries against the saved maximum
     * - Add buttons to scroll if necessary.
     *
     * @param event The {@link SlashCommandEvent event} that triggered this Command.
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        final var rolesSize = event.getGuild().getRoles()
            .stream()
            .filter(role -> !role.isManaged()) // Filter out managed roles
            .count();

        sendPaginatedMessage(event, (int) rolesSize - 1, event.getGuild().getId());
    }

    @Override
    protected EmbedBuilder getEmbed(final int startingIndex, final int maximum, final List<String> arguments) {
        final var guild = TheCommander.getInstance().getJda().getGuildById(arguments.get(0));
        if (guild == null) {
            return new EmbedBuilder().setDescription("Unknown guild.");
        }
        final var roles = guild.getRoles()
            .stream()
            .filter(role -> !role.isManaged())
            .toList();

        final var embed = new EmbedBuilder();
        embed.setColor(Color.GREEN);
        embed.setTitle("Users With Roles");
        embed.setDescription("A count of how many members have been assigned each role.");
        embed.addField("Role count:", String.valueOf(roles.size()), true);
        StringBuilder str = new StringBuilder();
        for (int i = startingIndex; i < startingIndex + getItemsPerPage() - 1; i++)
            if (i <= maximum) {
                str.append(roles.get(i).getAsMention())
                    .append(": ")
                    .append(roles.get(i).getGuild().getMembersWithRoles(roles.get(i)).size())
                    .append("\n");
            }

        embed.addField("", str.toString(), false);
        embed.setTimestamp(Instant.now());

        return embed;
    }
}
