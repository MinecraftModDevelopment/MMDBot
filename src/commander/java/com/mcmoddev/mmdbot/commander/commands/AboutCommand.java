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
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.commander.util.TheCommanderUtilities;
import com.mcmoddev.mmdbot.core.util.event.DismissListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.Color;
import java.util.Objects;

/**
 * Shows information about the bot.
 * Includes:
 * - Short description
 * - Build information
 * - Issue tracker link
 * - List of maintainers
 * <p>
 * Takes no parameters.
 *
 * @author KiriCattus
 * @author Jriwanek
 * @author Curle
 */
public final class AboutCommand extends SlashCommand {

    @RegisterSlashCommand
    public static final AboutCommand CMD = new AboutCommand();

    /**
     * Instantiates a new Cmd about.
     */
    private AboutCommand() {
        name = "about";
        aliases = new String[]{"build"};
        help = "Gives info about this bot.";
        category = new Category("Info");
        guildOnly = false;
    }

    public static final String[] MAINTAINERS = {
        "jriwanek", "KiriCattus", "matyrobbrt", "sciwhiz12", "Curle"
    };

    /**
     * Execute.
     *
     * @param event The {@link SlashCommandEvent event} that triggered this Command.
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        final var embed = new EmbedBuilder();

        embed.setTitle("Bot Build info");
        embed.setColor(Color.GREEN);
        embed.setThumbnail(event.getJDA().getSelfUser().getAvatarUrl());
        embed.setDescription("An in house bot to assists staff with daily tasks and provide fun and useful commands "
            + "for the community, please try ``/help`` for a list of commands!");
        embed.addField("Version:", TheCommander.VERSION, true);
        embed.addField("Issue Tracker:", MarkdownUtil.maskedLink("MMDBot's Github", HelpCommand.ISSUE_TRACKER),
            true);
        embed.addField("Current maintainers:", String.join(", ", MAINTAINERS),
            true);
        embed.addField("Online since: ", TimeFormat.RELATIVE.format(TheCommander.getStartupTime()), false);

        if (event.isFromGuild() && TheCommanderUtilities.memberHasRoles(Objects.requireNonNull(event.getMember()),
            TheCommander.getInstance().getGeneralConfig().roles().getBotMaintainers())) {
            event.deferReply(false).queue(hook -> {
                event.getJDA().retrieveCommands().queue(commands -> {
                    embed.addField("Globally registered commands", String.valueOf(commands.size()), false);
                    hook.editOriginalEmbeds(embed.build()).queue();
                });
            });
        } else {
            event.replyEmbeds(embed.build())
                .addActionRows(ActionRow.of(
                    DismissListener.createDismissButton()
                ))
                .queue();
        }
    }
}
