package com.mcmoddev.bot.events.users;

import com.mcmoddev.bot.misc.BotConfig;
import com.mcmoddev.bot.misc.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.time.Instant;

public class EventNicknameChanged extends ListenerAdapter {

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        TextChannel channel;
        EmbedBuilder embed = new EmbedBuilder();
        Long guildId = event.getGuild().getIdLong();
        String oldNick;
        String newNick;

        Utils.sleepTimer();

        if (event.getOldNickname() == null) {
            oldNick = event.getUser().getName();
        } else {
            oldNick = event.getOldNickname();
        }

        if (event.getNewNickname() == null) {
            newNick = event.getUser().getName();
        } else {
            newNick = event.getNewNickname();
        }

        if (BotConfig.getConfig().getGuildID().equals(guildId)) {
            channel = event.getGuild().getTextChannelById(BotConfig.getConfig().getChannelIDBasicEvents());

            embed.setColor(Color.YELLOW);
            embed.setTitle("Nickname Changed");
            embed.setThumbnail(event.getUser().getEffectiveAvatarUrl());
            embed.addField("User:", event.getUser().getName() + " #" + event.getUser().getDiscriminator(), true);
            embed.addField("User ID:", event.getUser().getId(), true);
            embed.addField("Old Nickname:", oldNick, true);
            embed.addField("New Nickname:", newNick, true);
            embed.setTimestamp(Instant.now());

            channel.sendMessage(embed.build()).queue();
        }
    }
}
