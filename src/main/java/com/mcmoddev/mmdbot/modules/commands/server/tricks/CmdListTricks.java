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

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.modules.commands.general.PaginatedCommand;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.tricks.Tricks;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

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
 */
public final class CmdListTricks extends PaginatedCommand {
    private static TrickListListener listener;

    /**
     * Instantiates a new Cmd list tricks.
     */
    public CmdListTricks() {
        super("listtricks", "List all registered tricks.", true, new ArrayList<>(), 10);
        category = new Category("Fun");
        aliases = new String[]{"list-tricks", "tricks"};
        guildOnly = true;
        // we need to use this unfortunately :( can't create more than one commandclient
        guildId = Long.toString(MMDBot.getConfig().getGuildID());
        listener = new TrickListListener();
    }

    public static TrickListListener getListener() {
        return listener;
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
            return "tricklist";
        }
    }

}
