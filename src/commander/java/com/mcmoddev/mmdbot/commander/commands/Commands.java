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
package com.mcmoddev.mmdbot.commander.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.commander.util.TheCommanderUtilities;
import com.mcmoddev.mmdbot.commander.util.mc.MCVersions;
import com.mcmoddev.mmdbot.core.util.Constants;
import com.mcmoddev.mmdbot.core.util.builder.SlashCommandBuilder;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Class containing different commands.
 */
@UtilityClass
public class Commands {

    @RegisterSlashCommand
    public static final SlashCommand MC_AGE = SlashCommandBuilder.builder()
        .name("mcage")
        .help("Tells you the age of a id of Minecraft.")
        .options(new OptionData(OptionType.STRING, "version", "The id to lookup information for").setAutoComplete(true))
        .onAutocomplete(event -> {
            final var currentOption = event.getOption("version", "", OptionMapping::getAsString).toLowerCase(Locale.ROOT);
            event.replyChoices(MCVersions.getKnownVersions()
                    .stream()
                    .filter(currentOption::startsWith)
                    .limit(5)
                    .map(s -> new Command.Choice(s, s))
                    .toList())
                .queue();
        })
        .executes(event -> {
            final var versionOpt = event.getOption("version", "", OptionMapping::getAsString);
            final var version = MCVersions.getVersionInfo(versionOpt);
            if (version == null) {
                event.deferReply().setContent("Unknown id: **%s**".formatted(versionOpt)).queue();
            } else {
                event.deferReply().queue(hook -> {
                    final var period = Period.between(version.getReleaseTime().atZone(ZoneOffset.UTC).toLocalDate(),
                        LocalDate.now(ZoneOffset.UTC));
                    final List<String> components = new ArrayList<>();
                    class Helper {
                        static void addComponent(List<String> components, int amount, String base) {
                            if (amount > 0) {
                                components.add(amount + " " + base + (amount > 1 ? "s" : ""));
                            }
                        }
                    }
                    Helper.addComponent(components, period.getYears(), "year");
                    Helper.addComponent(components, period.getMonths(), "month");
                    Helper.addComponent(components, period.getDays(), "day");
                    final String reply = "Minecraft " + versionOpt;
                    String age;
                    if (components.size() == 0) {
                        hook.editOriginal(reply + " was released today!").queue();
                        return;
                    }
                    if (components.size() == 1) {
                        age = components.get(0);
                    } else if (components.size() == 2) {
                        age = components.get(0) + " and " + components.get(1);
                    } else {
                        age = String.format("%s, %s, and %s", components.toArray(Object[]::new));
                    }
                    hook.editOriginal(reply + " is **" + age + "** old today.").queue();
                });
            }
        })
        .build();

    @RegisterSlashCommand
    public static final SlashCommand CAT_FACTS = SlashCommandBuilder.builder()
        .name("catfacts")
        .help("Get a random fact about cats, you learn something new every day!")
        .guildOnly(false)
        .executes(event -> {
            final var embed = new EmbedBuilder();
            final var fact = TheCommanderUtilities.getCatFact();
            if (!fact.isBlank()) {
                embed.setColor(Constants.RANDOM.nextInt(0x1000000));
                embed.appendDescription(fact);
                embed.setFooter("Purrwered by https://catfact.ninja");

                event.replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
            }
        })
        .build();
}
