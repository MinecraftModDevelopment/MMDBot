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
package com.mcmoddev.mmdbot.watcher.serveravatar;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.watcher.TheWatcher;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.IntConsumer;

public class GenerateAvatarCommand extends SlashCommand {
    public GenerateAvatarCommand() {
        name = "generate";
        help = "Generate an avatar";
        userPermissions = new Permission[] {
            Permission.MANAGE_ROLES
        };
        options = List.of(
            new OptionData(OptionType.STRING, "color", "Primary default colour for the avatar", true),
            new OptionData(OptionType.STRING, "pattern-color", "Background pattern colour"),
            new OptionData(OptionType.STRING, "ring-color", "Ring colour"),

            new OptionData(OptionType.STRING, "text-alpha", "Text tint alpha value"),
            new OptionData(OptionType.STRING, "pattern-alpha", "Background pattern tint alpha value"),
            new OptionData(OptionType.STRING, "ring-alpha", "Ring tint alpha value"),

            new OptionData(OptionType.BOOLEAN, "circular", "If the avatar is circular; default: false"),
            new OptionData(OptionType.BOOLEAN, "has-ring", "If the avatar has a ring; default: false"),
            new OptionData(OptionType.BOOLEAN, "has-pattern", "If the avatar has avatar pattern; default: true"),
            new OptionData(OptionType.BOOLEAN, "has-background", "If the avatar has background; default: true")
        );
        guildOnly = true;
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        event.deferReply().queue();
        final AvatarConfiguration.Builder configuration = AvatarConfiguration.builder();
        parseColour(event, "pattern-color", configuration::setBackgroundPatternColour);
        parseColour(event, "ring-color", configuration::setRingColour);
        parseColour(event, "color", configuration::setColour);

        configuration
            .setTextAlpha(parseFloat(event, "text-alpha", AvatarConfiguration.DEFAULT_TEXT_ALPHA))
            .setRingAlpha(parseFloat(event, "ring-alpha", AvatarConfiguration.DEFAULT_RING_ALPHA))
            .setBackgroundPatternAlpha(parseFloat(event, "pattern-alpha", AvatarConfiguration.DEFAULT_BG_PATTERN_ALPHA))

            .setCircular(event.getOption("circular", false, OptionMapping::getAsBoolean))
            .setHasRing(event.getOption("has-ring", false, OptionMapping::getAsBoolean))
            .setHasBackground(event.getOption("has-background", true, OptionMapping::getAsBoolean))
            .setHasBackgroundPattern(event.getOption("has-pattern", true, OptionMapping::getAsBoolean));

        try {
            final var bos = new ByteArrayOutputStream();
            ImageIO.write(ServerAvatarMaker.createAvatar(configuration.build()), "png", bos);
            event.getHook().editOriginalAttachments(
                FileUpload.fromData(bos.toByteArray(), "avatar.png")
            ).queue();
        } catch (IOException exception) {
            TheWatcher.LOGGER.error("Encountered exception generating avatar: ", exception);
            event.getHook().editOriginal("Encountered exception: *" + exception.getMessage() + "*").queue();
        }
    }

    private static void parseColour(final SlashCommandEvent event, final String name, IntConsumer caller) {
         String option = event.getOption(name, OptionMapping::getAsString);
         if (option != null) {
            if (option.startsWith("0x")) {
                option = option.substring(2);
            } else if (option.startsWith("#")) {
                option = option.substring(1);
            }
            caller.accept(Integer.parseInt(option, 16));
         }
    }

    private static float parseFloat(final SlashCommandEvent event, final String name, final float defaultValue) {
        return Float.parseFloat(event.getOption(name, () -> String.valueOf(defaultValue), OptionMapping::getAsString));
    }
}
