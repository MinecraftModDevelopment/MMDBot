/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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
package com.mcmoddev.mmdbot.commander.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.commander.util.TheCommanderUtilities;
import com.mcmoddev.mmdbot.core.util.Constants;
import com.mcmoddev.mmdbot.core.util.builder.SlashCommandBuilder;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Class containing different commands.
 */
@UtilityClass
public class Commands {

    @RegisterSlashCommand
    public static final SlashCommand CAT_FACTS = SlashCommandBuilder.builder()
        .name("catfacts")
        .help("Get a random fact about cats, you learn something new every day!")
        .guildOnly(false)
        .executes(event -> {
            final var embed = new EmbedBuilder();
            final var fact = TheCommanderUtilities.getCatFact();
            if (!"".equals(fact)) {
                embed.setColor(Constants.RANDOM.nextInt(0x1000000));
                embed.appendDescription(fact);
                embed.setFooter("Purrwered by https://catfact.ninja");

                event.replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
            }
        })
        .build();
}
