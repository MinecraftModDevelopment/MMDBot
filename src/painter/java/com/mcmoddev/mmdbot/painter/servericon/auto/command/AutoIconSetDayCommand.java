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
import com.mcmoddev.mmdbot.painter.servericon.auto.AutomaticIconConfiguration;
import com.mcmoddev.mmdbot.painter.util.ImageUtils;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.image.BufferedImage;
import java.util.List;

public class AutoIconSetDayCommand extends SlashCommand {
    public AutoIconSetDayCommand() {
        this.name = "set-day";
        this.help = "Set the current day of the auto icon.";
        this.subcommandGroup = ServerIconCommand.AUTO_ICON_SUBCOMMAND;
        this.options = List.of(
            new OptionData(OptionType.INTEGER, "day", "The day to set the auto icon counter to.", true).setRequiredRange(0, Integer.MAX_VALUE),
            new OptionData(OptionType.BOOLEAN, "backwards", "If the auto icon should be changed from the maximum color to the minimum.")
        );
    }

    @Override
    @SneakyThrows
    protected void execute(final SlashCommandEvent event) {
        final AutomaticIconConfiguration conf = ThePainter.getInstance().autoIcon().get(event.getGuild());
        if (conf == null) {
            event.reply("Guild has no auto icon configured.").setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();

        final int day = event.getOption("day", 0, OptionMapping::getAsInt);

        ThePainter.getInstance().dayCounter().update(
            event.getGuild(), day,
            event.getOption("backwards", () -> ThePainter.getInstance().dayCounter().isBackwards(event.getGuild()), OptionMapping::getAsBoolean)
        );

        final BufferedImage image = conf.createImage(day);

        event.getGuild().getManager()
            .setIcon(Icon.from(ImageUtils.toBytes(image, "png")))
            .flatMap(it -> event.getHook().editOriginal("Successfully advanced to day " + day + "."))
            .queue();
    }
}
