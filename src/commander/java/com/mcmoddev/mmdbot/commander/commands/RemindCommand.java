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

import com.ibm.icu.impl.duration.DurationFormatter;
import com.ibm.icu.impl.duration.DurationFormatterFactory;
import com.ibm.icu.text.DurationFormat;
import com.jagrosh.jdautilities.command.CooldownScope;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
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

public class RemindCommand {

    public static final LongSupplier MAXIMUM_TIME = () -> TheCommander.getInstance().getGeneralConfig().features().reminders().getTimeLimit();

    @RegisterSlashCommand
    public static final SlashCommand COMMAND = SlashCommandBuilder.builder()
        .name("remind")
        .guildOnly(false)
        .help("Reminder related commands.")
        .children(new In(), new At(), new ListCmd(), new Remove())
        .build();

    public static final class In extends SlashCommand {
        private In() {
            this.name = "in";
            this.help = "Adds a reminder relative to the current time.";
            options = List.of(
                new OptionData(OptionType.STRING, "time", "The relative time of the reminder. The format is: <time><unit>. Example: 12h15m", true),
                new OptionData(OptionType.STRING, "content", "The content of the reminder.")
            );
            cooldown = 20;
            cooldownScope = CooldownScope.USER;
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!checkEnabled(event)) return;
            if (event.isFromGuild() && TheCommanderUtilities.memberHasRoles(event.getMember(), TheCommander.getInstance().getGeneralConfig().roles().getBotMaintainers())) {
                // Remove the cooldown from bot maintainers, for testing purposes
                event.getClient().applyCooldown(getCooldownKey(event), 1);
            }
            final var userId = event.getUser().getIdLong();
            if (Reminders.userReachedMax(userId)) {
                event.deferReply(true).setContent("You cannot add any other reminders as you have reached the limit of %s pending ones. Remove some or wait until they fire."
                    .formatted(TheCommander.getInstance().getGeneralConfig().features().reminders().getLimitPerUser())).queue();
                return;
            }
            try {
                final var time = getDurationFromInput(event.getOption("time", "", OptionMapping::getAsString));
                final var limit = MAXIMUM_TIME.getAsLong();
                if (time.getSeconds() > limit) {
                    event.deferReply().setContent("Cannot add a reminder that is over the limit of %s seconds!".formatted(limit)).queue();
                    return;
                }
                final var remTime = Instant.now().plus(time);
                Reminders.addReminder(new Reminder(event.getOption("content", "", OptionMapping::getAsString),
                    event.getChannel().getIdLong(), event.isFromType(ChannelType.PRIVATE), userId, remTime));
                event.deferReply().setContent("Successfully scheduled reminder on %s (%s)!".formatted(TimeFormat.DATE_TIME_LONG.format(remTime),
                    TimeFormat.RELATIVE.format(remTime)))
                    .addActionRow(DismissListener.createDismissButton(userId))
                    .queue();
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                event.deferReply(true).setContent("Invalid time provided!").queue();
            }
        }
    }

    public static final class At extends SlashCommand {

        public static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .optionalStart()
                    .appendLiteral('_')
                    .appendValue(ChronoField.HOUR_OF_DAY, 2)
                        .optionalStart()
                            .appendLiteral(':')
                            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                            .optionalStart()
                                .appendLiteral(':')
                                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                            .optionalEnd()
                        .optionalEnd()
                .optionalEnd()
            .appendOffsetId()
            .toFormatter();

        public At() {
            name = "at";
            help = "Adds a reminder relative to an absolute time.";
            options = List.of(
                new OptionData(OptionType.STRING, "time", "The time of the reminder. The format is: uuuu-MM-dd_HH:mm+timezone. Example: 2022-03-25_21:17+02:00", true),
                new OptionData(OptionType.STRING, "content", "The content of the reminder.")
            );
            cooldown = 20;
            cooldownScope = CooldownScope.USER;
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!checkEnabled(event)) return;
            if (event.isFromGuild() && TheCommanderUtilities.memberHasRoles(event.getMember(), TheCommander.getInstance().getGeneralConfig().roles().getBotMaintainers())) {
                // Remove the cooldown from bot maintainers, for testing purposes
                event.getClient().applyCooldown(getCooldownKey(event), 1);
            }
            final var userId = event.getUser().getIdLong();
            if (Reminders.userReachedMax(userId)) {
                event.deferReply(true).setContent("You cannot add any other reminders as you have reached the limit of %s pending ones. Remove some or wait until they fire."
                    .formatted(TheCommander.getInstance().getGeneralConfig().features().reminders().getLimitPerUser())).queue();
                return;
            }
            final var limit = MAXIMUM_TIME.getAsLong();
            try {
                final var now = Instant.now();
                final var time = Instant.from(FORMATTER.parse(event.getOption("time", "", OptionMapping::getAsString)));
                if (time.isBefore(now)) {
                    event.deferReply().setContent("Cannot add a reminder in the past!").queue();
                    return;
                }
                if (time.minus(now.getEpochSecond(), ChronoUnit.SECONDS).getEpochSecond() > limit) {
                    event.deferReply().setContent("Cannot add a reminder that is over the limit of %s seconds!".formatted(limit)).queue();
                    return;
                }
                Reminders.addReminder(new Reminder(event.getOption("content", "", OptionMapping::getAsString),
                    event.getChannel().getIdLong(), event.isFromType(ChannelType.PRIVATE), userId, time));
                event.deferReply().setContent("Successfully scheduled reminder on %s (%s)!".formatted(TimeFormat.DATE_TIME_LONG.format(time),
                        TimeFormat.RELATIVE.format(time)))
                    .addActionRow(DismissListener.createDismissButton(userId))
                    .queue();
            } catch (DateTimeParseException e) {
                event.deferReply(true).setContent("Invalid time provided!").queue();
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
            this.help = "Lists all of your reminders.";
            listener = new ButtonListener();
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!checkEnabled(event)) return;
            final var userId = event.getUser().getIdLong();
            var reply = event.replyEmbeds(getEmbed(userId, 0).build());
            var buttons = createScrollButtons(0, userId, Reminders.getRemindersForUser(userId).size());
            if (buttons.length > 0) {
                reply = reply.addActionRow(buttons);
            }
            reply.queue();
        }

        private static final int ITEMS_PER_PAGE = 10;

        public static EmbedBuilder getEmbed(final long userId, final int index) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setDescription("Your reminders:");
            final var reminders = Reminders.getRemindersForUser(userId);

            for (var i = index; i < (reminders.size() <= ITEMS_PER_PAGE ? reminders.size() : index + ITEMS_PER_PAGE); i++) {
                final var reminder = reminders.get(i);
                embed.appendDescription(System.lineSeparator());
                embed.appendDescription("**%s**: *%s* - <#%s> at %s (%s)".formatted(i, reminder.content().isBlank() ? "No Content." : reminder.content(), reminder.channelId(),
                    TimeFormat.DATE_TIME_LONG.format(reminder.time()), TimeFormat.RELATIVE.format(reminder.time())));
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

            public static final String BUTTON_ID = "remind_list";

            @Override
            public void onButtonInteraction(@NotNull final ButtonInteractionEvent event) {
                var button = event.getButton();
                if (button.getId() == null) {
                    return;
                }

                String[] idParts = button.getId().split("-");
                // remind_list-pageNumber-operation-userid-maximum
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
                        .editMessageEmbeds(getEmbed(userId, current + 1).build())
                        .setActionRow(createScrollButtons(current + 1, userId, maximum))
                        .queue();
                } else {
                    if (idParts[2].equals("prev")) {
                        event
                            .editMessageEmbeds(getEmbed(userId, current - 1).build())
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
            help = "Removes a reminder.";
            options = List.of(
                new OptionData(OptionType.INTEGER, "index", "The index of the reminder to remove. Do not provide to clear all reminders.")
            );
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!checkEnabled(event)) return;
            final var index = event.getOption("index", -1, OptionMapping::getAsInt);
            final var userId = event.getUser().getIdLong();
            final var userRems = Reminders.getRemindersForUser(userId);
            if (index != -1 && userRems.size() <= index) {
                event.deferReply(true).setContent("Unknown index: **" + index + "**").queue();
                return;
            }
            if (index == -1) {
                Reminders.clearAllUserReminders(userId);
                event.deferReply().setContent("Removed all reminders!").queue();
            } else {
                final var rem = userRems.get(index);
                Reminders.removeReminder(rem);
                event.deferReply().setContent("Removed reminder with the index: **%s**!".formatted(index)).queue();
            }
        }
    }

    private static boolean checkEnabled(final SlashCommandEvent event) {
        final var enabled = TheCommander.getInstance().getGeneralConfig().features().reminders().areEnabled();
        if (!enabled) {
            event.deferReply(true).setContent("Reminders are disabled!").queue();
        }
        return enabled;
    }

    private static List<String> splitInput(String str) {
        final var list = new ArrayList<String>();
        var builder = new StringBuilder();
        for (final var ch : str.toCharArray()) {
            builder.append(ch);
            if (!Character.isDigit(ch)) {
                list.add(builder.toString());
                builder = new StringBuilder();
            }
        }
        return list;
    }

    private static Duration getDurationFromInput(String input) {
        final var data = splitInput(input);
        var duration = Duration.ofSeconds(0);
        for (final var dt : data) {
            final var time = SnoozingListener.decodeTime(dt);
            final var asSeconds = time.amount() * time.unit().getDuration().getSeconds();
            duration = duration.plus(asSeconds, ChronoUnit.SECONDS);
        }
        return duration;
    }
}
