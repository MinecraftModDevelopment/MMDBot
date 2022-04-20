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
package com.mcmoddev.mmdbot.commander.cfwebhooks;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.core.util.builder.SlashCommandBuilder;
import io.github.matyrobbrt.curseforgeapi.util.CurseForgeException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.time.Instant;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * The command used for CurseForge webhook managing
 *
 * @author matyrobbrt
 */
public final class CurseForgeWebhooksCommand {
    private static final SlashCommand ADD_PROJECT_CMD = SlashCommandBuilder.builder()
        .name("add-project")
        .help("Adds a CurseForge project from this channel.")
        .guildOnly(true)
        .userPermissions(Permission.MANAGE_WEBHOOKS)
        .options(new OptionData(OptionType.INTEGER, "project-id", "The ID of the project to add to this channel.", true))
        .executes(event -> {
            final var pId = Objects.requireNonNull(event.getOption("project-id", OptionMapping::getAsInt));
            checkConfigured(event, catchException((hook, manager) -> {
                // Start a check to see if the project ID is valid
                manager.api().getAsyncHelper().getMod(pId)
                    .queue(response -> {
                        if (response.isPresent()) {
                            // Project is present, add it to the manager and inform the user
                            final var mod = response.get();
                            manager.projects().addProject(pId, event.getChannel().getIdLong());
                            hook.editOriginal("Successfully added project **%s** (%s) to this channel!"
                                    .formatted(mod.name(), mod.id()))
                                .queue();
                        } else {
                            // Unknown project.. Inform the user!
                            hook.editOriginal("Could not find project with the ID **%s**!".formatted(pId)).queue();
                        }
                    });
            }));
        })
        .build();

    private static final SlashCommand REMOVE_PROJECT_CMD = SlashCommandBuilder.builder()
        .name("remove-project")
        .guildOnly(true)
        .userPermissions(Permission.MANAGE_WEBHOOKS)
        .help("Removes a CurseForge project from this channel.")
        .options(new OptionData(OptionType.INTEGER, "project-id", "The ID of the project to remove from this channel.", true))
        .executes(event -> {
            final var pId = Objects.requireNonNull(event.getOption("project-id", OptionMapping::getAsInt));
            checkConfigured(event, catchException((hook, manager) -> {
                // Start a check to see if the project ID is valid
                manager.api().getAsyncHelper().getMod(pId)
                    .queue(response -> {
                        if (response.isPresent()) {
                            // Project is present, remove it from the manager and inform the user
                            final var mod = response.get();
                            manager.projects().removeProject(pId, event.getChannel().getIdLong());
                            hook.editOriginal("Successfully removed project **%s** (%s) from this channel!"
                                    .formatted(mod.name(), mod.id()))
                                .queue();
                        } else {
                            // Unknown project.. Inform the user!
                            hook.editOriginal("Could not find project with the ID **%s**!".formatted(pId)).queue();
                        }
                    });
            }));
        })
        .build();

    private static final SlashCommand LIST_PROJECTS_CMD = SlashCommandBuilder.builder()
        .name("list-projects")
        .guildOnly(true)
        .help("Lists all of the CurseForge projects added to this channel.")
        .executes(event -> checkConfigured(event, catchException((hook, manager) -> {
            final var projects = manager.projects().getProjectsForChannel(event.getChannel().getIdLong());
            final var embed = new EmbedBuilder()
                .setTitle("Projects in this channel")
                .setDescription(projects
                    .stream()
                    .map(String::valueOf)
                    .reduce("", (a, b) -> a + "\n" + b))
                .setTimestamp(Instant.now());
            hook.editOriginalEmbeds(embed.build()).queue();
        })))
        .build();

    private static final SlashCommand REFRESH_PROJECTS_CMD = SlashCommandBuilder.builder()
        .name("refresh")
        .guildOnly(true)
        .help("Re-runs the CurseForge update checkers.")
        .executes(event -> checkConfigured(event, catchException((hook, manager) -> {
            if (!event.getMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
                hook.editOriginal("This command requires the Manage Webhooks permission across the entire guild!").queue();
                return;
            }
            TheCommander.CURSE_FORGE_UPDATE_SCHEDULER.execute(manager.projects());
            hook.editOriginal("Refreshing CurseForge update checkers.").queue();
        })))
        .build();

    @RegisterSlashCommand
    public static final SlashCommand INSTANCE = SlashCommandBuilder.builder()
        .name("curseforge-webhooks")
        .guildOnly(true)
        .help("Commands regarding CurseForge webhooks.")
        .userPermissions(Permission.MANAGE_WEBHOOKS)
        .children(ADD_PROJECT_CMD, REMOVE_PROJECT_CMD, LIST_PROJECTS_CMD, REFRESH_PROJECTS_CMD)
        .build();

    private static void checkConfigured(SlashCommandEvent event, BiConsumer<InteractionHook, CurseForgeManager> consumer) {
        final var curseManager = TheCommander.getInstance().getCurseForgeManager();
        curseManager.ifPresentOrElse(manager -> event.deferReply().queue(hook -> consumer.accept(hook, manager)),
            () -> event.deferReply(true)
                .setContent("I am not configured with a (valid) CurseForge API key. Please contact the bot owner.")
                .queue());
    }

    @FunctionalInterface
    public interface ExceptionBiConsumer<F, S, E extends Exception> {
        void accept(F f, S s) throws E;
    }

    private static BiConsumer<InteractionHook, CurseForgeManager> catchException(ExceptionBiConsumer<InteractionHook, CurseForgeManager, CurseForgeException> consumer) {
        return (h, m) -> {
            try {
                consumer.accept(h, m);
            } catch (CurseForgeException e) {
                h.editOriginal("There was an exception while executing that command!").queue();
                TheCommander.LOGGER.error("Exception while running a CurseForge Webhooks command", e);
            }
        };
    }
}
