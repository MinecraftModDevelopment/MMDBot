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
package com.mcmoddev.updatinglauncher.discord.commands;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.updatinglauncher.Config;
import com.mcmoddev.updatinglauncher.JarUpdater;
import com.mcmoddev.updatinglauncher.Main;
import com.mcmoddev.updatinglauncher.ThreadInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

public class StatusCommand extends ULCommand implements EventListener {

    public static final String BUTTON_NAME = "thread_dump";

    public StatusCommand(final Supplier<JarUpdater> jarUpdater, final Config.Discord config) {
        super(jarUpdater, config);
        name = "status";
        help = "Gets information about the process status.";
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        final var updater = jarUpdater.get();
        final var process = updater.getProcess();
        final var version = updater.getJarVersion();
        if (process == null) {
            event.deferReply().addEmbeds(
                new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Process is not running")
                    .addField("Jar Version", version.orElse("Unknown"), true)
                    .setTimestamp(Instant.now())
                    .build()
            ).queue();
        } else {
            final var embed = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Process is running.")
                .addField("Jar Version", version.orElse("Unknown"), true)
                .addField("Running Since", process.process().info().startInstant().map(TimeFormat.RELATIVE::format).orElse("Unknown startup time"), true)
                .setTimestamp(Instant.now());

            final var connector = process.connector();
            if (connector != null) {
                try {
                    final var mem = connector.getMemoryUsage();
                    final var memoryUsed = mem.totalMemory() - mem.freeMemory();
                    embed.addField("Memory Usage", bytesToFriendly(memoryUsed / 1024) + "/" + bytesToFriendly(mem.totalMemory() / 1024), true);

                    embed.addField("CPU Load", connector.getCPULoad() * 100 + "%", true);
                } catch (RemoteException ignored) {}
            }

            event.deferReply()
                .addEmbeds(embed.build())
                .addActionRow(Button.primary(BUTTON_NAME, "\uD83D\uDCF7 Thread Dump"))
                .queue();
        }
    }

    @Override
    public void onEvent(@NotNull final GenericEvent e$) {
        if (!(e$ instanceof ButtonInteractionEvent event)) return;
        if (event.getButton().getId() == null || !event.getButton().getId().equals(BUTTON_NAME) || !event.isFromGuild()) {
            event.deferEdit().queue();
            return;
        }
        final var member = Objects.requireNonNull(event.getMember());
        if (member.getRoles().stream().noneMatch(r -> isEnabled(r.getId()))) {
            event.deferEdit().queue();
            return;
        }
        final var process = jarUpdater.get().getProcess();
        if (process == null) {
            event.deferReply(true).setContent("The process is not running.").queue();
            return;
        }
        final var connector = process.connector();
        if (connector != null) {
            try {
                final var threads = connector.getThreads();
                final var dumpPath = Main.UL_DIRECTORY.resolve("thread-dumps").resolve(Instant.now().getEpochSecond() + ".md");
                if (!Files.exists(dumpPath.getParent())) {
                    Files.createDirectories(dumpPath.getParent());
                }
                try (final var writer = Files.newBufferedWriter(dumpPath, StandardOpenOption.CREATE)) {
                    writer.write(getThreadDump(threads));
                    event.deferReply().addFile(dumpPath.toFile(), "dump.md").queue();
                }
            } catch (Exception e) {
                event.deferReply(true).setContent("Could not retrieve process threads!").queue();
                Main.LOG.error("Error retrieving process threads: ", e);
            }
        } else {
            event.deferReply(true).setContent("Could not retrieve process threads!").queue();
        }
    }

    public String getThreadDump(ThreadInfo[] threads) {
        final var builder = new StringBuilder();
        builder.append("# Thread dump at ")
            .append(DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
            .append(System.lineSeparator())
            .append(System.lineSeparator());
        builder.append("## All active threads:");
        for (final var info : threads) {
            builder
                .append(System.lineSeparator())
                .append("- ")
                .append(info.name())
                .append(info.group() == null ? "" : "[" + info.group().name() + "]")
                .append('@')
                .append(info.id());
        }
        builder.append(System.lineSeparator())
            .append(System.lineSeparator())
            .append("## Detailed information about each thread:")
            .append(System.lineSeparator());
        for (final var thread : threads) {
            builder.append(System.lineSeparator());
            builder.append("- ");
            builder.append(buildThreadInfo(thread));
            for (final var stack : thread.stackElements()) {
                builder.append(System.lineSeparator())
                    .append("    ")
                    .append("at ")
                    .append(stack.toString());
            }
        }
        return builder.toString();
    }

    public String buildThreadInfo(final ThreadInfo thread) {
        return "\"%s@%s\" %sprio=%s %s".formatted(
            thread.group() == null ? thread.name() : thread.name() + " [" + thread.group().name() + "]",
            thread.id(),
            thread.daemon() ? "daemon " : "", thread.priority(), thread.state().toString().toLowerCase(Locale.ROOT)
        );
    }

    public boolean isEnabled(final String roleId) {
        for (var r : enabledRoles) {
            if (r.equals(roleId)) return true;
        }
        return false;
    }

    /**
     * Converts a given amount of bytes into friendlier data.<br>
     * Example: 2048 => 2 KB
     *
     * @param bytes the amount of bytes
     * @return the formatted string
     */
    public static String bytesToFriendly(long bytes) {
        // Find size of repo and list it
        int k = 1024;
        String[] measure = new String[]{"B", "KB", "MB", "GB", "TB"};
        double i;
        if (bytes == 0) {
            i = 0;
        } else {
            i = Math.floor(Math.log(bytes) / Math.log(k));
        }
        final var df = new DecimalFormat("#.##");
        return df.format(bytes / Math.pow(k, i)) + " " + measure[(int) i + 1];
    }
}
