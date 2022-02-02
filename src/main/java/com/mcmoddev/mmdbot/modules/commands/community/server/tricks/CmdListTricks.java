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
package com.mcmoddev.mmdbot.modules.commands.community.server.tricks;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.modules.commands.community.PaginatedCommand;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.tricks.Tricks;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Instant;
import java.util.ArrayList;

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
public final class CmdListTricks extends PaginatedCommand {
    private static ButtonListener listListener;

    /**
     * Instantiates a new Cmd list tricks.
     */
    public CmdListTricks() {
        super("list", "List all registered tricks.", true, new ArrayList<>(), 10);
        category = new Category("Fun");
        guildOnly = true;
        this.listener = new TrickListListener();
        listListener = this.listener;
    }

    public static ButtonListener getListListener() {
        return listListener;
    }

    /**
     * Execute.
     *
     * @param event the event
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        if (!event.isFromGuild()) {
            event.deferReply(true).setContent("This command only works in a guild!").queue();
            return;
        }

        updateMaximum(Tricks.getTricks().size());
        sendPaginatedMessage(event);
    }

    @Override
    protected EmbedBuilder getEmbed(int from) {
        return new EmbedBuilder()
            .setTitle("Tricks")
            .setDescription(Tricks.getTricks()
                .subList(from, Math.min(from + items_per_page, maximum))
                .stream()
                .map(it -> it.getNames().stream().reduce("", (a, b) -> (a.isEmpty() ? a : a + " / ") + b))
                .reduce("", (a, b) -> a + "\n" + b))
            .setTimestamp(Instant.now());
    }

    public class TrickListListener extends ButtonListener {
        @Override
        public String getButtonID() {
            return "listtricks";
        }
    }

}
