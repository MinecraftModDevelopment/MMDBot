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
package com.mcmoddev.mmdbot.painter.command;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.painter.servericon.GenerateIconCommand;
import com.mcmoddev.mmdbot.painter.util.ImageUtils;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class TintCommand extends SlashCommand {

    public TintCommand() {
        this.name = "tint";
        this.help = "Apply a tint to an image.";
        this.options = List.of(
            new OptionData(OptionType.ATTACHMENT, "image", "Image to apply tint to.", true),
            new OptionData(OptionType.STRING, "tint", "Colour of the tint to apply.", true),
            new OptionData(OptionType.NUMBER, "transparency", "The transparency used for the tint mask; default: 0.5").setRequiredRange(0d, 1d)
        );
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        event.deferReply().queue();

        try (final var is = URI.create(event.getOption("image", "", it -> it.getAsAttachment().getProxyUrl())).toURL().openStream()) {
            final BufferedImage image = ImageIO.read(is);
            final BufferedImage output = ImageUtils.tint(
                image,
                new Color(GenerateIconCommand.readColour(event.getOption("tint", "", OptionMapping::getAsString))),
                event.getOption("transparency", 0.5f, it -> (float)it.getAsDouble())
            );
            event.getHook().editOriginalAttachments(
                FileUpload.fromData(ImageUtils.toBytes(output, "png"), "tinted.png")
            ).queue();
        } catch (IOException exception) {
            event.getHook().editOriginal("Encountered exception processing image: " + exception.getMessage()).queue();
        }
    }
}
