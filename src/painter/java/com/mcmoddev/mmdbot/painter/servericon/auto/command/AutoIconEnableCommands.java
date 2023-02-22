/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2023 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.painter.servericon.auto.command;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.painter.ThePainter;
import com.mcmoddev.mmdbot.painter.servericon.ServerIconCommand;

public class AutoIconEnableCommands {
    public static final class DisableCommand extends SlashCommand {
        public DisableCommand() {
            this.name = "disable";
            this.help = "Disable the server's current auto icon.";
            this.subcommandGroup = ServerIconCommand.AUTO_ICON_SUBCOMMAND;
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            ThePainter.getInstance().autoIcon().setEnabled(event.getGuild(), false);
            event.reply("Successfully disabled auto icon.").queue();
        }
    }

    public static final class EnableCommand extends SlashCommand {
        public EnableCommand() {
            this.name = "enable";
            this.help = "Enable the server's current auto icon.";
            this.subcommandGroup = ServerIconCommand.AUTO_ICON_SUBCOMMAND;
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            ThePainter.getInstance().autoIcon().setEnabled(event.getGuild(), true);
            event.reply("Successfully enabled auto icon.").queue();
        }
    }
}
