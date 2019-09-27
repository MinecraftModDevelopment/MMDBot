package com.mcmoddev.bot.events.users;

import com.mcmoddev.bot.misc.BotConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.time.Instant;

public class EventUserLeft extends ListenerAdapter {

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        Long guildId = event.getGuild().getIdLong();
        TextChannel channel = event.getGuild().getTextChannelById(BotConfig.getConfig().getChannelIDBasicEvents());

        if (BotConfig.getConfig().getGuildID().equals(guildId)) {
            embed.setColor(Color.RED);
            embed.setTitle("User Left");
            embed.setThumbnail(event.getUser().getEffectiveAvatarUrl());
            embed.addField("User:", event.getUser().getName() + " #" + event.getUser().getDiscriminator(), true);
            embed.addField("User ID:", event.getUser().getId(), true);
            //TODO Get the roles a user has on leaving the server and save them to a dat file of sorts or a DB.
            embed.setTimestamp(Instant.now());

            channel.sendMessage(embed.build()).queue();
        }
    }
}
