package com.mcmoddev.mmdbot.commander.curseforge;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import io.github.matyrobbrt.curseforgeapi.util.CurseForgeException;
import io.github.matyrobbrt.curseforgeapi.util.ExceptionConsumer;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

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
            new AddProject()
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
                new OptionData(OptionType.INTEGER, "project-id", "The ID of the project to add to this channel.")
            );
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            final var pId = Objects.requireNonNull(event.getOption("project-id", OptionMapping::getAsInt));
            if (TheCommander.getInstance().getCurseForgeManager().isEmpty()) {
                event.deferReply(true)
                    .setContent("I am not configured with a (valid) CurseForge API key. Please contact the bot owner.")
                    .queue();
                return;
            }
            final var cfManager = TheCommander.getInstance().getCurseForgeManager().get();
            final var cfApi = cfManager.api();
            event.deferReply().queue(catchException(hook -> {
                // Start a check to see if the project ID is valid
                cfApi.getAsyncHelper().getMod(pId)
                    .queue(response -> {
                        if (response.isPresent()) {
                            // Project is present, add it to the manager and inform the user
                            final var mod = response.get();
                            cfManager.projects().addProject(pId, event.getChannel().getIdLong());
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

    private static Consumer<InteractionHook> catchException(ExceptionConsumer<InteractionHook, CurseForgeException> consumer) {
        return h -> {
            try {
                consumer.acceptWithException(h);
            } catch (CurseForgeException e) {
                h.editOriginal("There was an exception while executing that command!").queue();
                TheCommander.LOGGER.error("Exception while running a CurseForge Webhooks command", e);
            }
        };
    }
}
