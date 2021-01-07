package com.mcmoddev.mmdbot.commands.staff;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

import java.util.concurrent.TimeUnit;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;
import static com.mcmoddev.mmdbot.logging.MMDMarkers.MUTING;

public class CmdMute extends Command {

    public CmdMute() {
        super();
        name = "mute";
        help = "Mutes a user. Usage: !mmd-mute <userID/mention> [time, otherwise forever] [unit, otherwise minutes]";
        hidden = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (!Utils.checkCommand(this, event)) return;
        final Guild guild = event.getGuild();
        final MessageChannel channel = event.getChannel();
        final String[] args = event.getArgs().split(" ");
        final Member author = event.getGuild().getMember(event.getAuthor());
        if (author == null) return;
        final Member member = Utils.getMemberFromString(args[0], event.getGuild());
        final long mutedRoleID = getConfig().getRole("muted");
        final Role mutedRole = guild.getRoleById(mutedRoleID);

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
                } catch (NumberFormatException e) {
                    time1 = -1;
                }
                time = time1;
            } else {
                time = -1;
            }

            if (args.length > 2) {
                TimeUnit unit1;
                try {
                    unit1 = TimeUnit.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException e) {
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
                timeString = " " + time + " " + unit.toString().toLowerCase();
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
