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
import com.mcmoddev.mmdbot.painter.util.ImageUtils;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class RescaleCommand extends SlashCommand {
    public RescaleCommand() {
        this.name = "rescale";
        this.help = "Rescale an image.";
        this.options = List.of(
            new OptionData(OptionType.ATTACHMENT, "image", "Image to rescale", true),
            new OptionData(OptionType.STRING, "target-size", "Image target size. Example: 720 (W and H), 1048x720 (WxH)", true),
            new OptionData(OptionType.STRING, "strategy", "Image scaling strategy").addChoices(
                Stream.of(ScaleStrategy.values()).map(it -> new Command.Choice(it.friendlyName, it.name())).toList()
            )
        );
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        event.deferReply().queue();

        try (final var is = URI.create(event.getOption("image", "", it -> it.getAsAttachment().getProxyUrl())).toURL().openStream()) {
            final String[] dimensions = event.getOption("target-size", "720", OptionMapping::getAsString).toLowerCase(Locale.ROOT).split("x");
            final int width = Integer.parseInt(dimensions[0]);
            final int height = dimensions.length > 1 ? Integer.parseInt(dimensions[1]) : width;

            final BufferedImage image = ImageIO.read(is);
            final BufferedImage output = ImageUtils.resizeImage(image, width, height,
                event.getOption("strategy", ScaleStrategy.DEFAULT, it -> ScaleStrategy.valueOf(it.getAsString())).hint);
            event.getHook().editOriginalAttachments(
                FileUpload.fromData(ImageUtils.toBytes(output, "png"), "rescaled.png")
            ).queue();
        } catch (IOException exception) {
            event.getHook().editOriginal("Encountered exception processing image: " + exception.getMessage()).queue();
        }
    }

    public enum ScaleStrategy {
        DEFAULT("Default", Image.SCALE_DEFAULT),
        FAST("Fast", Image.SCALE_FAST),
        SMOOTH("Smooth", Image.SCALE_SMOOTH),
        REPLICATE("Replicate", Image.SCALE_REPLICATE),
        AREA_AVERAGING("Area Averaging", Image.SCALE_AREA_AVERAGING);

        private final String friendlyName;
        private final int hint;

        ScaleStrategy(final String friendlyName, final int hint) {
            this.friendlyName = friendlyName;
            this.hint = hint;
        }
    }
}
