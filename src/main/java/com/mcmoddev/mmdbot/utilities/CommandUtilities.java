package com.mcmoddev.mmdbot.utilities;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.modules.commands.community.server.DeletableCommand;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mcmoddev.mmdbot.MMDBot.getConfig;

/**
 * Utility methods related to commands and slash commands.
 */
public class CommandUtilities {
    /**
     * Prevent instantiation of this utility class.
     */
    private CommandUtilities() {
    }

    /**
     * Checks if the command can run in the given context, and returns if it should continue running.
     * <p>
     * This does the following checks in order (checks prefixed with GUILD will only take effect when ran from a
     * {@linkplain TextChannel guild channel}):
     * <ul>
     *     <li>GUILD; checks if the command is enabled in the guild.</li>
     *     <li>not in GUILD; checks if the command is enabled globally.</li>
     *     <li>GUILD: checks if the command is blocked in the channel/category.</li>
     *     <li>GUILD: checks if the command is allowed in the channel/category.</li>
     * </ul>
     *
     * @param command The command
     * @param event   The command event
     * @return If the command can run in that context
     */
    public static boolean checkCommand(final Command command, final CommandEvent event) {

        if (!isEnabled(command, event)) {
            //Could also send an informational message.
            return false;
        }

        if (event.isFromType(ChannelType.TEXT)) {
            final List<Long> exemptRoles = getConfig().getChannelExemptRoles();
            if (event.getMember().getRoles().stream()
                .map(ISnowflake::getIdLong)
                .anyMatch(exemptRoles::contains)) {
                //The member has a channel-checking-exempt role, bypass checking and allow the command.
                return true;
            }
        }

        if (isBlocked(command, event)) {
            event.getChannel()
                .sendMessage("This command is blocked from running in this channel!")
                .queue();
            return false;
        }

        //Sent from a guild.
        if (event.isFromType(ChannelType.TEXT)) {
            final List<Long> allowedChannels = getConfig().getAllowedChannels(command.getName(),
                event.getGuild().getIdLong());

            //If the allowlist is empty, default allowed.
            if (allowedChannels.isEmpty()) {
                return true;
            }

            final var channelID = event.getChannel().getIdLong();
            @Nullable final var category = event.getTextChannel().getParentCategory();
            boolean allowed;
            if (category == null) {
                allowed = allowedChannels.stream().anyMatch(id -> id == channelID);
            } else { // If there's a category, also check that
                final var categoryID = category.getIdLong();
                allowed = allowedChannels.stream()
                    .anyMatch(id -> id == channelID || id == categoryID);
            }

            if (!allowed) {
                final List<Long> hiddenChannels = getConfig().getHiddenChannels();
                final String allowedChannelStr = allowedChannels.stream()
                    .filter(id -> !hiddenChannels.contains(id))
                    .map(id -> "<#" + id + ">")
                    .collect(Collectors.joining(", "));

                final StringBuilder str = new StringBuilder(84)
                    .append("This command cannot be run in this channel");
                if (!allowedChannelStr.isEmpty()) {
                    str.append(", only in ")
                        .append(allowedChannelStr);
                }
                event.getChannel()
                    //TODO: Remove the allowed channel string?
                    .sendMessage(str.append("!"))
                    .queue();
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the command can run in the given context, and returns if it should continue running.
     * <p>
     * This does the following checks in order (checks prefixed with GUILD will only take effect when ran from a
     * {@linkplain TextChannel guild channel}):
     * <ul>
     *     <li>GUILD; checks if the command is enabled in the guild.</li>
     *     <li>not in GUILD; checks if the command is enabled globally.</li>
     *     <li>GUILD: checks if the command is blocked in the channel/category.</li>
     *     <li>GUILD: checks if the command is allowed in the channel/category.</li>
     * </ul>
     * <p>
     * For Slash commands only.
     *
     * @param command The command
     * @param event   The command event
     * @return If the command can run in that context
     */
    public static boolean checkCommand(final Command command, final SlashCommandEvent event) {

        if (!isEnabled(command, event)) {
            //Could also send an informational message.
            return false;
        }

        if (event.isFromGuild()) {
            final List<Long> exemptRoles = getConfig().getChannelExemptRoles();
            if (event.getMember().getRoles().stream()
                .map(ISnowflake::getIdLong)
                .anyMatch(exemptRoles::contains)) {
                //The member has a channel-checking-exempt role, bypass checking and allow the command.
                return true;
            }
        }

        if (isBlocked(command, event)) {
            event.reply("This command is blocked from running in this channel!")
                .setEphemeral(true)
                .queue();
            return false;
        }

        //Sent from a guild.
        if (event.isFromGuild()) {
            final List<Long> allowedChannels = getConfig().getAllowedChannels(command.getName(),
                event.getGuild().getIdLong());

            //If the allowlist is empty, default allowed.
            if (allowedChannels.isEmpty()) {
                return true;
            }

            final var channelID = event.getChannel().getIdLong();
            @Nullable final var category = event.getTextChannel().getParentCategory();
            boolean allowed;
            if (category == null) {
                allowed = allowedChannels.stream().anyMatch(id -> id == channelID);
            } else { // If there's a category, also check that
                final var categoryID = category.getIdLong();
                allowed = allowedChannels.stream()
                    .anyMatch(id -> id == channelID || id == categoryID);
            }

            if (!allowed) {
                final List<Long> hiddenChannels = getConfig().getHiddenChannels();
                final String allowedChannelStr = allowedChannels.stream()
                    .filter(id -> !hiddenChannels.contains(id))
                    .map(id -> "<#" + id + ">")
                    .collect(Collectors.joining(", "));

                final StringBuilder str = new StringBuilder(84)
                    .append("This command cannot be run in this channel");
                if (!allowedChannelStr.isEmpty()) {
                    str.append(", only in ")
                        .append(allowedChannelStr);
                }
                event.reply(str.append("!").toString())
                    .setEphemeral(true)
                    .queue();
                return false;
            }
        }

        if (command instanceof DeletableCommand && ((DeletableCommand) command).isDeleted()) {
            return false;
        }

        return true;
    }

    /**
     * Is enabled boolean.
     * For textual commands only.
     *
     * @param command the command
     * @param event   the event
     * @return boolean. boolean
     */
    private static boolean isEnabled(final Command command, final CommandEvent event) {
        if (event.isFromType(ChannelType.TEXT)) { // Sent from a guild
            return getConfig().isEnabled(command.getName(), event.getGuild().getIdLong());
        }
        return getConfig().isEnabled(command.getName());
    }

    /**
     * Is enabled boolean.
     * For Slash commands only.
     *
     * @param command the command
     * @param event   the event
     * @return boolean. boolean
     */
    private static boolean isEnabled(final Command command, final SlashCommandEvent event) {
        if (event.isFromGuild()) { // Sent from a guild
            return getConfig().isEnabled(command.getName(), event.getGuild().getIdLong());
        }
        return getConfig().isEnabled(command.getName());
    }

    /**
     * Is blocked boolean.
     * For textual commands only.
     *
     * @param command the command
     * @param event   the event
     * @return boolean. boolean
     */
    private static boolean isBlocked(final Command command, final CommandEvent event) {
        if (event.isFromType(ChannelType.TEXT)) { // Sent from a guild
            final var channelID = event.getChannel().getIdLong();
            final List<Long> blockedChannels = getConfig().getBlockedChannels(command.getName(),
                event.getGuild().getIdLong());
            @Nullable final var category = event.getTextChannel().getParentCategory();
            if (category != null) {
                final var categoryID = category.getIdLong();
                return blockedChannels.stream()
                    .anyMatch(id -> id == channelID || id == categoryID);
            }
            return blockedChannels.stream().anyMatch(id -> id == channelID);
        }
        return false; // If not from a guild, default not blocked
    }

    /**
     * Is blocked boolean.
     * For Slash commands only.
     *
     * @param command the command
     * @param event   the event
     * @return boolean. boolean
     */
    private static boolean isBlocked(final Command command, final SlashCommandEvent event) {
        if (event.isFromGuild()) { // Sent from a guild
            final var channelID = event.getChannel().getIdLong();
            final List<Long> blockedChannels = getConfig().getBlockedChannels(command.getName(),
                event.getGuild().getIdLong());
            @Nullable final var category = event.getChannelType() == ChannelType.TEXT ?
                event.getTextChannel().getParentCategory() : null;
            if (category != null) {
                final var categoryID = category.getIdLong();
                return blockedChannels.stream()
                    .anyMatch(id -> id == channelID || id == categoryID);
            }
            return blockedChannels.stream().anyMatch(id -> id == channelID);
        }
        return false; // If not from a guild, default not blocked
    }

    /**
     * Get a non-null string from an OptionMapping.
     *
     * @param option an OptionMapping to get as a string - may be null
     * @return the option mapping as a string, or an empty string if the mapping was null
     */
    public static String getOrEmpty(@Nullable OptionMapping option) {
        return Optional.ofNullable(option).map(OptionMapping::getAsString).orElse("");
    }

    /**
     * Gets an argument from a slash command as a string.
     *
     * @param event the slash command event
     * @param name  the name of the option
     * @return the option's value as a string, or an empty string if the option had no value
     */
    public static String getOrEmpty(SlashCommandEvent event, String name) {
        return getOrEmpty(event.getOption(name));
    }

    public static <T> T getArgumentOr(SlashCommandEvent event, String name, Function<OptionMapping, T> getter, T orElse) {
        final var option = event.getOption(name);
        if (option != null) {
            return getter.apply(option);
        } else {
            return orElse;
        }
    }

    public static void clearGuildCommands(final @Nonnull Guild guild, final Runnable... after) {
        guild.retrieveCommands().queue(cmds -> {
            for (int i = 0; i < cmds.size(); i++) {
                try {
                    guild.deleteCommandById(cmds.get(i).getIdLong()).submit().get();
                } catch (InterruptedException | ExecutionException e) {
                    MMDBot.LOGGER.error("Error while trying to clear the commands of the guild {}", guild, e);
                }
            }
            for (var toRun : after) {
                toRun.run();
            }
        });
    }

    public static void clearGlobalCommands(final Runnable... after) {
        MMDBot.getJDA().retrieveCommands().queue(cmds -> {
            for (int i = 0; i < cmds.size(); i++) {
                try {
                    MMDBot.getJDA().deleteCommandById(cmds.get(i).getIdLong()).submit().get();
                } catch (InterruptedException | ExecutionException e) {
                    MMDBot.LOGGER.error("Error while trying to clear the global commands", e);
                }
            }
            for (var toRun : after) {
                toRun.run();
            }
        });
    }
}
