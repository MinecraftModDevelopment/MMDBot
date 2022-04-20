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
package com.mcmoddev.mmdbot.commander.commands.menu.user;

import com.jagrosh.jdautilities.command.UserContextMenu;
import com.jagrosh.jdautilities.command.UserContextMenuEvent;
import com.mcmoddev.mmdbot.commander.util.TheCommanderUtilities;
import com.mcmoddev.mmdbot.core.util.event.DismissListener;

public class UserInfoContextMenu extends UserContextMenu {

    public UserInfoContextMenu() {
        name = "User Info";
    }

    @Override
    protected void execute(final UserContextMenuEvent event) {
        if (!event.isFromGuild()) {
            event.deferReply(true).setContent("This command can only be used in a guild!");
            return;
        }
        final var embed = TheCommanderUtilities.createMemberInfoEmbed(event.getTargetMember());
        event.replyEmbeds(embed.build()).addActionRow(DismissListener.createDismissButton(event))
            .mentionRepliedUser(false).queue();
    }
}
