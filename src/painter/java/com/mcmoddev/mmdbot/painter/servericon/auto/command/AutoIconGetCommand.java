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
import com.mcmoddev.mmdbot.core.util.Utils;
import com.mcmoddev.mmdbot.painter.ThePainter;
import com.mcmoddev.mmdbot.painter.servericon.ServerIconCommand;
import com.mcmoddev.mmdbot.painter.servericon.auto.AutomaticIconConfiguration;
import com.mcmoddev.mmdbot.painter.servericon.auto.DayCounter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class AutoIconGetCommand extends SlashCommand {
    public AutoIconGetCommand() {
        this.name = "get";
        this.help = "Get the current server auto icon.";
        this.subcommandGroup = ServerIconCommand.AUTO_ICON_SUBCOMMAND;
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        try {
            final var data = AutomaticIconConfiguration.get(event.getGuild().getId());
            if (data == null) {
                event.deferReply(true).setContent("This server does not have an automatic icon configured!").queue();
                return;
            }
            final var day = DayCounter.read().getCurrentDay(event.getGuild());
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("Server automatic icon")
                    .setDescription("The server's automatic icon is *" + (data.enabled() ? "enabled" : "disabled") + "*.")
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .addField("Colours", "`%s` -> `%s`".formatted(
                        Utils.rgbToString(data.colours().get(0)),
                        Utils.rgbToString(data.colours().get(data.colours().size() - 1))
                    ), false)
                    .addField("Days", "%s days total.\nCurrently at day %s.%s".formatted(
                        data.colours().size(), day.day(),
                        day.backwards() ? "\n*Going backwards.*" : ""
                    ), false)
                    .setColor(data.colours().get(day.day() - 1))
                    .build())
                .setActionRow(Button.primary(AutoIconSetCommand.BUTTON_ID, "Generate preview"))
                .queue();
        } catch (Exception exception) {
            event.reply("Encountered exception: *" + exception.getMessage() + "*")
                .setEphemeral(true).queue();
            ThePainter.LOGGER.error("Encountered exception in auto-icon get command: ", exception);
        }
    }
}
