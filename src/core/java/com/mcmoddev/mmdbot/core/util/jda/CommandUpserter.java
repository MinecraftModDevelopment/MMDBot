package com.mcmoddev.mmdbot.core.util.jda;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.core.util.Pair;
import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Utility class for upserting commands. <br>
 * This class also listens for the {@link ReadyEvent} and upserts commands in that event.
 *
 * @author matyrobbrt
 */
public class CommandUpserter implements EventListener {

    private final CommandClient client;
    private final boolean forceGuild;
    @Nullable
    private final String guildId;

    /**
     * Instantiates a new upserter. <br>
     * If {@code forceGuild} is false, but a guild ID is still specified,
     * all the commands from that guild will be removed.
     *
     * @param client     the client that contains the command to upsert
     * @param forceGuild if commands should be forced to be guild-only
     * @param guildId    the ID of the guild commands should be upserted to
     */
    public CommandUpserter(final CommandClient client, final boolean forceGuild, final @Nullable String guildId) {
        this.client = client;
        this.forceGuild = forceGuild;
        this.guildId = guildId;
    }

    @Override
    public void onEvent(@NonNull final GenericEvent event) {
        if (event instanceof ReadyEvent) {
            upsertCommands(event.getJDA());
        }
    }

    /**
     * Upserts the command to the {@code jda} instance.
     *
     * @param jda the jda instance to upsert the commands to.
     */
    public void upsertCommands(@NonNull final JDA jda) {
        if (forceGuild) {
            final var guild = jda.getGuildById(Objects.requireNonNull(guildId));
            if (guild == null) throw new NullPointerException("Unknown guild with ID: " + guildId);
            guild.retrieveCommands().queue(commands -> {
                // Delete old commands.
                RestAction.allOf(getCommandsToRemove(commands).mapToObj(guild::deleteCommandById).toList()).queue();

                // Upsert new ones
                RestAction.allOf(client.getSlashCommands().stream()
                        .map(SlashCommand::buildCommandData)
                        .map(guild::upsertCommand)
                        .collect(Collectors.toSet()))
                    .queue();
            });
        } else {
            if (guildId != null) {
                final var guild = jda.getGuildById(Objects.requireNonNull(guildId));
                if (guild != null) {
                    // Guild still specified? Then remove guild commands
                    guild.retrieveCommands().flatMap(commands -> RestAction.allOf(commands.stream()
                            .map(Command::getIdLong)
                            .map(guild::deleteCommandById)
                            .toList()))
                        .queue();
                }
            }
            jda.retrieveCommands().queue(commands -> {
                // Delete old commands.
                RestAction.allOf(getCommandsToRemove(commands).mapToObj(jda::deleteCommandById).toList()).queue();

                // Upsert new ones
                RestAction.allOf(client.getSlashCommands().stream()
                        .map(SlashCommand::buildCommandData)
                        .map(jda::upsertCommand)
                        .collect(Collectors.toSet()))
                    .queue();
            });
        }
    }

    private LongStream getCommandsToRemove(final List<Command> existingCommands) {
        final var ext = existingCommands.stream()
            .map(c -> Pair.of(c.getName(), c.getIdLong()))
            .collect(Collectors.toSet());
        final var clientCommandNames = client.getSlashCommands().stream().map(SlashCommand::getName).collect(Collectors.toSet());
        ext.removeIf(p -> {
            final var contains = clientCommandNames.contains(p.first());
            if (contains) {
                clientCommandNames.remove(p.first());
            }
            return contains;
        });
        return ext.stream().mapToLong(Pair::second);
    }
}
