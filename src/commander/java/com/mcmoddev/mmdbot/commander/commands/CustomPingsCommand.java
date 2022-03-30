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
import com.mcmoddev.mmdbot.commander.custompings.CustomPing;
import com.mcmoddev.mmdbot.commander.custompings.CustomPings;
import com.mcmoddev.mmdbot.core.util.event.DismissListener;
import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.util.builder.SlashCommandBuilder;
import com.mcmoddev.mmdbot.core.util.command.PaginatedCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CustomPingsCommand {

    @RegisterSlashCommand
    public static final SlashCommand COMMAND = SlashCommandBuilder.builder()
        .name("custom-pings")
        .guildOnly(true)
        .help("Custom ping related commands.")
        .children(new Add(), new ListCmd(), new Remove())
        .build();

    public static final class Add extends SlashCommand {
        private Add() {
            name = "add";
            help = "Adds a custom ping.";
            options = List.of(
                new OptionData(OptionType.STRING, "pattern", "The regex pattern of the custom ping.", true),
                new OptionData(OptionType.STRING, "text", "The text of the custom ping", true)
            );
            guildOnly = true;
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!checkEnabled(event)) return;
            final var pings = CustomPings.getPingsForUser(event.getGuild().getIdLong(), event.getUser().getIdLong());
            final var limit = TheCommander.getInstance().getGeneralConfig().features().customPings().getLimitPerUser();
            if (pings.size() >= limit) {
                event.deferReply(true).setContent("You cannot add any other custom pings as you have reached the limit of %s."
                    .formatted(limit)).queue();
                return;
            }
            final var patternText = event.getOption("pattern", "", OptionMapping::getAsString);
            try {
                final var pattern = Pattern.compile(patternText);
                final var text = event.getOption("text", "", OptionMapping::getAsString);
                final var ping = new CustomPing(pattern, text);
                CustomPings.addPing(event.getGuild().getIdLong(), event.getUser().getIdLong(), ping);
                event.deferReply().setContent("Custom ping added!").queue();
            } catch (PatternSyntaxException e) {
                event.deferReply().setContent("Invalid regex pattern: `%s`!".formatted(patternText)).queue();
            }
        }
    }

    public static final class ListCmd extends PaginatedCommand {

        private ListCmd() {
            super(TheCommander.getComponentListener("list-custom-pings-cmd"), Component.Lifespan.TEMPORARY, 20, true);
            this.name = "list";
            this.help = "Lists all your custom pings.";
            guildOnly = true;
            dismissibleMessage = true;
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!checkEnabled(event)) return;

            // Args:
            // guildId, userId
            sendPaginatedMessage(
                event,
                CustomPings.getPingsForUser(Objects.requireNonNull(event.getGuild()).getIdLong(), event.getUser().getIdLong()).size(),
                event.getGuild().getId(),
                event.getUser().getId()
            );
        }

        @Override
        protected EmbedBuilder getEmbed(final int index, final int maximum, final List<String> arguments) {
            if (!TheCommander.getInstance().getGeneralConfig().features().areQuotesEnabled()) {
                return new EmbedBuilder().setDescription("Quotes are not enabled!");
            }
            final long guildId = Long.parseLong(arguments.get(0));
            final long userId = Long.parseLong(arguments.get(1));
            final var embed = new EmbedBuilder();
            embed.setTitle("Your custom pings:");
            final var pings = CustomPings.getPingsForUser(guildId, userId);

            for (var i = index; i < index + itemsPerPage - 1; i++) {
                if (i < pings.size()) {
                    final var ping = pings.get(i);
                    embed.appendDescription(System.lineSeparator());
                    embed.appendDescription("%s) `%s` | %s".formatted(i, ping.pattern(), ping.text()));
                }
            }
            return embed;
        }

    }

    public static final class Remove extends SlashCommand {

        private Remove() {
            name = "remove";
            help = "Removes a custom ping.";
            options = List.of(
                new OptionData(OptionType.INTEGER, "index", "The index of the ping to remove. Do not provide to clear all pings.")
            );
            guildOnly = true;
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!checkEnabled(event)) return;
            final var gId = event.getGuild().getIdLong();
            final var index = event.getOption("index", -1, OptionMapping::getAsInt);
            final var userId = event.getUser().getIdLong();
            final var userRems = CustomPings.getPingsForUser(gId, userId);
            if (index != -1 && userRems.size() <= index) {
                event.deferReply(true).setContent("Unknown index: **" + index + "**").queue();
                return;
            }
            if (index == -1) {
                CustomPings.clearPings(gId, userId);
                event.deferReply().setContent("Removed all custom pings!").queue();
            } else {
                final var cp = userRems.get(index);
                CustomPings.removePing(gId, userId, cp);
                event.deferReply().setContent("Removed custom ping with the index: **%s**!".formatted(index)).queue();
            }
        }
    }

    private static boolean checkEnabled(final IReplyCallback event) {
        final var enabled = TheCommander.getInstance().getGeneralConfig().features().customPings().areEnabled();
        if (!enabled) {
            event.deferReply(true).setContent("Custom Pings are disabled!").queue();
        }
        return enabled;
    }
}
