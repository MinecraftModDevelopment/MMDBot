/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
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
package com.mcmoddev.mmdbot.modules.commands.bot.management;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.common.ScamDetector;

/**
 * Refreshes the {@link ScamDetector#SCAM_LINKS}
 *
 * @author matyrobbrt
 */
public class CmdRefreshScamLinks extends Command {

    public CmdRefreshScamLinks() {
        name = "refresh-scam-links";
        aliases = new String[]{"refreshscamlinks"};
        help = "Refreshes the scam links";
        category = new Category("Management");
        hidden = true;
        guildOnly = false;
        ownerCommand = true;
    }

    @Override
    protected void execute(final CommandEvent event) {
        event.getMessage().reply("Refreshing scam links...").mentionRepliedUser(false).queue(msg -> {
            new Thread(() -> {
                if (ScamDetector.setupScamLinks()) {
                    msg.editMessage("Scam links successfully refreshed!").queue();
                } else {
                    msg.editMessage("Scam links could not be refreshed! This is most likely caused by a connection issue.").queue();
                }
            }, "RefreshingScamLinks").start();
        });
    }
}
