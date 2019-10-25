package com.mcmoddev.bot.commands.locked.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.misc.BotConfig;
import com.mcmoddev.bot.misc.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;

public class CmdUser extends Command {

    public CmdUser() {
        name = "me";
        aliases = new String[]{"whoami", "myinfo"};
        help = "Get information about your own user. **Locked to <#572261616956211201>**";
    }

    @Override
    protected void execute(CommandEvent event) {
        Member member = event.getMember();
        EmbedBuilder embed = new EmbedBuilder();
        TextChannel channel = event.getTextChannel();
        Instant dateJoinedDiscord = member.getTimeCreated().toInstant();
        Instant dateJoinedMMD = member.getTimeJoined().toInstant();

        embed.setTitle("User info");
        embed.setColor(Color.WHITE);
        embed.setThumbnail(member.getUser().getEffectiveAvatarUrl());
        embed.addField("Username:", member.getUser().getName(), true);
        embed.addField("Users discriminator:", "#" + member.getUser().getDiscriminator(), true);
        embed.addField("Users id:", member.getId(), true);

        if (member.getNickname() != null) {
            embed.addField("Users nickname:", member.getNickname(), true);
        } else {
            embed.addField("Users nickname:", "No nickname applied.", true);
        }

        embed.addField("Joined Discord:", Utils.DATE.format(dateJoinedDiscord.toEpochMilli()), true);
        embed.addField("Joined MMD:", Utils.DATE.format(dateJoinedMMD.toEpochMilli()), true);
        embed.addField("Member for:", Utils.getTimeDifference(Utils.getLocalTime(dateJoinedMMD), LocalDateTime.now()), true);
        embed.setTimestamp(Instant.now());

        if (channel.getIdLong() != BotConfig.getConfig().getBotStuffChannelId()) {
            channel.sendMessage("This command is channel locked to <#572261616956211201>").queue();
        } else {
            channel.sendMessage(embed.build()).queue();
        }
    }
}