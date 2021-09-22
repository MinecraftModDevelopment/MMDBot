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
package com.mcmoddev.mmdbot.modules.commands.bot.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.References;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Instant;

/**
 * The type Cmd about.
 *
 * @author ProxyNeko
 * @author Jriwanek
 */
public final class CmdAbout extends Command {

    /**
     * Instantiates a new Cmd about.
     */
    public CmdAbout() {
        super();
        name = "about";
        aliases = new String[]{"build"};
        help = "Gives info about this bot.";
        category = new Category("Info");
        guildOnly = true;
    }

    /**
     * Execute.
     *
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }
        final var embed = new EmbedBuilder();
        final var channel = event.getMessage();

        embed.setTitle("Bot Build info");
        embed.setColor(Color.GREEN);
        embed.setThumbnail(MMDBot.getInstance().getSelfUser().getAvatarUrl());
        embed.setDescription("An in house bot to assists staff with daily tasks and provide fun and useful commands "
            + "for the community, please try ``" + MMDBot.getConfig().getMainPrefix()
            + "help`` for a list of commands!");
        embed.addField("Version:", References.VERSION, true);
        embed.addField("Issue Tracker:", Utils.makeHyperlink("MMDBot's Github", References.ISSUE_TRACKER),
            true);
        embed.addField("Current maintainers:", "jriwanek, WillBL, ProxyNeko, sciwhiz12, Poke, Curle",
            true);
        embed.setTimestamp(Instant.now());
        channel.replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
    }
}
