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
package com.mcmoddev.mmdbot.core.util.jda;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import java.util.List;

public interface EnabledRolesCommand {
    List<Long> getEnabledRoles();

    default boolean checkCanUse(final SlashCommandEvent event) {
        if (event.getMember().getRoles().stream().noneMatch(r -> getEnabledRoles().contains(r.getIdLong()))) {
            event.deferReply(true).setContent("You do not have the required role to use this command!");
            return false;
        }
        return true;
    }

    abstract class Base extends SlashCommand implements EnabledRolesCommand {
        protected List<Long> enabledRoles;

        @Override
        public List<Long> getEnabledRoles() {
            return enabledRoles;
        }
    }
}
