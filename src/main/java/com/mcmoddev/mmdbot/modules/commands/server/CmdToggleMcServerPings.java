/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2021 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.modules.commands.server;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

/**
 * The type Cmd toggle mc server pings.
 *
 * @author
 */
public final class CmdToggleMcServerPings extends Command {

    /**
     * Instantiates a new Cmd toggle mc server pings.
     */
    public CmdToggleMcServerPings() {
        super();
        name = "server-pings";
        help = "Add or remove the public server player role from your user.";
        category = new Category("Info");
        aliases = new String[]{"serverpings", "mc-pings", "mc-server-pings", "mcserverpings"};
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
        final var channel = event.getMessage();
        // TODO: Get the per guild ID if enabled for the guild the command was run in.
        final var role = guild.getRoleById(MMDBot.getConfig().getRole("pings.toggle-mc-server-pings"));

        if (role == null) {
            channel.reply("The Server Players role doesn't exist! The config may be broken, "
                + "please contact one of the bot maintainers...").mentionRepliedUser(false).queue();
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

        channel.replyFormat("You %s have the MMD Public Server Players role.", added ? "now" : "no longer").queue();
    }
}
