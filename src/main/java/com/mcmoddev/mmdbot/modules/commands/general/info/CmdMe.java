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
package com.mcmoddev.mmdbot.modules.commands.general.info;

import com.jagrosh.jdautilities.command.Command;
import com.mcmoddev.mmdbot.modules.commands.server.moderation.CmdUser;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.ArrayList;

/**
 * Get information about the user who initiated the command.
 *
 * @author ProxyNeko
 * @author sciwhiz12
 * @author Curle
 */
public final class CmdMe extends CmdUser {

    /**
     * Instantiates a new Cmd me.
     */
    public CmdMe() {
        super();
        name = "me";
        help = "Get information about your own user.";
        category = new Category("Info");
        aliases = new String[]{"whoami", "myinfo"};
        guildOnly = true;
        requiredRole = null;

        options = new ArrayList<>();
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

        final EmbedBuilder embed = createMemberEmbed(event.getMember());
        event.replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
    }
}
