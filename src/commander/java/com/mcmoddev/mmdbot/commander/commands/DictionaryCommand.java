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
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.core.util.Utils;
import com.mcmoddev.mmdbot.core.util.dictionary.DictionaryEntry;
import com.mcmoddev.mmdbot.core.util.dictionary.DictionaryUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

public final class DictionaryCommand extends SlashCommand {

    @RegisterSlashCommand
    public static final SlashCommand COMMAND = new DictionaryCommand();

    public static ListenerAdapter listener;

    public DictionaryCommand() {
        name = "dictionary";
        help = "Looks up a word";
        options = List.of(new OptionData(OptionType.STRING, "word", "The word to lookup").setRequired(true));
        guildOnly = false;
        listener = new ButtonListener();
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        if (!DictionaryUtils.hasToken()) {
            event.deferReply(true).setContent("I have not been configured in order to lookup definitions! Please contact the bot owner.").queue();
            return;
        }
        final var word = event.getOption("word").getAsString().split(" ")[0];
        try {
            final var definition = DictionaryUtils.getDefinition(word);
            var reply = event.replyEmbeds(getEmbed(word, 0).build());
            var buttons = createScrollButtons(0, word, definition.definitions().size());
            if (buttons.length > 0) {
                reply.addActionRow(buttons);
            }
            reply.queue();
        } catch (DictionaryUtils.DictionaryException e) {
            if (e.getErrorCode() == 404) {
                event.deferReply().setContent("Unknown word: " + word).queue();
            } else {
                event.deferReply(true).setContent("There was an exception while trying to execute that command: " + e.getLocalizedMessage());
                TheCommander.LOGGER.error("Exception while trying to show definition for word {} ", word, e);
            }
        }

    }

    public static EmbedBuilder getEmbed(final String word, final int pageNumber) {
        return getEmbed(DictionaryUtils.getDefinitionNoException(word), pageNumber);
    }

    public static EmbedBuilder getEmbed(final DictionaryEntry entry, final int pageNumber) {
        final var embed = new EmbedBuilder()
            .setTitle("Dictionary lookup of " + entry.word()).setColor(Color.CYAN)
            .setTimestamp(Instant.now()).setFooter("Powered by OwlBot", "https://owlbot.info/static/owlbot/img/logo.png");
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

    private static ItemComponent[] createScrollButtons(int start, String word, int maximum) {
        Button backward = Button.primary(ButtonListener.BUTTON_ID + "-" + start + "-prev-" + word + "-" + maximum,
            Emoji.fromUnicode("◀️")).asDisabled();
        Button forward = Button.primary(ButtonListener.BUTTON_ID + "-" + start + "-next-" + word + "-" + maximum,
            Emoji.fromUnicode("▶️")).asDisabled();

        if (start != 0) {
            backward = backward.asEnabled();
        }

        if (start + 1 < maximum) {
            forward = forward.asEnabled();
        }

        return new ItemComponent[]{backward, forward};
    }

    private final class ButtonListener extends ListenerAdapter {

        public static final String BUTTON_ID = "dictionary";

        @Override
        public void onButtonInteraction(@NotNull final ButtonInteractionEvent event) {
            var button = event.getButton();
            if (button.getId() == null) {
                return;
            }

            String[] idParts = button.getId().split("-");
            // dictionary-pageNumber-operation-word-maximum
            if (idParts.length != 5) {
                return;
            }

            if (!idParts[0].equals(BUTTON_ID)) {
                return;
            }

            final int current = Integer.parseInt(idParts[1]);
            final var word = idParts[3];
            final int maximum = Integer.parseInt(idParts[4]);

            if (idParts[2].equals("next")) {
                event
                    .editMessageEmbeds(getEmbed(word, current + 1).build())
                    .setActionRow(createScrollButtons(current + 1, word, maximum))
                    .queue();
            } else {
                if (idParts[2].equals("prev")) {
                    event
                        .editMessageEmbeds(getEmbed(word, current - 1).build())
                        .setActionRow(createScrollButtons(current - 1, word, maximum))
                        .queue();
                }
            }
        }

    }
}
