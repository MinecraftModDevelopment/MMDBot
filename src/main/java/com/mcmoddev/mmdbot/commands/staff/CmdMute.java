package com.mcmoddev.mmdbot.commands.staff;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;
import static com.mcmoddev.mmdbot.logging.MMDMarkers.MUTING;

/**
 *
 * @author
 *
 */
public final class CmdMute extends Command {

   /**
    *
    */
    public CmdMute() {
        super();
        name = "mute";
        help = "Mutes a user. Usage: !mmd-mute <userID/mention> [time, otherwise forever] [unit, otherwise minutes]";
        hidden = true;
    }

    /**
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }
        final var guild = event.getGuild();
        final var author = guild.getMember(event.getAuthor());
        if (author == null) {
            return;
        }
        final String[] args = event.getArgs().split(" ");
        final var member = Utils.getMemberFromString(args[0], event.getGuild());
        final long mutedRoleID = getConfig().getRole("muted");
        final var mutedRole = guild.getRoleById(mutedRoleID);
        final MessageChannel channel = event.getChannel();

        if (author.hasPermission(Permission.KICK_MEMBERS)) {
            if (member == null) {
                channel.sendMessage(String.format("User %s not found.", event.getArgs())).queue();
                return;
            }

            if (mutedRole == null) {
                LOGGER.error(MUTING, "Unable to find muted role {}", mutedRoleID);
                return;
            }

            final long time;
            final TimeUnit unit;
            if (args.length > 1) {
                long time1;
                try {
                    time1 = Long.parseLong(args[1]);
                } catch (NumberFormatException ex) {
                    time1 = -1;
                }
                time = time1;
            } else {
                time = -1;
            }

            if (args.length > 2) {
                TimeUnit unit1;
                try {
                    unit1 = TimeUnit.valueOf(args[2].toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException ex) {
                    unit1 = TimeUnit.MINUTES;
                }
                unit = unit1;
            } else {
                unit = TimeUnit.MINUTES;
            }

            guild.addRoleToMember(member, mutedRole).queue();

            if (time > 0) {
                guild.removeRoleFromMember(member, mutedRole).queueAfter(time, unit);
            }

            final String timeString;
            if (time > 0) {
                timeString = " " + time + " " + unit.toString().toLowerCase(Locale.ROOT);
            } else {
                timeString = "ever";
            }

            channel.sendMessageFormat("Muted user %s for%s.", member.getAsMention(), timeString).queue();
            LOGGER.info(MUTING, "User {} was muted by {} for{}", member, author, timeString);
        } else {
            channel.sendMessage("You do not have permission to use this command.").queue();
        }
    }
}
