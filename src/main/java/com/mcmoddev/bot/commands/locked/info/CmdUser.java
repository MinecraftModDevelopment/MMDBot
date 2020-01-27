package com.mcmoddev.bot.commands.locked.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.misc.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Locale;

/**
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
        help = "Get information about another user with their user ID. **Locked to <#" + MMDBot.getConfig().getBotStuffChannelId() + ">**";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        final TextChannel channel = event.getTextChannel();
        final Member member = Utils.getMemberFromString(event.getArgs(), event.getGuild());

        if (member == null) {
            channel.sendMessage(String.format("User %s not found.", event.getArgs())).queue();
            return;
        }

        final EmbedBuilder embed = createMemberEmbed(member);

        final long channelID = MMDBot.getConfig().getBotStuffChannelId();
        if (channel.getIdLong() != channelID) {
            channel.sendMessage("This command is channel locked to <#" + channelID + ">").queue();
        } else {
            channel.sendMessage(embed.build()).queue();
        }
    }

    protected EmbedBuilder createMemberEmbed(final Member member) {
        final User user = member.getUser();
        final EmbedBuilder embed = new EmbedBuilder();
        final Instant dateJoinedDiscord = member.getTimeCreated().toInstant();
        final Instant dateJoinedMMD = Utils.getMemberJoinTime(member);

        embed.setTitle("User info");
        embed.setColor(Color.WHITE);
        embed.setThumbnail(user.getEffectiveAvatarUrl());
        embed.addField("Username:", user.getName(), true);
        embed.addField("Users discriminator:", "#" + user.getDiscriminator(), true);
        embed.addField("Users id:", member.getId(), true);

        if (member.getNickname() != null) {
            embed.addField("Users nickname:", member.getNickname(), true);
        } else {
            embed.addField("Users nickname:", "No nickname applied.", true);
        }

        final SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
        embed.addField("Joined Discord:", date.format(dateJoinedDiscord.toEpochMilli()), true);
        embed.addField("Joined MMD:", date.format(dateJoinedMMD.toEpochMilli()), true);
        embed.addField("Member for:", Utils.getTimeDifference(Utils.getLocalTime(dateJoinedMMD), LocalDateTime.now()), true);
        embed.setTimestamp(Instant.now());

        return embed;
    }
}
