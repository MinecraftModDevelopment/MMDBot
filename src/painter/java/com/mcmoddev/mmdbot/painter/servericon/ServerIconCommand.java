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
package com.mcmoddev.mmdbot.painter.servericon;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.painter.ThePainter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;

public class ServerIconCommand extends SlashCommand {
    public ServerIconCommand() {
        name = "server-icon";
        help = "Server icon related commands";
        userPermissions = new Permission[] {
            Permission.MANAGE_ROLES
        };
        children = new SlashCommand[] {
            new GenerateIconCommand(), new Set()
        };
        guildOnly = true;
    }

    @Override
    protected void execute(final SlashCommandEvent event) {

    }

    public static final class Set extends SlashCommand {
        private Set() {
            name = "set";
            help = "Set the server's icon";
            userPermissions = new Permission[] {
                Permission.MANAGE_ROLES
            };
            guildOnly = true;
            options = List.of(
                new OptionData(OptionType.ATTACHMENT, "icon", "Server icon as file", false),
                new OptionData(OptionType.STRING, "icon-url", "Server icon url", false)
            );
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            try {
                final String url = event.getOption("icon", () -> event.getOption("icon-url", OptionMapping::getAsString), it -> it.getAsAttachment().getProxyUrl());

                try (final var is = url == null ? null : URI.create(url).toURL().openStream()) {
                    Objects.requireNonNull(event.getGuild()).getManager()
                        .setIcon(is == null ? null : Icon.from(is))
                        .flatMap(it -> event.reply("Icon successfully updated!"))
                        .queue();
                }
            } catch (IOException exception) {
                ThePainter.LOGGER.error("Encountered exception setting server icon: ", exception);
                event.reply("Encountered exception: *" + exception.getMessage() + "*")
                    .setEphemeral(true).queue();
            }
        }
    }
}
