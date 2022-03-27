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

import com.jagrosh.jdautilities.command.CooldownScope;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.commander.custompings.CustomPing;
import com.mcmoddev.mmdbot.commander.custompings.CustomPings;
import com.mcmoddev.mmdbot.commander.eventlistener.DismissListener;
import com.mcmoddev.mmdbot.commander.reminders.Reminder;
import com.mcmoddev.mmdbot.commander.reminders.Reminders;
import com.mcmoddev.mmdbot.commander.reminders.SnoozingListener;
import com.mcmoddev.mmdbot.commander.util.TheCommanderUtilities;
import com.mcmoddev.mmdbot.core.util.builder.SlashCommandBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;
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

    public static final class ListCmd extends SlashCommand {

        private static ListenerAdapter listener;

        public static ListenerAdapter getListener() {
            return listener;
        }

        private ListCmd() {
            this.name = "list";
            this.help = "Lists all your custom pings.";
            listener = new ButtonListener();
            guildOnly = true;
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!checkEnabled(event)) return;
            final var userId = event.getUser().getIdLong();
            var reply = event.replyEmbeds(getEmbed(userId, event.getGuild().getIdLong(), 0).build());
            var buttons = createScrollButtons(0, userId, Reminders.getRemindersForUser(userId).size());
            if (buttons.length > 0) {
                reply = reply.addActionRow(buttons);
            }
            reply.queue();
        }

        private static final int ITEMS_PER_PAGE = 20;

        public static EmbedBuilder getEmbed(final long userId, final long guildId, final int index) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Your custom pings:");
            final var pings = CustomPings.getPingsForUser(guildId, userId);

            for (var i = index; i < (pings.size() <= ITEMS_PER_PAGE ? pings.size() : index + ITEMS_PER_PAGE); i++) {
                final var ping = pings.get(i);
                embed.appendDescription(System.lineSeparator());
                embed.appendDescription("%s) `%s` | %s".formatted(i, ping.pattern(), ping.text()));
            }
            return embed;
        }

        private static ItemComponent[] createScrollButtons(int start, long userId, int maximum) {
            Button backward = Button.primary(ButtonListener.BUTTON_ID + "-" + start + "-prev-" + userId + "-" + maximum,
                Emoji.fromUnicode("◀️")).asDisabled();
            Button forward = Button.primary(ButtonListener.BUTTON_ID + "-" + start + "-next-" + userId + "-" + maximum,
                Emoji.fromUnicode("▶️")).asDisabled();

            if (start != 0) {
                backward = backward.asEnabled();
            }

            if (start + 1 < maximum) {
                forward = forward.asEnabled();
            }

            return new ItemComponent[]{backward, forward};
        }

        private static final class ButtonListener extends ListenerAdapter {

            public static final String BUTTON_ID = "custom_pings_list";

            @Override
            public void onButtonInteraction(@NotNull final ButtonInteractionEvent event) {
                if (!checkEnabled(event)) return;
                final var button = event.getButton();
                if (button.getId() == null) {
                    return;
                }

                String[] idParts = button.getId().split("-");
                // custom_pings_list-pageNumber-operation-userid-maximum
                if (idParts.length != 5) {
                    return;
                }

                if (!idParts[0].equals(BUTTON_ID)) {
                    return;
                }

                final int current = Integer.parseInt(idParts[1]);
                final var userId = Long.parseLong(idParts[3]);
                final int maximum = Integer.parseInt(idParts[4]);

                if (idParts[2].equals("next")) {
                    event
                        .editMessageEmbeds(getEmbed(userId, event.getGuild().getIdLong(), current + 1).build())
                        .setActionRow(createScrollButtons(current + 1, userId, maximum))
                        .queue();
                } else {
                    if (idParts[2].equals("prev")) {
                        event
                            .editMessageEmbeds(getEmbed(userId, event.getGuild().getIdLong(), current - 1).build())
                            .setActionRow(createScrollButtons(current - 1, userId, maximum))
                            .queue();
                    }
                }
            }

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
