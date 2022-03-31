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

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.util.Utils;
import com.mcmoddev.mmdbot.core.commands.PaginatedCommand;
import com.mcmoddev.mmdbot.core.util.dictionary.DictionaryEntry;
import com.mcmoddev.mmdbot.core.util.dictionary.DictionaryUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

public final class DictionaryCommand extends PaginatedCommand {

    @RegisterSlashCommand
    public static final DictionaryCommand COMMAND = new DictionaryCommand();

    public DictionaryCommand() {
        super(TheCommander.getComponentListener("dictionary-cmd"), Component.Lifespan.TEMPORARY, 1);
        name = "dictionary";
        help = "Looks up a word";
        options = List.of(new OptionData(OptionType.STRING, "word", "The word to lookup").setRequired(true));
        guildOnly = false;
        dismissibleMessage = true;
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        if (!DictionaryUtils.hasToken()) {
            event.deferReply(true).setContent("I have not been configured in order to lookup definitions! Please contact the bot owner.").queue();
            return;
        }
        final var word = event.getOption("word", "", OptionMapping::getAsString).split(" ")[0];
        try {
            final var definition = DictionaryUtils.getDefinition(word);
            sendPaginatedMessage(event, definition.definitions().size(), word);
        } catch (DictionaryUtils.DictionaryException e) {
            if (e.getErrorCode() == 404) {
                event.deferReply().setContent("Unknown word: " + word).queue();
            } else {
                event.deferReply(true).setContent("There was an exception while trying to execute that command: " + e.getLocalizedMessage());
                TheCommander.LOGGER.error("Exception while trying to show definition for word {} ", word, e);
            }
        }
    }

    @Override
    protected EmbedBuilder getEmbed(final int startingIndex, final int maximum, final List<String> arguments) {
        return getEmbed(DictionaryUtils.getDefinitionNoException(arguments.get(0)), startingIndex);
    }

    public static EmbedBuilder getEmbed(final DictionaryEntry entry, final int pageNumber) {
        final var embed = new EmbedBuilder()
            .setTitle("Dictionary lookup of " + entry.word()).setColor(Color.CYAN)
            .setTimestamp(Instant.now())
            .setFooter("Powered by OwlBot", "https://owlbot.info/static/owlbot/img/logo.png");
        final var definition = entry.definitions().get(pageNumber);

        if (definition.type() != null) {
            embed.addField("Type", definition.type(), false);
        }

        if (entry.pronunciation() != null) {
            embed.addField("Pronunciation", entry.pronunciation(), true);
        }

        embed.addField("Definition", Utils.uppercaseFirstLetter(definition.definition()), false);

        if (definition.example() != null) {
            embed.addField("Usage example", Utils.uppercaseFirstLetter(definition.example()), false);
        }

        if (definition.emoji() != null) {
            embed.addField("Emoji", Emoji.fromUnicode(definition.emoji()).getAsMention(), false);
        }

        if (definition.image() != null) {
            embed.setThumbnail(definition.image());
        }

        return embed;
    }

}
