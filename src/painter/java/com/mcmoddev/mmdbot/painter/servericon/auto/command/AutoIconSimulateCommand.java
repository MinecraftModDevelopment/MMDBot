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
package com.mcmoddev.mmdbot.painter.servericon.auto.command;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.painter.servericon.ServerIconCommand;
import com.mcmoddev.mmdbot.painter.servericon.ServerIconMaker;
import com.mcmoddev.mmdbot.painter.servericon.auto.AutomaticIconConfiguration;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.utils.FileUpload;

public class AutoIconSimulateCommand extends SlashCommand {
    public AutoIconSimulateCommand() {
        this.name = "simulate";
        this.help = "Simulate an auto icon cycle.";
        // We want to copy the options
        this.options = AutoIconSetCommand.INSTANCE.getOptions();
        this.subcommandGroup = ServerIconCommand.AUTO_ICON_SUBCOMMAND;
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        event.deferReply()
            .flatMap(it -> it.editOriginalAttachments(createPreview(event)))
            .queue();
    }

    @SneakyThrows
    private FileUpload createPreview(SlashCommandEvent event) {
        final AutomaticIconConfiguration cfg = AutoIconSetCommand.create(event);
        final byte[] bytes = ServerIconMaker.createSlideshow(cfg);
        return FileUpload.fromData(bytes, "icons.gif");
    }
}
