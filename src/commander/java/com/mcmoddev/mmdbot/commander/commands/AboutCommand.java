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

import static com.mcmoddev.mmdbot.core.util.Utils.bytesToFriendly;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.commander.util.TheCommanderUtilities;
import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.component.ComponentListener;
import com.mcmoddev.mmdbot.core.commands.component.context.ButtonInteractionContext;
import com.mcmoddev.mmdbot.core.util.event.DismissListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.Color;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

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

    public static final ComponentListener COMPONENT_LISTENER = TheCommander.getComponentListener("about-cmd")
        .onButtonInteraction(CMD::onButtonInteraction)
        .build();

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
        embed.addField("Memory Usage: ", getMemoryUsage(), true);
        embed.addField("CPU Load: ", ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getCpuLoad() * 100 + "%", true);
        embed.setTimestamp(Instant.now());

        if (event.isFromGuild() && TheCommanderUtilities.memberHasRoles(Objects.requireNonNull(event.getMember()),
            TheCommander.getInstance().getGeneralConfig().roles().getBotMaintainers())) {
            event.deferReply(false).queue(hook -> {
                event.getJDA().retrieveCommands().queue(commands -> {
                    embed.addField("Globally registered commands", String.valueOf(commands.size()), false);
                    hook.editOriginalEmbeds(embed.build()).queue();
                });
            });
        } else {
            final var id = UUID.randomUUID();
            COMPONENT_LISTENER.insertComponent(id, Component.Lifespan.TEMPORARY);
            event.replyEmbeds(embed.build())
                .addActionRows(ActionRow.of(
                    DismissListener.createDismissButton(), Button.primary(Component.createIdWithArguments(id.toString(), ButtonType.THREAD_DUMP.toString()), "Thread Dump")
                ))
                .queue();
        }
    }

    public void onButtonInteraction(final ButtonInteractionContext context) {
        final var type = ButtonType.valueOf(context.getItemComponentArguments().get(0));
        if (type == ButtonType.THREAD_DUMP) {
            context.getEvent().replyFile(getThreadDump().getBytes(), "dump.txt").queue();
        }
    }

    public String getThreadDump() {
        final var builder = new StringBuilder();
        final var traces = Thread.getAllStackTraces();
        builder.append("Thread dump at ")
            .append(DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
            .append(System.lineSeparator())
            .append(System.lineSeparator());
        builder.append("All active threads:");
        traces.keySet().forEach(thread -> builder
            .append("    ")
            .append(System.lineSeparator())
            .append(thread.getName())
            .append(thread.getThreadGroup() == null ? "" : "[" + thread.getThreadGroup().getName() + "]")
            .append('@')
            .append(thread.getId())
        );
        builder.append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("Detailed information about each thread:")
                .append(System.lineSeparator());
        traces.forEach((thread, stacks) -> {
            builder.append(System.lineSeparator());
            builder.append(buildThreadInfo(thread));
            builder.append(System.lineSeparator());
            for (final var stack : stacks) {
                builder.append("  ")
                    .append("at ")
                    .append(stack.toString());
            }
        });
        return builder.toString();
    }

    public String buildThreadInfo(final Thread thread) {
        return "\"%s@%s\" %sprio=%s %s".formatted(
            thread.getThreadGroup() == null ? thread.getName() : thread.getName() + " [" + thread.getThreadGroup().getName() + "]",
            thread.getId(),
            thread.isDaemon() ? "daemon" : "", thread.getPriority(), thread.getState().toString().toLowerCase(Locale.ROOT)
        );
    }

    enum ButtonType {
        THREAD_DUMP
    }

    public static String getMemoryUsage() {
        final var runtime = Runtime.getRuntime();
        final var memoryUsed = runtime.totalMemory() - runtime.freeMemory();
        return bytesToFriendly(memoryUsed / 1024) + "/" + bytesToFriendly(runtime.totalMemory() / 1024);
    }
}
