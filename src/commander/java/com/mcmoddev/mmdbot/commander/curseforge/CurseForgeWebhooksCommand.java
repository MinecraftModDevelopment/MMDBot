package com.mcmoddev.mmdbot.commander.curseforge;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import io.github.matyrobbrt.curseforgeapi.util.CurseForgeException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * The command used for CurseForge webhook managing
 * @author matyrobbrt
 */
public class CurseForgeWebhooksCommand extends SlashCommand {

    public CurseForgeWebhooksCommand() {
        this.name = "curseforge-webhooks";
        guildOnly = true;
        help = "Commands regarding CurseForge webhooks.";
        userPermissions = new Permission[] {
            Permission.MANAGE_WEBHOOKS
        };
        children = new SlashCommand[] {
            new AddProject(), new RemoveProject(), new ListProjects()
        };
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
    }

    public static final class AddProject extends SlashCommand {

        private AddProject() {
            this.name = "add-project";
            guildOnly = true;
            help = "Adds a CurseForge project to this channel.";
            userPermissions = new Permission[] {
                Permission.MANAGE_WEBHOOKS
            };
            options = List.of(
                new OptionData(OptionType.INTEGER, "project-id", "The ID of the project to add to this channel.", true)
            );
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
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
        }
    }

    public static final class RemoveProject extends SlashCommand {

        private RemoveProject() {
            this.name = "remove-project";
            guildOnly = true;
            help = "Adds a CurseForge project from this channel.";
            userPermissions = new Permission[] {
                Permission.MANAGE_WEBHOOKS
            };
            options = List.of(
                new OptionData(OptionType.INTEGER, "project-id", "The ID of the project to remove from this channel.", true)
            );
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
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
        }
    }

    public static final class ListProjects extends SlashCommand {

        private ListProjects() {
            this.name = "list-projects";
            guildOnly = true;
            help = "Lists all of the CurseForge projects added to this channel.";
            userPermissions = new Permission[] {
                Permission.MANAGE_WEBHOOKS
            };
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            checkConfigured(event, catchException((hook, manager) -> {
                final var projects = manager.projects().getProjectsForChannel(event.getChannel().getIdLong());
                final var embed = new EmbedBuilder()
                    .setTitle("Projects in this channel")
                    .setDescription(projects
                        .stream()
                        .map(String::valueOf)
                        .toList()
                        .subList(0, projects.size())
                        .stream()
                        .reduce("", (a, b) -> a + "\n" + b))
                    .setTimestamp(Instant.now());
                hook.editOriginalEmbeds(embed.build()).queue();
            }));
        }
    }

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
