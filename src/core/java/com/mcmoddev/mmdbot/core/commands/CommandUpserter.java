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
package com.mcmoddev.mmdbot.core.commands;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.ContextMenu;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.core.util.EmptyRestAction;
import com.mcmoddev.mmdbot.core.util.Pair;
import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
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
    private static final Logger LOG = LoggerFactory.getLogger("CommandUpserter");

    private final CommandClient client;
    private final boolean forceGuild;
    @Nullable
    private final String guildId;
    private final Collection<Permission> invitePermissions;

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
        this.invitePermissions = List.of();
    }

    /**
     * Instantiates a new upserter. <br>
     * If {@code forceGuild} is false, but a guild ID is still specified,
     * all the commands from that guild will be removed.
     *
     * @param client     the client that contains the command to upsert
     * @param forceGuild if commands should be forced to be guild-only
     * @param guildId    the ID of the guild commands should be upserted to
     * @param invitePermissions a list of permissions the bot needs. This list will be used for generating an invite URL if the bot doesn't have the required scope in the forced guild.
     */
    public CommandUpserter(final CommandClient client, final boolean forceGuild, final @Nullable String guildId, final @NonNull Collection<Permission> invitePermissions) {
        this.client = client;
        this.forceGuild = forceGuild;
        this.guildId = guildId;
        this.invitePermissions = invitePermissions;
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
                final var toRemove = getCommandsToRemove(commands);
                if (toRemove.length > 0) {
                    RestAction.allOf(LongStream.of(toRemove).mapToObj(guild::deleteCommandById).toList()).queue();
                }

                // Upsert new ones
                RestAction.allOf(client.getSlashCommands().stream()
                        .map(SlashCommand::buildCommandData)
                        .map(guild::upsertCommand)
                        .collect(Collectors.toSet()))
                    .queue(cmds -> LOG.info("Registered {} commands to guild '{}' ({})", cmds.size(), guild.getName(), guild.getId())
                        , createErrorHandler(guild));

                if (!client.getContextMenus().isEmpty()) {
                    // Upsert menus
                    RestAction.allOf(client.getContextMenus().stream()
                            .map(ContextMenu::buildCommandData)
                            .map(guild::upsertCommand)
                            .collect(Collectors.toSet()))
                        .queue(cmds -> LOG.info("Registered {} context menus to guild '{}' ({})", cmds.size(), guild.getName(), guild.getId()), createErrorHandler(guild));
                }
            });
        } else {
            if (guildId != null) {
                final var guild = jda.getGuildById(Objects.requireNonNull(guildId));
                if (guild != null) {
                    // Guild still specified? Then remove guild commands
                    guild.retrieveCommands()
                        .flatMap(commands -> commands.isEmpty() ? EmptyRestAction.empty() : RestAction.allOf(commands.stream()
                            .map(Command::getIdLong)
                            .map(guild::deleteCommandById)
                            .toList()))
                        .queue();
                }
            }
            jda.retrieveCommands().queue(commands -> {
                // Delete old commands.
                final var toRemove = getCommandsToRemove(commands);
                if (toRemove.length > 1) {
                    RestAction.allOf(LongStream.of(toRemove).mapToObj(jda::deleteCommandById).toList()).queue();
                }

                // Upsert new ones
                RestAction.allOf(client.getSlashCommands()
                        .stream()
                        .map(SlashCommand::buildCommandData)
                        .map(jda::upsertCommand)
                        .collect(Collectors.toSet()))
                    .queue(cmds -> LOG.info("Registered {} global commands.", cmds.size()));

                if (!client.getContextMenus().isEmpty()) {
                    // Upsert menus
                    RestAction.allOf(client.getContextMenus().stream()
                            .map(ContextMenu::buildCommandData)
                            .map(jda::upsertCommand)
                            .collect(Collectors.toSet()))
                        .queue(cmds -> LOG.info("Registered {} context menus.", cmds.size()));
                }
            });
        }
    }

    public ErrorHandler createErrorHandler(final Guild guild) {
        return new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, e -> {
            // Find the first channel in which the bot can talk to send the information message
            final var reportChannel = guild.getTextChannels()
                .stream()
                .filter(TextChannel::canTalk)
                .findFirst();

            reportChannel.map(channel -> channel.sendMessage("""
                    I require the `application.commands` scope. Please invite me correctly.
                    A direct message with a valid invite URL will be sent to the guild owner.
                    I will now leave this guild."""))
                .map(action -> action.flatMap($ -> guild.retrieveOwner()))
                    .map(action -> action.flatMap(m -> m.getUser().openPrivateChannel()))
                    .map(action -> action.flatMap(dm -> dm.sendMessage("""
                        I could not register guild-forced commands in the server you own, %s.
                        Below is an invite URL that invites the bot with the required scopes:
                        %s"""
                        .formatted(guild.getName(), guild.getJDA().getInviteUrl(invitePermissions)))))
                .map(action -> action.flatMap($ -> guild.leave()))
                .ifPresent(action -> action.queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER)));

            LOG.error("Guild '{}' does not have the required scope. Unable to register forced guild commands. Leaving the guild...", guild.getId());
        });
    }

    private long[] getCommandsToRemove(final List<Command> existingCommands) {
        final var ext = existingCommands.stream()
            .filter(c -> c.getType() == Command.Type.SLASH)
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
        return ext.stream().mapToLong(Pair::second).toArray();
    }
}
