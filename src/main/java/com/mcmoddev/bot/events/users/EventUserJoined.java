package com.mcmoddev.bot.events.users;

import com.mcmoddev.bot.misc.BotConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.time.Instant;

public class EventUserJoined extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        Long guildId = event.getGuild().getIdLong();
        TextChannel channel = event.getGuild().getTextChannelById(BotConfig.getConfig().getChannelIDBasicEvents().toString());

        if (BotConfig.getConfig().getGuildID().equals(guildId)) {
            embed.setColor(Color.GREEN);
            embed.setTitle("User Joined");
            embed.setThumbnail(event.getUser().getEffectiveAvatarUrl());
            embed.addField("User:", event.getUser().getName() + " #" + event.getUser().getDiscriminator(), true);
            embed.addField("User ID:", event.getUser().getId(), true);
            embed.setTimestamp(Instant.now());

            channel.sendMessage(embed.build()).queue();
        }
    }
}
