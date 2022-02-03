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
package com.mcmoddev.mmdbot.modules.commands.community.contextmenu.user;

import com.jagrosh.jdautilities.command.UserContextMenu;
import com.jagrosh.jdautilities.command.UserContextMenuEvent;
import com.mcmoddev.mmdbot.modules.commands.community.contextmenu.GuildOnlyMenu;
import com.mcmoddev.mmdbot.modules.commands.community.information.CmdUser;

public class ContextMenuUserInfo extends UserContextMenu implements GuildOnlyMenu {

    public ContextMenuUserInfo() {
        name = "User Info";
    }

    @Override
    protected void execute(final UserContextMenuEvent event) {
        final var embed = CmdUser.createMemberEmbed(event.getTargetMember());
        event.replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
    }
}