/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2021 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.modules.commands.server.tricks;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.tricks.Tricks;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author williambl
 * <p>
 * The type Cmd list tricks.
 */
public final class CmdListTricks extends Command {

    private static final int TRICKS_PER_PAGE = 10;

    /**
     * Instantiates a new Cmd list tricks.
     */
    public CmdListTricks() {
        super();
        name = "listtricks";
        aliases = new String[]{"list-tricks", "tricks"};
        help = "Lists all tricks";
    }

    /**
     * Execute.
     *
     * @param event the event
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }
        final var channel = event.getTextChannel();
        final var builder = getTrickList(0);

        if (!builder.isEmpty()) {
            var message = channel.sendMessageEmbeds(builder);
            Component[] buttons = createActionRow(0);
            if (buttons.length > 0) {
                message = message.setActionRow(buttons);
            }
            message.queue();
        } else {
            channel.sendMessage("No tricks currently exist!").queue();
        }
    }

    private static MessageEmbed getTrickList(int from) {
        return new EmbedBuilder()
            .setTitle("Tricks")
            .setDescription(Tricks.getTricks()
                .subList(from, from + TRICKS_PER_PAGE)
                .stream()
                .map(it -> it.getNames().stream().reduce("", (a, b) -> (a.isEmpty() ? a : a + " / ") + b))
                .reduce("", (a, b) -> a + "\n" + b))
            .build();
    }

    private static Component[] createActionRow(int lowestTrickIndex) {
        List<Component> components = new ArrayList<>();
        if (lowestTrickIndex != 0) {
            components.add(Button.secondary(ButtonListener.BUTTON_ID_PREFIX + "-" + lowestTrickIndex + "-prev", Emoji.fromUnicode("◀️")));
        }
        if (lowestTrickIndex + TRICKS_PER_PAGE < Tricks.getTricks().size()) {
            components.add(Button.primary(ButtonListener.BUTTON_ID_PREFIX + "-" + lowestTrickIndex + "-next", Emoji.fromUnicode("▶️")));
        }
        return components.toArray(new Component[0]);
    }

    public static class ButtonListener extends ListenerAdapter {
        private static final String BUTTON_ID_PREFIX = "tricklist";

        @Override
        public void onButtonClick(@NotNull final ButtonClickEvent event) {
            var button = event.getButton();
            if (button == null || button.getId() == null) {
                return;
            }

            String[] idParts = button.getId().split("-");
            if (idParts.length != 3) {
                return;
            }

            if (!idParts[0].equals(BUTTON_ID_PREFIX)) {
                return;
            }

            int current = Integer.parseInt(idParts[1]);

            if (idParts[2].equals("next")) {
                event
                    .editMessageEmbeds(List.of(getTrickList(current + TRICKS_PER_PAGE)))
                    .setActionRow(createActionRow(current + TRICKS_PER_PAGE))
                    .queue();
            } else if (idParts[2].equals("prev")) {
                event
                    .editMessageEmbeds(List.of(getTrickList(current - TRICKS_PER_PAGE)))
                    .setActionRow(createActionRow(current - TRICKS_PER_PAGE))
                    .queue();
            }
        }
    }
}
