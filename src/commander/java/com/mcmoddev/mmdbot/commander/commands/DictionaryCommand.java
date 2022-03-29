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
import com.mcmoddev.mmdbot.core.commands.component.ButtonInteractionContext;
import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.component.ComponentListener;
import com.mcmoddev.mmdbot.core.commands.component.ComponentManager;
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
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class DictionaryCommand extends SlashCommand {

    @RegisterSlashCommand
    public static final DictionaryCommand COMMAND = new DictionaryCommand();
    public static final ComponentListener COMPONENT_LISTENER = TheCommander.getComponentListener("dictionary-cmd")
        .onButtonInteraction(COMMAND::onButtonInteraction)
        .build();

    public DictionaryCommand() {
        name = "dictionary";
        help = "Looks up a word";
        options = List.of(new OptionData(OptionType.STRING, "word", "The word to lookup").setRequired(true));
        guildOnly = false;
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

    private static final Emoji NEXT_EMOJI = Emoji.fromUnicode("▶️");
    private static final Emoji PREVIOUS_EMOJI = Emoji.fromUnicode("◀️");

    private static ItemComponent[] createScrollButtons(String buttonId, int start, String word, int maximum) {
        var backward = Button.primary(Component.createIdWithArguments(buttonId, "prev"), PREVIOUS_EMOJI).asDisabled();
        var forward = Button.primary(Component.createIdWithArguments(buttonId, "next"), NEXT_EMOJI).asDisabled();

        if (start != 0) {
            backward = backward.asEnabled();
        }

        if (start + 1 < maximum) {
            forward = forward.asEnabled();
        }

        return new ItemComponent[]{backward, forward};
    }

    private static ItemComponent[] createScrollButtons(int start, String word, int maximum) {
        final var id = UUID.randomUUID();
        final var comp = new Component(COMPONENT_LISTENER.getName(), id, createButtonArgs(
            start, word, maximum
        ));
        COMPONENT_LISTENER.insertComponent(comp);
        final var idString = id.toString();
        var backward = Button.primary(Component.createIdWithArguments(idString, "prev"), PREVIOUS_EMOJI).asDisabled();
        var forward = Button.primary(Component.createIdWithArguments(idString, "next"), NEXT_EMOJI).asDisabled();

        if (start != 0) {
            backward = backward.asEnabled();
        }

        if (start + 1 < maximum) {
            forward = forward.asEnabled();
        }

        return new ItemComponent[]{
            backward, forward
        };
    }

    public static List<String> createButtonArgs(final int pageNumber, final String word, final int maximum) {
        return List.of(
            String.valueOf(pageNumber), word, String.valueOf(maximum)
        );
    }

    public void onButtonInteraction(final ButtonInteractionContext context) {
        final var event = context.getEvent();
        // pageNumber, word, max
        final int pageNumber = context.getArgument(0, () -> 0, Integer::parseInt);
        final var word = context.getArguments().get(1);
        final int maximum = context.getArgument(2, () -> DictionaryUtils.getDefinitionNoException(word).definitions().size(), Integer::parseInt);

        final var id = context.getComponentId().toString();
        switch (context.getButtonArguments().get(0)) {
            case "next" -> {
                event.editMessageEmbeds(getEmbed(word, pageNumber + 1).build())
                    .setActionRow(createScrollButtons(id, pageNumber + 1, word, maximum))
                    .queue();

                context.updateArgument(0, String.valueOf(pageNumber + 1));
            }
            case "prev" -> {
                event.editMessageEmbeds(getEmbed(word, pageNumber - 1).build())
                    .setActionRow(createScrollButtons(id, pageNumber - 1, word, maximum))
                    .queue();

                context.updateArgument(0, String.valueOf(pageNumber - 1));
            }
        }
    }

}
