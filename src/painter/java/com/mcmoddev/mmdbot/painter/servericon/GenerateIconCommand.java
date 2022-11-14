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
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;

public class GenerateIconCommand extends SlashCommand {
    public GenerateIconCommand() {
        name = "generate";
        help = "Generate an icon";
        options = List.of(
            new OptionData(OptionType.STRING, "color", "Primary default colour for the icon", true),
            new OptionData(OptionType.STRING, "pattern-color", "Background pattern colour"),
            new OptionData(OptionType.STRING, "ring-color", "Ring colour"),

            new OptionData(OptionType.STRING, "text-alpha", "Text tint alpha value; default: " + IconConfiguration.DEFAULT_TEXT_ALPHA),
            new OptionData(OptionType.STRING, "pattern-alpha", "Background pattern tint alpha value; default: " + IconConfiguration.DEFAULT_BG_PATTERN_ALPHA),
            new OptionData(OptionType.STRING, "ring-alpha", "Ring tint alpha value; default: " + IconConfiguration.DEFAULT_RING_ALPHA),

            new OptionData(OptionType.BOOLEAN, "circular", "If the icon is circular; default: false"),
            new OptionData(OptionType.BOOLEAN, "has-ring", "If the icon has a ring; default: false"),
            new OptionData(OptionType.BOOLEAN, "has-pattern", "If the icon has avatar pattern; default: true"),
            new OptionData(OptionType.BOOLEAN, "has-background", "If the icon has background; default: true"),

            new OptionData(OptionType.BOOLEAN, "set-icon", "Sets the server icon to the generated avatar")
        );
        guildOnly = true;
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        event.deferReply().queue();
        final IconConfiguration.Builder configuration = IconConfiguration.builder();
        parseColour(event, "pattern-color", configuration::setBackgroundPatternColour);
        parseColour(event, "ring-color", configuration::setRingColour);
        parseColour(event, "color", configuration::setColour);

        configuration
            .setTextAlpha(parseFloat(event, "text-alpha", IconConfiguration.DEFAULT_TEXT_ALPHA))
            .setRingAlpha(parseFloat(event, "ring-alpha", IconConfiguration.DEFAULT_RING_ALPHA))
            .setBackgroundPatternAlpha(parseFloat(event, "pattern-alpha", IconConfiguration.DEFAULT_BG_PATTERN_ALPHA))

            .setCircular(event.getOption("circular", false, OptionMapping::getAsBoolean))
            .setHasRing(event.getOption("has-ring", false, OptionMapping::getAsBoolean))
            .setHasBackground(event.getOption("has-background", true, OptionMapping::getAsBoolean))
            .setHasBackgroundPattern(event.getOption("has-pattern", true, OptionMapping::getAsBoolean));

        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(ServerIconMaker.createIcon(configuration.build()), "png", bos);
            final byte[] bytes = bos.toByteArray();

            event.getHook().editOriginalAttachments(
                FileUpload.fromData(bytes, "icon.png")
            ).queue();

            if (event.getOption("set-icon", false, OptionMapping::getAsBoolean)) {
                Objects.requireNonNull(event.getGuild()).getManager()
                    .setIcon(Icon.from(bytes))
                    .queue();
            }
        } catch (IOException exception) {
            ThePainter.LOGGER.error("Encountered exception generating avatar: ", exception);
            event.getHook().editOriginal("Encountered exception: *" + exception.getMessage() + "*").queue();
        }
    }

    public static void parseColour(final SlashCommandEvent event, final String name, IntConsumer caller) {
         String option = event.getOption(name, OptionMapping::getAsString);
         if (option != null) {
            caller.accept(readColour(option));
         }
    }

    public static int readColour(String colour) {
        if (colour.startsWith("0x")) {
            colour = colour.substring(2);
        } else if (colour.startsWith("#")) {
            colour = colour.substring(1);
        }
        return Integer.parseInt(colour, 16);
    }

    private static float parseFloat(final SlashCommandEvent event, final String name, final float defaultValue) {
        return Float.parseFloat(event.getOption(name, () -> String.valueOf(defaultValue), OptionMapping::getAsString));
    }
}
