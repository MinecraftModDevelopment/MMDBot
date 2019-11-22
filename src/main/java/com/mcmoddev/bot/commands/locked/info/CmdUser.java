package com.mcmoddev.bot.commands.locked.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.misc.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 *
 */
public final class CmdUser extends Command {

	/**
	 *
	 */
    public CmdUser() {
        super();
        name = "me";
        aliases = new String[]{"whoami", "myinfo"};
        help = "Get information about your own user. **Locked to <#" + MMDBot.getConfig().getBotStuffChannelId() + ">**";
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        final Member member = event.getMember();
        final User user = member.getUser();
        final EmbedBuilder embed = new EmbedBuilder();
        final TextChannel channel = event.getTextChannel();
        final Instant dateJoinedDiscord = member.getTimeCreated().toInstant();
        final Instant dateJoinedMMD = member.getTimeJoined().toInstant();

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

        final long channelID = MMDBot.getConfig().getBotStuffChannelId();
        if (channel.getIdLong() != channelID) {
            channel.sendMessage("This command is channel locked to <#" + channelID + ">").queue();
        } else {
            channel.sendMessage(embed.build()).queue();
        }
    }
}
