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
package com.mcmoddev.mmdbot.modules.commands.server;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.entities.Role;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import java.util.List;

/**
 * Toggles whether the user has the Event Pings role.
 *
 * @author Unknown
 * @author Curle
 */
public final class CmdToggleEventPings extends SlashCommand {

    /**
     * Instantiates a new Cmd toggle event pings.
     */
    public CmdToggleEventPings() {
        super();
        name = "eventpings";
        help = "Toggle the event notifications role on your user.";
        category = new Category("Info");
        aliases = new String[]{"eventpings", "event-notifications", "eventnotifications"};
        guildOnly = true;
    }

    /**
     * Execute.
     *
     * @param event The {@link SlashCommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        final var guild = event.getGuild();
        final var role = guild.getRoleById(MMDBot.getConfig().getRole("pings.event-pings"));

        if (role == null) {
            event.reply("The Event Notifications role doesn't exist! Please contact one of the bot devs.")
                .mentionRepliedUser(false).setEphemeral(true).queue();
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

        event.replyFormat("You %s have the Event Notifications role.", added ? "now" : "no longer").setEphemeral(true).queue();
    }
}
