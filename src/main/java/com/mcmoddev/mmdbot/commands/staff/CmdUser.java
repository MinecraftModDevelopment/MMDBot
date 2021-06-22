package com.mcmoddev.mmdbot.commands.staff;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 *
 * @author
 *
 */
public class CmdUser extends Command {

    /**
     *
     */
    public CmdUser() {
        super();
        name = "user";
        aliases = new String[]{"whois", "userinfo"};
        help = "Get information about another user with their user ID.";
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
        final var channel = event.getTextChannel();
        final var member = Utils.getMemberFromString(event.getArgs(), event.getGuild());
        if (member == null) {
            channel.sendMessage(String.format("User %s not found.", event.getArgs())).queue();
            return;
        }
        final EmbedBuilder embed = createMemberEmbed(member);
        channel.sendMessageEmbeds(embed.build()).queue();
    }

    /**
     *
     * @param member
     * @return EmbedBuilder.
     */
    protected EmbedBuilder createMemberEmbed(final Member member) {
        final var user = member.getUser();
        final var embed = new EmbedBuilder();
        final var dateJoinedDiscord = member.getTimeCreated().toInstant();
        final Instant dateJoinedMMD = Utils.getMemberJoinTime(member);

        embed.setTitle("User info");
        embed.setColor(Color.WHITE);
        embed.setThumbnail(user.getEffectiveAvatarUrl());
        embed.addField("Username:", user.getName(), true);
        embed.addField("Users discriminator:", "#" + user.getDiscriminator(), true);
        embed.addField("Users id:", member.getId(), true);

        if (member.getNickname() == null) {
            embed.addField("Users nickname:", "No nickname applied.", true);
        } else {
            embed.addField("Users nickname:", member.getNickname(), true);
        }

        final var date = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
        embed.addField("Joined Discord:", date.format(dateJoinedDiscord.toEpochMilli()), true);
        embed.addField("Joined MMD:", date.format(dateJoinedMMD.toEpochMilli()), true);
        embed.addField("Member for:", Utils.getTimeDifference(Utils.getLocalTime(dateJoinedMMD), LocalDateTime.now()), true);
        embed.setTimestamp(Instant.now());

        return embed;
    }
}
