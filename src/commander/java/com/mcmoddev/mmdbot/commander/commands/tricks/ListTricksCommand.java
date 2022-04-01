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
package com.mcmoddev.mmdbot.commander.commands.tricks;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.tricks.Tricks;
import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.paginate.PaginatedCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.time.Instant;
import java.util.List;

/**
 * List all registered tricks.
 * All aliases will appear after the name, as `name / name [/ name]`.
 * <p>
 * Takes no parameters.
 *
 * @author Will BL
 * @author Curle
 * @author matyrobbrt
 */
public final class ListTricksCommand extends PaginatedCommand {

    /**
     * Instantiates a new Cmd list tricks.
     */
    public ListTricksCommand() {
        super(TheCommander.getComponentListener("list-tricks-cmd"), Component.Lifespan.TEMPORARY, 10);
        name = "list";
        help = "List all registered tricks.";
        category = new Category("Fun");
        options = List.of(
            new OptionData(OptionType.INTEGER, "page", "The index of the page to display. 1 if not specified.")
        );
    }

    /**
     * Execute.
     *
     * @param event the event
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        if (!event.isFromGuild()) {
            event.deferReply(true).setContent("This command only works in a guild!").queue();
            return;
        }
        if (!TheCommander.getInstance().getGeneralConfig().features().tricks().tricksEnabled()) {
            event.deferReply(true).setContent("Tricks are not enabled!").queue();
            return;
        }
        final var pgIndex = event.getOption("page", 1, OptionMapping::getAsInt);
        final var startingIndex = (pgIndex - 1) * getItemsPerPage();
        final var maximum = Tricks.getTricks().size();
        if (maximum <= startingIndex) {
            event.deferReply().setContent("The page index provided (%s) was too big! There are only %s pages."
                .formatted(pgIndex, getPagesNumber(maximum))).queue();
            return;
        }

        sendPaginatedMessage(event, startingIndex, maximum);
    }

    @Override
    protected EmbedBuilder getEmbed(final int from, final int maximum, final List<String> arguments) {
        return new EmbedBuilder()
            .setTitle("Tricks page %s/%s".formatted(from / getItemsPerPage() + 1, getPagesNumber(maximum)))
            .setDescription(Tricks.getTricks()
                .subList(from, Math.min(from + getItemsPerPage(), maximum))
                .stream()
                .map(it -> it.getNames().stream().reduce("", (a, b) -> (a.isEmpty() ? a : a + " / ") + b))
                .reduce("", (a, b) -> a + "\n" + b))
            .setTimestamp(Instant.now());
    }

    private int getPagesNumber(final int maximum) {
        return maximum % getItemsPerPage() == 0 ? maximum / getItemsPerPage() : maximum / getItemsPerPage() + 1;
    }
}
